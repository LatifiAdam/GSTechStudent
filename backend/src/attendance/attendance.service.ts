import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';

import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

import { Appel } from '../entities/appel.entity';
import {
  Presence,
  StatutPresence,
} from '../entities/presence.entity';
import { Creneau } from '../entities/creneau.entity';
import { Affectation } from '../entities/affectation.entity';
import { stagiaire } from '../entities/stagiaire.entity';

import { OpenCallDto } from './dto/open-call.dto';
import { UpdateRecordsDto } from './dto/update-records.dto';

import { NotificationsService } from '../notifications/notifications.service';
import { StatutJustification } from '../entities/justification.entity';
import { Role } from '../common/enums/roles.enum';

@Injectable()
export class AttendanceService {
  constructor(
    @InjectRepository(Appel)
    private readonly appelRepo: Repository<Appel>,

    @InjectRepository(Presence)
    private readonly presenceRepo: Repository<Presence>,

    @InjectRepository(Creneau)
    private readonly creneauRepo: Repository<Creneau>,

    @InjectRepository(Affectation)
    private readonly affectationRepo: Repository<Affectation>,

    @InjectRepository(stagiaire)
    private readonly stagiaireRepo: Repository<stagiaire>,

    private readonly notifications: NotificationsService,
  ) {}

  // ============================================================
  // POST /attendance/calls
  // ============================================================
  //
  // Ouvre un appel pour un créneau précis.
  //
  // Structure :
  //
  // appel
  //   ↓
  // creneau
  //   ↓
  // affectation
  //   ↓
  // classe
  //   ↓
  // étudiants
  //
  // Tous les étudiants de la classe commencent PRESENT.
  // ============================================================

  async openCall(
    dto: OpenCallDto,
    formateurId: string,
  ) {
    // ----------------------------------------------------------
    // 1. Récupérer le créneau
    // ----------------------------------------------------------

    const creneau =
      await this.creneauRepo.findOne({
        where: {
          idCreneau: dto.idCreneau,
        },
      });

    if (!creneau) {
      throw new NotFoundException(
        'Créneau introuvable',
      );
    }

    // ----------------------------------------------------------
    // 2. Récupérer l'affectation
    // ----------------------------------------------------------

    const affectation =
      await this.affectationRepo.findOne({
        where: {
          idAffectation:
            creneau.idAffectation,
        },
      });

    if (!affectation) {
      throw new NotFoundException(
        'Affectation introuvable',
      );
    }

    // ----------------------------------------------------------
    // 3. Vérifier que le professeur enseigne
    //    bien ce créneau
    // ----------------------------------------------------------

    if (
      affectation.idFormateur !==
      formateurId
    ) {
      throw new ForbiddenException(
        "Vous n'êtes pas responsable de ce créneau",
      );
    }

    // ----------------------------------------------------------
    // 4. Vérifier qu'un appel n'existe pas
    //    déjà pour ce créneau et cette date
    // ----------------------------------------------------------

    const dateHeure =
      new Date(dto.dateHeure);

    const existingCall =
      await this.appelRepo
        .createQueryBuilder('appel')
        .where(
          'appel.id_creneau = :idCreneau',
          {
            idCreneau: dto.idCreneau,
          },
        )
        .andWhere(
          'DATE(appel.date_heure) = DATE(:dateHeure)',
          {
            dateHeure,
          },
        )
        .getOne();

    if (existingCall) {
      // Opening attendance is intentionally idempotent for a given
      // schedule slot and calendar date. The mobile formateur screen can
      // be reopened after a retry/navigation event without creating a
      // duplicate call. Return the existing call instead of turning a
      // normal reopen into a 400 error.
      return this.findCall(existingCall.idAppel);
    }

    // ----------------------------------------------------------
    // 5. Créer l'appel
    // ----------------------------------------------------------

    const appel =
      this.appelRepo.create({
        idCreneau: dto.idCreneau,
        idFormateur: formateurId,
        dateHeure,
        valide: false,
      });

    const savedAppel =
      await this.appelRepo.save(appel);

    // ----------------------------------------------------------
    // 6. Récupérer les étudiants de la classe
    // ----------------------------------------------------------

    const stagiaires =
      await this.stagiaireRepo.find({
        where: {
          idClasse:
            affectation.idClasse,
        },
      });

    // ----------------------------------------------------------
    // 7. Créer une présence pour chaque étudiant
    // ----------------------------------------------------------

    const presences =
      stagiaires.map((stagiaire) =>
        this.presenceRepo.create({
          idAppel:
            savedAppel.idAppel,

          idstagiaire:
            stagiaire.idUtilisateur,

          statut:
            StatutPresence.PRESENT,
        }),
      );

    if (presences.length > 0) {
      await this.presenceRepo.save(
        presences,
      );
    }

    // ----------------------------------------------------------
    // 8. Retourner l'appel
    // ----------------------------------------------------------

    return {
      ...savedAppel,

      nbstagiaires:
        presences.length,
    };
  }

  // ============================================================
  // PATCH /attendance/calls/:id/records
  // ============================================================

  async updateRecords(
    id: string,
    dto: UpdateRecordsDto,
    formateurId: string,
  ) {
    // ----------------------------------------------------------
    // Récupérer l'appel
    // ----------------------------------------------------------

    const appel =
      await this.appelRepo.findOne({
        where: {
          idAppel: id,
        },
      });

    if (!appel) {
      throw new NotFoundException(
        'Appel introuvable',
      );
    }

    // ----------------------------------------------------------
    // Vérifier le professeur
    // ----------------------------------------------------------

    if (
      appel.idFormateur !==
      formateurId
    ) {
      throw new ForbiddenException(
        "Seul l'formateur responsable de l'appel peut le modifier",
      );
    }

    // ----------------------------------------------------------
    // Vérifier si l'appel est verrouillé
    // ----------------------------------------------------------

    if (appel.valide) {
      throw new BadRequestException(
        'Cet appel est déjà validé et verrouillé',
      );
    }

    // ----------------------------------------------------------
    // Modifier les présences
    // ----------------------------------------------------------

    for (const record of dto.records) {
      const presence =
        await this.presenceRepo.findOne({
          where: {
            idAppel: id,
            idstagiaire:
              record.idstagiaire,
          },
        });

      if (!presence) {
        throw new NotFoundException(
          `Aucune présence trouvée pour l'étudiant ${record.idstagiaire}`,
        );
      }

      presence.statut =
        record.statut;

      await this.presenceRepo.save(
        presence,
      );

      // --------------------------------------------------------
      // Vérification du seuil d'absence
      // --------------------------------------------------------

      if (
        record.statut ===
        StatutPresence.ABSENT
      ) {
        await this.checkAbsenceThreshold(
          record.idstagiaire,
        );
      }
    }

    return this.findCall(id);
  }

  // ============================================================
  // POST /attendance/calls/:id/validate
  // ============================================================

  async validateCall(
    id: string,
    formateurId: string,
  ) {
    const appel =
      await this.appelRepo.findOne({
        where: {
          idAppel: id,
        },
      });

    if (!appel) {
      throw new NotFoundException(
        'Appel introuvable',
      );
    }

    if (
      appel.idFormateur !==
      formateurId
    ) {
      throw new ForbiddenException(
        "Seul l'formateur responsable de l'appel peut le valider",
      );
    }

    if (appel.valide) {
      throw new BadRequestException(
        'Cet appel est déjà validé',
      );
    }

    appel.valide = true;

    return this.appelRepo.save(appel);
  }

  // ============================================================
  // GET /attendance/calls/:id
  // ============================================================

  async findCall(id: string) {
    const appel =
      await this.appelRepo.findOne({
        where: {
          idAppel: id,
        },
        relations: [
          'creneau',
          'creneau.affectation',
          'creneau.affectation.cours',
          'creneau.affectation.classe',
          'formateur',
        ],
      });

    if (!appel) {
      throw new NotFoundException(
        'Appel introuvable',
      );
    }

    const presences =
      await this.presenceRepo.find({
        where: {
          idAppel: id,
        },
        relations: [
          'stagiaire',
        ],
      });

    return {
      ...appel,
      presences,
    };
  }

  // ============================================================
  // GET /attendance/stagieres/:id/history
  // ============================================================

  async studentHistory(id: string, requesterId: string, requesterRole: Role) {
    if (requesterRole === Role.stagiaire && id !== requesterId) {
      throw new ForbiddenException('Vous ne pouvez consulter que votre propre historique');
    }

    if (requesterRole === Role.FORMATEUR) {
      const student = await this.stagiaireRepo.findOne({ where: { idUtilisateur: id } });
      if (!student) throw new NotFoundException('Étudiant introuvable');
      const allowed = await this.affectationRepo.exist({
        where: { idClasse: student.idClasse ?? '', idFormateur: requesterId },
      });
      if (!allowed) throw new ForbiddenException('Vous ne pouvez consulter que les étudiants de vos classes');
    }

    return this.presenceRepo.find({
      where: {
        idstagiaire: id,
      },

      relations: [
        'appel',
        'appel.creneau',
        'appel.creneau.affectation',
        'appel.creneau.affectation.cours',
        'appel.creneau.affectation.classe',
      ],

      order: {
        idPresence: 'DESC',
      },
    });
  }

  // ============================================================
  // RG5
  // ============================================================
  //
  // Vérifie le nombre d'absences non justifiées.
  //
  // IMPORTANT :
  // Le compteur est global à l'étudiant.
  //
  // Peu importe le cours :
  //
  // Réseaux : 2 absences
  // Linux   : 1 absence
  // SQL     : 1 absence
  //
  // Total = 4 absences.
  // ============================================================

  private async checkAbsenceThreshold(
    stagiaireId: string,
  ) {
    const threshold =
      Number(
        process.env
          .ABSENCE_ALERT_THRESHOLD,
      ) || 3;

    const count =
      await this.presenceRepo
        .createQueryBuilder('p')

        .leftJoin(
          'p.justification',
          'j',
        )

        .where(
          'p.id_stagiaire = :stagiaireId',
          {
            stagiaireId,
          },
        )

        .andWhere(
          'p.statut = :statut',
          {
            statut:
              StatutPresence.ABSENT,
          },
        )

        .andWhere(
          `
          (
            j.id_justification IS NULL
            OR
            j.statut_justification != :accepted
          )
          `,
          {
            accepted:
              StatutJustification.ACCEPTEE,
          },
        )

        .getCount();

    if (count >= threshold) {
      await this.notifications.notifyUsers(
        [stagiaireId],

        'absences_repetees',

        `Vous avez atteint ${count} absence(s) non justifiée(s). Merci de régulariser votre situation.`,
      );
    }
  }
}