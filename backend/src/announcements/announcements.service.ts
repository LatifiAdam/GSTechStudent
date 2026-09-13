import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';

import { InjectRepository } from '@nestjs/typeorm';

import { In, Repository } from 'typeorm';

import { Annonce, TypeAnnonce } from '../entities/annonce.entity';
import { Cours } from '../entities/cours.entity';
import { Affectation } from '../entities/affectation.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Utilisateur } from '../entities/utilisateur.entity';

import { CreateAnnouncementDto } from './dto/create-announcement.dto';

import { NotificationsService } from '../notifications/notifications.service';

import { Role } from '../common/enums/roles.enum';

@Injectable()
export class AnnouncementsService {
  constructor(
    @InjectRepository(Annonce)
    private readonly annonceRepo: Repository<Annonce>,

    @InjectRepository(Cours)
    private readonly coursRepo: Repository<Cours>,

    @InjectRepository(Affectation)
    private readonly affectationRepo: Repository<Affectation>,

    @InjectRepository(stagiaire)
    private readonly stagiaireRepo: Repository<stagiaire>,

    @InjectRepository(Utilisateur)
    private readonly utilisateurRepo: Repository<Utilisateur>,

    private readonly notifications: NotificationsService,
  ) {}

  // ============================================================
  // GET /announcements
  // ============================================================

  async findAll(requesterId: string, requesterRole: Role, classId?: string, authorRole?: Role, year?: number) {
    const qb = this.annonceRepo.createQueryBuilder('a')
      .leftJoinAndSelect('a.cours', 'cours')
      .leftJoinAndSelect('a.classe', 'classe')
      .leftJoinAndSelect('a.auteur', 'auteur')
      .orderBy('a.date_publication', 'DESC');

    // Formateurs see their own history by default. Stagieres see only relevant
    // announcements: their class + general admin announcements.
    if (requesterRole === Role.FORMATEUR) {
      qb.andWhere('a.id_auteur = :requesterId', { requesterId });
      if (classId) qb.andWhere('a.id_classe = :classId', { classId });
      if (year) { const start = yearStart(year); const end = new Date(start.getFullYear() + 1, start.getMonth(), start.getDate()); qb.andWhere('a.date_publication >= :start AND a.date_publication < :end', { start, end }); }
    } else if (requesterRole === Role.stagiaire) {
      qb.innerJoin('stagiaire', 'e', 'e.id_utilisateur = :requesterId', { requesterId });
      qb.andWhere('(a.id_classe = e.id_classe OR (a.id_classe IS NULL AND a.type_annonce = :general))', { general: TypeAnnonce.GENERALE });
      if (authorRole === Role.FORMATEUR) qb.andWhere('a.id_classe IS NOT NULL');
      if ((authorRole === Role.DF || authorRole === Role.DIRECTEUR)) qb.andWhere('a.type_annonce = :general2', { general2: TypeAnnonce.GENERALE });
    } else if (authorRole) {
      if ((authorRole === Role.DF || authorRole === Role.DIRECTEUR)) qb.andWhere('a.type_annonce = :general', { general: TypeAnnonce.GENERALE });
      else qb.andWhere('a.id_classe IS NOT NULL');
    }
    return qb.getMany();
  }

  // ============================================================
  // POST /announcements
  // ============================================================

  async create(
    dto: CreateAnnouncementDto,
    authorId: string,
    authorRole: Role,
  ) {
    /*
     * GENERAL ANNOUNCEMENT
     *
     * According to the project rules, only an administrator
     * can publish a general announcement.
     */
    const typeAnnonce = dto.typeAnnonce ?? ((authorRole === Role.DF || authorRole === Role.DIRECTEUR) && !dto.idClasse ? TypeAnnonce.GENERALE : TypeAnnonce.CONTROLE);

    if (!dto.idClasse && !dto.idCours) {
      if ((authorRole !== Role.DF && authorRole !== Role.DIRECTEUR)) {
        throw new ForbiddenException(
          'Seul un administrateur peut publier une annonce générale',
        );
      }
    }

    if (dto.idClasse) {
      const affectation = await this.affectationRepo.findOne({ where: { idClasse: dto.idClasse, idFormateur: authorId } });
      if (authorRole === Role.FORMATEUR && !affectation) throw new ForbiddenException('Vous ne pouvez publier une annonce que pour une classe qui vous est affectée');
      if (!affectation && (authorRole !== Role.DF && authorRole !== Role.DIRECTEUR)) throw new ForbiddenException('Classe non autorisée');
      if (authorRole === Role.FORMATEUR && dto.idCours && (!affectation || affectation.idCours !== dto.idCours)) {
        throw new ForbiddenException('La classe et le cours sélectionnés ne correspondent pas à votre affectation');
      }
    }

    /*
     * COURSE ANNOUNCEMENT
     *
     * The course must exist.
     */
    if (dto.idCours) {
      const cours = await this.coursRepo.findOne({
        where: {
          idCours: dto.idCours,
        },
      });

      if (!cours) {
        throw new NotFoundException('Cours introuvable');
      }

      /*
       * An instructor can only publish an announcement
       * for a course assigned to him through AFFECTATION.
       */
      if (authorRole === Role.FORMATEUR) {
        const affectation =
          await this.affectationRepo.findOne({
            where: {
              idCours: dto.idCours,
              idFormateur: authorId,
            },
          });

        if (!affectation) {
          throw new ForbiddenException(
            'Vous ne pouvez publier une annonce que pour un cours que vous enseignez',
          );
        }
      }
    }

    /*
     * The schema.sql does not contain:
     *
     * - priorite
     * - audience_cible
     * - date_planifiee
     * - publiee
     *
     * Therefore announcements are always published immediately.
     */
    const annonce = this.annonceRepo.create({
      titre: dto.titre.trim(),

      contenu: dto.contenu.trim(),

      typeAnnonce,

      dateEvenement: dto.dateEvenement
        ? new Date(dto.dateEvenement)
        : null,

      idCours: dto.idCours ?? null,

      idClasse: dto.idClasse ?? null,

      idAuteur: authorId,
    });

    const savedAnnonce =
      await this.annonceRepo.save(annonce);

    /*
     * The schema has no audience column.
     *
     * Therefore:
     *
     * - course announcement -> stagieres of classes assigned
     *   to that course
     *
     * - general announcement -> all users
     */
    await this.dispatchNotifications(savedAnnonce);

    return savedAnnonce;
  }

  // ============================================================
  // NOTIFICATIONS
  // ============================================================

  async dispatchNotifications(
    annonce: Annonce,
  ) {
    let recipientIds: string[] = [];

    /*
     * COURSE ANNOUNCEMENT
     *
     * Annonce
     *   -> Cours
     *      -> Affectation
     *         -> Classe
     *            -> stagiaire
     */
    if (annonce.idClasse) {
      const stagiaires = await this.stagiaireRepo.find({ where: { idClasse: annonce.idClasse } });
      recipientIds = stagiaires.map((stagiaire) => stagiaire.idUtilisateur);
    } else if (annonce.idCours) {
      const affectations =
        await this.affectationRepo.find({
          where: {
            idCours: annonce.idCours,
          },
        });

      const classeIds = [
        ...new Set(
          affectations
            .map(
              (affectation) =>
                affectation.idClasse,
            )
            .filter(Boolean),
        ),
      ];

      if (classeIds.length > 0) {
        const stagiaires =
          await this.stagiaireRepo.find({
            where: {
              idClasse: In(classeIds),
            },
          });

        recipientIds = stagiaires.map(
          (stagiaire) =>
            stagiaire.idUtilisateur,
        );
      }
    }

    /*
     * GENERAL ANNOUNCEMENT
     *
     * General announcements are sent to all users.
     */
    else {
      const utilisateurs =
        await this.utilisateurRepo.find();

      recipientIds = utilisateurs.map(
        (utilisateur) =>
          utilisateur.idUtilisateur,
      );
    }

    /*
     * Remove duplicates before sending.
     */
    recipientIds = [
      ...new Set(recipientIds),
    ];

    if (recipientIds.length > 0) {
      await this.notifications.notifyUsers(
        recipientIds,
        `annonce_${annonce.typeAnnonce}`,
        annonce.titre,
      );
    }
  }

  // ============================================================
  // DELETE /announcements/:id
  // ============================================================

  async remove(
    id: string,
    userId: string,
    isAdmin: boolean,
  ) {
    const annonce =
      await this.annonceRepo.findOne({
        where: {
          idAnnonce: id,
        },
        relations: ['auteur'],
      });

    if (!annonce) {
      throw new NotFoundException(
        'Annonce introuvable',
      );
    }

    if (
      !isAdmin &&
      annonce.auteur.idUtilisateur !== userId
    ) {
      throw new ForbiddenException(
        "Seul l'auteur ou un administrateur peut retirer cette annonce",
      );
    }

    await this.annonceRepo.delete({
      idAnnonce: id,
    });

    return {
      success: true,
    };
  }
}
function yearStart(year: number): Date {
  // Academic year resets every August: year means the calendar year in which August starts.
  return new Date(year, 7, 1, 0, 0, 0, 0);
}
