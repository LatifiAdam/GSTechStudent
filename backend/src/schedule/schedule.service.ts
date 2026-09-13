import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';

import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

import { Creneau } from '../entities/creneau.entity';
import { Affectation } from '../entities/affectation.entity';

import { CreateCreneauDto } from './dto/create-creneau.dto';
import { UpdateCreneauDto } from './dto/update-creneau.dto';
import { Directeur } from '../entities/directeur.entity';
import { Role } from '../common/enums/roles.enum';
import { ForbiddenException } from '@nestjs/common';

@Injectable()
export class ScheduleService {
  constructor(
    @InjectRepository(Creneau)
    private readonly creneauRepo: Repository<Creneau>,

    @InjectRepository(Affectation)
    private readonly affectationRepo: Repository<Affectation>,
    @InjectRepository(Directeur) private readonly directeurRepo: Repository<Directeur>,
  ) {}

  // ============================================================
  // GET /schedule
  // ============================================================

  async findAll(
    coursId?: string,
    etudiantId?: string,
    formateurId?: string,
    classeId?: string,
  ) {
    const qb = this.creneauRepo
      .createQueryBuilder('creneau')
      .innerJoinAndSelect(
        'creneau.affectation',
        'affectation',
      )
      .innerJoinAndSelect(
        'affectation.cours',
        'cours',
      )
      .innerJoinAndSelect(
        'affectation.classe',
        'classe',
      )
      .innerJoinAndSelect(
        'affectation.formateur',
        'formateur',
      );

    // ----------------------------------------------------------
    // Course
    // ----------------------------------------------------------

    if (coursId) {
      qb.andWhere(
        'affectation.id_cours = :coursId',
        { coursId },
      );
    }

    // ----------------------------------------------------------
    // Formateur
    // ----------------------------------------------------------

    if (formateurId) {
      qb.andWhere(
        'affectation.id_formateur = :formateurId',
        { formateurId },
      );
    }

    // ----------------------------------------------------------
    // Class
    // ----------------------------------------------------------

    if (classeId) {
      qb.andWhere(
        'affectation.id_classe = :classeId',
        { classeId },
      );
    }

    // ----------------------------------------------------------
    // Student
    //
    // Student sees the schedule of his class.
    // ----------------------------------------------------------

    if (etudiantId) {
      qb.innerJoin(
        'etudiant',
        'etudiant',
        'etudiant.id_classe = affectation.id_classe',
      );

      qb.andWhere(
        'etudiant.id_utilisateur = :etudiantId',
        { etudiantId },
      );
    }

    return qb
      .orderBy(
        'creneau.jour_semaine',
        'ASC',
      )
      .addOrderBy(
        'creneau.heure_debut',
        'ASC',
      )
      .getMany();
  }

  // ============================================================
  // POST /schedule
  // ============================================================

  async create(dto: CreateCreneauDto, actorId?:string, actorRole?:Role) {
    // ----------------------------------------------------------
    // Verify affectation
    // ----------------------------------------------------------

    const affectation =
      await this.affectationRepo.findOne({
        where: {
          idAffectation: dto.idAffectation,
        },
      });

    if (!affectation) {
      throw new NotFoundException(
        'Affectation introuvable',
      );
    }
    if(actorRole===Role.DIRECTEUR&&actorId){const d=await this.directeurRepo.findOne({where:{idUtilisateur:actorId},relations:['etablissement']});const a=await this.affectationRepo.findOne({where:{idAffectation:dto.idAffectation},relations:['classe']});if(a?.classe?.idEtablissement!==d?.etablissement?.idEtablissement)throw new ForbiddenException('Créneau hors de votre établissement');}

    // ----------------------------------------------------------
    // Validate time
    // ----------------------------------------------------------

    if (dto.heureFin <= dto.heureDebut) {
      throw new BadRequestException(
        "L'heure de fin doit être supérieure à l'heure de début",
      );
    }

    // ----------------------------------------------------------
    // Check overlapping schedule
    //
    // A formateur cannot teach two courses at the same time.
    //
    // A class cannot have two courses at the same time.
    // ----------------------------------------------------------

    const overlap =
      await this.creneauRepo
        .createQueryBuilder('creneau')
        .innerJoin(
          'creneau.affectation',
          'existingAffectation',
        )
        .where(
          `
          (
            existingAffectation.id_formateur = :formateurId
            OR
            existingAffectation.id_classe = :classeId
          )
          `,
          {
            formateurId:
              affectation.idFormateur,

            classeId:
              affectation.idClasse,
          },
        )
        .andWhere(
          'creneau.jour_semaine = :jour',
          {
            jour: dto.jourSemaine,
          },
        )
        .andWhere(
          `
          creneau.heure_debut < :heureFin
          AND
          creneau.heure_fin > :heureDebut
          `,
          {
            heureDebut:
              dto.heureDebut,

            heureFin:
              dto.heureFin,
          },
        )
        .getOne();

    if (overlap) {
      throw new BadRequestException(
        'Ce créneau entre en conflit avec un autre cours pour cet formateur ou cette classe',
      );
    }

    // ----------------------------------------------------------
    // Create slot
    // ----------------------------------------------------------

    const creneau = this.creneauRepo.create({
      jourSemaine: dto.jourSemaine,

      heureDebut: dto.heureDebut,

      heureFin: dto.heureFin,

      salle: dto.salle,

      idAffectation: dto.idAffectation,

      dateDebut:
        dto.dateDebut ?? null,

      dateFin:
        dto.dateFin ?? null,
    });

    return this.creneauRepo.save(creneau);
  }

  // ============================================================
  // PATCH /schedule/:id
  // ============================================================

  async update(id: string, dto: UpdateCreneauDto, actorId?:string, actorRole?:Role) {
    const creneau =
      await this.creneauRepo.findOne({
        where: {
          idCreneau: id,
        },
      });
    if(actorRole===Role.DIRECTEUR&&actorId&&creneau){const d=await this.directeurRepo.findOne({where:{idUtilisateur:actorId},relations:['etablissement']});const a=await this.affectationRepo.findOne({where:{idAffectation:creneau.idAffectation},relations:['classe']});if(a?.classe?.idEtablissement!==d?.etablissement?.idEtablissement)throw new ForbiddenException('Créneau hors de votre établissement');}

    if (!creneau) {
      throw new NotFoundException(
        'Créneau introuvable',
      );
    }
    if(actorRole===Role.DIRECTEUR&&actorId){const d=await this.directeurRepo.findOne({where:{idUtilisateur:actorId},relations:['etablissement']});const a=await this.affectationRepo.findOne({where:{idAffectation:creneau.idAffectation},relations:['classe']});if(a?.classe?.idEtablissement!==d?.etablissement?.idEtablissement)throw new ForbiddenException('Créneau hors de votre établissement');}

    // ----------------------------------------------------------
    // Update basic fields
    // ----------------------------------------------------------

    if (dto.jourSemaine !== undefined) {
      creneau.jourSemaine =
        dto.jourSemaine;
    }

    if (dto.heureDebut !== undefined) {
      creneau.heureDebut =
        dto.heureDebut;
    }

    if (dto.heureFin !== undefined) {
      creneau.heureFin =
        dto.heureFin;
    }

    if (dto.salle !== undefined) {
      creneau.salle =
        dto.salle;
    }

    if (dto.dateDebut !== undefined) {
      creneau.dateDebut =
        dto.dateDebut ?? null;
    }

    if (dto.dateFin !== undefined) {
      creneau.dateFin =
        dto.dateFin ?? null;
    }

    // ----------------------------------------------------------
    // Change affectation
    // ----------------------------------------------------------

    if (dto.idAffectation) {
      const affectation =
        await this.affectationRepo.findOne({
          where: {
            idAffectation:
              dto.idAffectation,
          },
        });

      if (!affectation) {
        throw new NotFoundException(
          'Affectation introuvable',
        );
      }

      creneau.idAffectation =
        dto.idAffectation;
    }

    // ----------------------------------------------------------
    // Validate time
    // ----------------------------------------------------------

    if (
      creneau.heureFin <=
      creneau.heureDebut
    ) {
      throw new BadRequestException(
        "L'heure de fin doit être supérieure à l'heure de début",
      );
    }

    return this.creneauRepo.save(
      creneau,
    );
  }

  // ============================================================
  // DELETE /schedule/:id
  // ============================================================

  async remove(id: string, actorId?:string, actorRole?:Role) {
    const result =
      await this.creneauRepo.delete({
        idCreneau: id,
      });

    if (result.affected === 0) {
      throw new NotFoundException(
        'Créneau introuvable',
      );
    }

    return {
      success: true,
    };
  }
}