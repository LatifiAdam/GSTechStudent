import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { Classe } from '../entities/classe.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Affectation } from '../entities/affectation.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Directeur } from '../entities/directeur.entity';
import { Role } from '../common/enums/roles.enum';
import { CreateClasseDto } from './dto/create-classe.dto';
import { UpdateClasseDto } from './dto/update-classe.dto';

@Injectable()
export class ClassesService {
  constructor(
    @InjectRepository(Gestionnaire)
    private readonly gestionnaireRepo: Repository<Gestionnaire>,
    @InjectRepository(Directeur)
    private readonly directeurRepo: Repository<Directeur>,
    @InjectRepository(Classe)
    private readonly classeRepo: Repository<Classe>,
    @InjectRepository(stagiaire)
    private readonly stagiaireRepo: Repository<stagiaire>,
    @InjectRepository(Affectation)
    private readonly affectationRepo: Repository<Affectation>,
  ) {}

  async formateurClasses(formateurId: string) {
    const affectations = await this.affectationRepo.find({
      where: { idFormateur: formateurId },
      relations: ['classe', 'cours'],
    });
    const ids = [...new Set(affectations.map((a) => a.idClasse))];
    if (!ids.length) return [];
    return this.classeRepo.find({
      where: { idClasse: In(ids) },
      relations: [
        'stagiaires',
        'stagiaires.utilisateur',
        'affectations',
        'affectations.cours',
        'affectations.formateur',
        'affectations.formateur.utilisateur',
      ],
    });
  }

  async findAll(actorId?: string, actorRole?: Role) {
    const where: any = {};

    if (actorRole === Role.DIRECTEUR && actorId) {
      const directeur = await this.directeurRepo.findOne({
        where: { idUtilisateur: actorId },
        relations: ['etablissement'],
      });
      if (directeur?.etablissement?.idEtablissement) {
        where.idEtablissement = directeur.etablissement.idEtablissement;
      }
    }

    if (actorRole === Role.GESTIONNAIRE && actorId) {
      const gestionnaire = await this.gestionnaireRepo.findOne({
        where: { idUtilisateur: actorId },
      });
      if (gestionnaire?.idEtablissement) {
        where.idEtablissement = gestionnaire.idEtablissement;
      }
    }

    const classes = await this.classeRepo.find({
      where,
      relations: [
        'stagiaires',
        'stagiaires.utilisateur',
        'affectations',
        'affectations.cours',
        'affectations.formateur',
        'affectations.formateur.utilisateur',
      ],
    });

    // Always return the current stagiaire relation so clients can refresh counts
    // immediately after an assignment without relying on stale cached state.
    return classes.map((classe) => ({
      ...classe,
      stagiaires: classe.stagiaires ?? [],
    }));
  }

  findAllLegacy() {
    return this.classeRepo.find({
      relations: [
        'stagiaires',
        'stagiaires.utilisateur',
        'affectations',
        'affectations.cours',
        'affectations.formateur',
        'affectations.formateur.utilisateur',
      ],
    });
  }

  async findOne(id: string) {
    const classe = await this.classeRepo.findOne({
      where: { idClasse: id },
      relations: [
        'stagiaires',
        'stagiaires.utilisateur',
        'affectations',
        'affectations.cours',
        'affectations.formateur',
        'affectations.formateur.utilisateur',
      ],
    });
    if (!classe) throw new NotFoundException('Classe introuvable');
    return classe;
  }

  async create(dto: CreateClasseDto, actorId?: string, actorRole?: Role) {
    let idEtablissement: string | null = null;

    if (actorRole === Role.GESTIONNAIRE && actorId) {
      const gestionnaire = await this.gestionnaireRepo.findOne({
        where: { idUtilisateur: actorId },
      });
      idEtablissement = gestionnaire?.idEtablissement ?? null;
      if (!idEtablissement) {
        throw new ForbiddenException('Gestionnaire sans établissement');
      }
    }

    if (actorRole === Role.DIRECTEUR && actorId) {
      const directeur = await this.directeurRepo.findOne({
        where: { idUtilisateur: actorId },
        relations: ['etablissement'],
      });
      idEtablissement = directeur?.etablissement?.idEtablissement ?? null;
      if (!idEtablissement) {
        throw new ForbiddenException('Directeur sans établissement');
      }
    }

    const existing = await this.classeRepo.findOne({
      where: { nomClasse: dto.nomClasse, idEtablissement },
    });
    if (existing) {
      throw new BadRequestException(
        'Une classe porte déjà ce nom dans cet établissement',
      );
    }

    return this.classeRepo.save(
      this.classeRepo.create({
        nomClasse: dto.nomClasse,
        description: dto.description ?? null,
        idEtablissement,
      }),
    );
  }

  async update(id: string, dto: UpdateClasseDto) {
    const classe = await this.classeRepo.findOne({ where: { idClasse: id } });
    if (!classe) throw new NotFoundException('Classe introuvable');

    if (
      dto.nomClasse &&
      dto.nomClasse !== classe.nomClasse &&
      (await this.classeRepo.findOne({
        where: { nomClasse: dto.nomClasse, idEtablissement: classe.idEtablissement },
      }))
    ) {
      throw new BadRequestException('Une classe porte déjà ce nom');
    }

    Object.assign(classe, dto);
    return this.classeRepo.save(classe);
  }

  async remove(id: string) {
    const result = await this.classeRepo.delete(id);
    if (!result.affected) throw new NotFoundException('Classe introuvable');
    return { success: true };
  }

  async stagieres(id: string, requesterId?: string) {
    const classe = await this.classeRepo.findOne({ where: { idClasse: id } });
    if (!classe) throw new NotFoundException('Classe introuvable');

    if (
      requesterId &&
      !(await this.affectationRepo.findOne({
        where: { idClasse: id, idFormateur: requesterId },
      }))
    ) {
      throw new ForbiddenException('Vous ne gérez pas cette classe');
    }

    return this.stagiaireRepo.find({
      where: { idClasse: id },
      relations: ['utilisateur'],
    });
  }

  /**
   * Seul le GS (Gestionnaire) peut affecter un stagiaire à un groupe.
   * Le Directeur gère les formateurs et les créneaux, pas les stagiaires.
   */
  async assignStudent(
    classId: string,
    studentId: string,
    requesterId?: string,
    requesterRole?: Role | string,
  ) {
    if (requesterRole !== Role.GESTIONNAIRE && requesterRole !== Role.DF) {
      throw new ForbiddenException(
        'Seul le Gestionnaire peut affecter les stagiaires aux groupes',
      );
    }

    const classe = await this.classeRepo.findOne({ where: { idClasse: classId } });
    if (!classe) throw new NotFoundException('Classe introuvable');

    const student = await this.stagiaireRepo.findOne({
      where: { idUtilisateur: studentId },
    });
    if (!student) throw new NotFoundException('Stagiaire introuvable');

    if (requesterRole === Role.GESTIONNAIRE && requesterId) {
      const gestionnaire = await this.gestionnaireRepo.findOne({
        where: { idUtilisateur: requesterId },
      });
      if (
        !gestionnaire?.idEtablissement ||
        gestionnaire.idEtablissement !== classe.idEtablissement
      ) {
        throw new ForbiddenException('Classe hors de votre établissement');
      }
      student.idEtablissement = gestionnaire.idEtablissement;
    }

    if (
      student.idEtablissement &&
      classe.idEtablissement &&
      student.idEtablissement !== classe.idEtablissement
    ) {
      throw new ForbiddenException('Stagiaire hors de l’établissement');
    }

    student.idClasse = classId;
    return this.stagiaireRepo.save(student);
  }

  async removeStudent(classId: string, studentId: string) {
    const student = await this.stagiaireRepo.findOne({
      where: { idUtilisateur: studentId, idClasse: classId },
    });
    if (!student) {
      throw new NotFoundException('Stagiaire non trouvé dans cette classe');
    }
    student.idClasse = null;
    return this.stagiaireRepo.save(student);
  }

  async assignments(id: string) {
    const classe = await this.classeRepo.findOne({ where: { idClasse: id } });
    if (!classe) throw new NotFoundException('Classe introuvable');

    return this.affectationRepo.find({
      where: { idClasse: id },
      relations: ['cours', 'formateur', 'formateur.utilisateur', 'creneaux'],
    });
  }
}
