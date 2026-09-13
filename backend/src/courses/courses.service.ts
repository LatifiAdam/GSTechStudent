// src/courses/courses.service.ts

import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';

import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

import { Cours } from '../entities/cours.entity';
import { Affectation } from '../entities/affectation.entity';
import { Directeur } from '../entities/directeur.entity';
import { Etablissement } from '../entities/etablissement.entity';
import { Role } from '../common/enums/roles.enum';
import { ForbiddenException } from '@nestjs/common';

import { CreateCourseDto } from './dto/create-course.dto';
import { UpdateCourseDto } from './dto/update-course.dto';

@Injectable()
export class CoursesService {
  constructor(
    @InjectRepository(Cours)
    private readonly coursRepo: Repository<Cours>,

    @InjectRepository(Affectation)
    private readonly affectationRepo: Repository<Affectation>,
    @InjectRepository(Directeur)
    private readonly directeurRepo: Repository<Directeur>,

    @InjectRepository(Etablissement)
    private readonly etablissementRepo: Repository<Etablissement>,
  ) {}

  async findAll(formateurId?: string, actorId?: string, actorRole?: Role) {
    const qb = this.coursRepo
      .createQueryBuilder('cours')
      .leftJoinAndSelect(
        'cours.affectations',
        'affectation',
      )
      .leftJoinAndSelect(
        'affectation.classe',
        'classe',
      )
      .leftJoinAndSelect(
        'affectation.formateur',
        'formateur',
      )
      .leftJoinAndSelect(
        'formateur.utilisateur',
        'utilisateur',
      )
      .leftJoinAndSelect(
        'affectation.creneaux',
        'creneau');

    if (actorRole === Role.DIRECTEUR && actorId) {
      const etablissement = await this.etablissementRepo.findOne({ where: { idDirecteur: actorId } });
      if (etablissement?.idEtablissement) qb.andWhere('cours.id_etablissement = :actorEfp', { actorEfp: etablissement.idEtablissement });
    }
    if (formateurId) {
      qb.andWhere(
        'affectation.id_formateur = :formateurId',
        {
          formateurId,
        },
      );
    }

    const courses = await qb
      .orderBy(
        'cours.nom_cours',
        'ASC',
      )
      .getMany();

    return courses.map((cours) => ({
      idCours: cours.idCours,
      nomCours: cours.nomCours,
      dateCreation: cours.dateCreation,

      affectations:
        (cours.affectations ?? []).map(
          (affectation) => ({
            idAffectation:
              affectation.idAffectation,

            idClasse:
              affectation.idClasse,

            nomClasse:
              affectation.classe
                ?.nomClasse ?? null,

            idFormateur:
              affectation.idFormateur,

            formateurNom:
              affectation.formateur
                ?.utilisateur
                ? `${affectation.formateur.utilisateur.prenom} ${affectation.formateur.utilisateur.nom}`
                : null,

            creneaux:
              (affectation.creneaux ?? []).map(
                (creneau) => ({
                  idCreneau:
                    creneau.idCreneau,

                  jourSemaine:
                    creneau.jourSemaine,

                  heureDebut:
                    creneau.heureDebut,

                  heureFin:
                    creneau.heureFin,

                  salle:
                    creneau.salle,

                  dateDebut:
                    creneau.dateDebut,

                  dateFin:
                    creneau.dateFin,
                }),
              ),
          }),
        ),
    }));
  }

  async findOne(id: string, actorId?: string, actorRole?: Role) {
    const cours =
      await this.coursRepo.findOne({
        where: {
          idCours: id,
        },

        relations: [
          'affectations',
          'affectations.classe',
          'affectations.formateur',
          'affectations.formateur.utilisateur',
          'affectations.creneaux',
        ],
      });

    if (!cours) { throw new NotFoundException('Cours introuvable'); }
    if (actorRole === Role.DIRECTEUR && actorId) { const etablissement=await this.etablissementRepo.findOne({where:{idDirecteur:actorId}}); if(cours.idEtablissement !== etablissement?.idEtablissement) throw new ForbiddenException('Cours hors de votre établissement'); }

    return {
      idCours: cours.idCours,
      nomCours: cours.nomCours,
      dateCreation: cours.dateCreation,

      affectations:
        (cours.affectations ?? []).map(
          (affectation) => ({
            idAffectation:
              affectation.idAffectation,

            idClasse:
              affectation.idClasse,

            nomClasse:
              affectation.classe
                ?.nomClasse ?? null,

            idFormateur:
              affectation.idFormateur,

            formateurNom:
              affectation.formateur
                ?.utilisateur
                ? `${affectation.formateur.utilisateur.prenom} ${affectation.formateur.utilisateur.nom}`
                : null,

            creneaux:
              (affectation.creneaux ?? []).map(
                (creneau) => ({
                  idCreneau:
                    creneau.idCreneau,

                  jourSemaine:
                    creneau.jourSemaine,

                  heureDebut:
                    creneau.heureDebut,

                  heureFin:
                    creneau.heureFin,

                  salle:
                    creneau.salle,

                  dateDebut:
                    creneau.dateDebut,

                  dateFin:
                    creneau.dateFin,
                }),
              ),
          }),
        ),
    };
  }

  async create(dto: CreateCourseDto, actorId?: string, actorRole?: Role) {
    if (
      !dto.nomCours ||
      !dto.nomCours.trim()
    ) {
      throw new BadRequestException(
        'Le nom du cours est obligatoire',
      );
    }

    let idEtablissement: string | null = null;
    if (actorRole === Role.DIRECTEUR && actorId) {
      const etablissement = await this.etablissementRepo.findOne({
        where: { idDirecteur: actorId },
      });
      idEtablissement = etablissement?.idEtablissement ?? null;
      if (!idEtablissement) {
        throw new ForbiddenException('Directeur sans établissement');
      }
    }
    const cours = this.coursRepo.create({ nomCours: dto.nomCours.trim(), idEtablissement });

    return this.coursRepo.save(cours);
  }

  async update(id: string, dto: UpdateCourseDto, actorId?: string, actorRole?: Role) {
    const cours =
      await this.coursRepo.findOne({
        where: {
          idCours: id,
        },
      });

    if (!cours) {
      throw new NotFoundException(
        'Cours introuvable',
      );
    }

    if (
      dto.nomCours !== undefined
    ) {
      if (
        !dto.nomCours.trim()
      ) {
        throw new BadRequestException(
          'Le nom du cours ne peut pas être vide',
        );
      }

      cours.nomCours =
        dto.nomCours.trim();
    }

    return this.coursRepo.save(
      cours,
    );
  }

  async remove(id: string, actorId?: string, actorRole?: Role) {
    const cours=await this.coursRepo.findOne({where:{idCours:id}});
    if(!cours) throw new NotFoundException('Cours introuvable');
    if(actorRole===Role.DIRECTEUR&&actorId){const etablissement=await this.etablissementRepo.findOne({where:{idDirecteur:actorId}});if(cours.idEtablissement!==etablissement?.idEtablissement)throw new ForbiddenException('Cours hors de votre établissement');}
    await this.coursRepo.delete(id);
    return {success:true};
  }

  async getStudentCount(idCours: string, actorId?: string, actorRole?: Role) {
    const cours =
      await this.coursRepo.findOne({
        where: {
          idCours,
        },
      });

    if (!cours) throw new NotFoundException('Cours introuvable');
    if (actorRole === Role.DIRECTEUR && actorId) { const etablissement=await this.etablissementRepo.findOne({where:{idDirecteur:actorId}}); if(cours.idEtablissement !== etablissement?.idEtablissement) throw new ForbiddenException('Cours hors de votre établissement'); }

    const rows =
      await this.affectationRepo
        .createQueryBuilder(
          'affectation',
        )
        .leftJoin(
          'affectation.classe',
          'classe',
        )
        .leftJoin(
          'classe.etudiants',
          'etudiant',
        )
        .where(
          'affectation.id_cours = :idCours',
          {
            idCours,
          },
        )
        .select(
          'affectation.id_affectation',
          'idAffectation',
        )
        .addSelect(
          'affectation.id_classe',
          'idClasse',
        )
        .addSelect(
          'classe.nom_classe',
          'nomClasse',
        )
        .addSelect(
          'COUNT(etudiant.id_utilisateur)',
          'totalStagieres',
        )
        .groupBy(
          'affectation.id_affectation',
        )
        .addGroupBy(
          'affectation.id_classe',
        )
        .addGroupBy(
          'classe.nom_classe',
        )
        .getRawMany();

    const totalStagieres =
      rows.reduce(
        (
          total,
          row,
        ) =>
          total +
          Number(
            row.totalStagieres,
          ),
        0,
      );

    return {
      idCours:
        cours.idCours,

      nomCours:
        cours.nomCours,

      totalStagieres,

      classes:
        rows.map(
          (row) => ({
            idAffectation:
              row.idAffectation,

            idClasse:
              row.idClasse,

            nomClasse:
              row.nomClasse,

            totalStagieres:
              Number(
                row.totalStagieres,
              ),
          }),
        ),
    };
  }

  async getAffectations(idCours: string, actorId?: string, actorRole?: Role) {
    const cours =
      await this.coursRepo.findOne({
        where: {
          idCours,
        },
      });

    if (!cours) {
      throw new NotFoundException(
        'Cours introuvable',
      );
    }
    if (actorRole === Role.DIRECTEUR && actorId) { const etablissement=await this.etablissementRepo.findOne({where:{idDirecteur:actorId}}); if(cours.idEtablissement !== etablissement?.idEtablissement) throw new ForbiddenException('Cours hors de votre établissement'); }

    const rows = await this.affectationRepo.find({
      where: { idCours },
      relations: ['classe', 'formateur', 'formateur.utilisateur', 'creneaux'],
    });
    return rows.map((a) => ({
      ...a,
      idCours: a.idCours,
      idcours: a.idCours,
    }));
  }
}