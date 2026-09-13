import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Affectation } from '../entities/affectation.entity';
import { Classe } from '../entities/classe.entity';
import { Cours } from '../entities/cours.entity';
import { Formateur } from '../entities/formateur.entity';
import { Directeur } from '../entities/directeur.entity';
import { Role } from '../common/enums/roles.enum';
import { CreateAffectationDto } from './dto/create-affectation.dto';
import { UpdateAffectationDto } from './dto/update-affectation.dto';

type AffectationInput = Partial<Pick<CreateAffectationDto, 'idClasse' | 'idCours' | 'idFormateur'>> &
  Partial<Pick<CreateAffectationDto, 'idclasse' | 'idcours' | 'idformateur'>>;

function normalizeAffectation(dto: AffectationInput) {
  return {
    idClasse: dto.idClasse ?? dto.idclasse,
    idCours: dto.idCours ?? dto.idcours,
    idFormateur: dto.idFormateur ?? dto.idformateur,
  };
}

@Injectable()
export class AffectationsService {
  constructor(
    @InjectRepository(Affectation) private readonly repo: Repository<Affectation>,
    @InjectRepository(Classe) private readonly classes: Repository<Classe>,
    @InjectRepository(Cours) private readonly courses: Repository<Cours>,
    @InjectRepository(Formateur) private readonly formateurs: Repository<Formateur>,
    @InjectRepository(Directeur) private readonly directeurs: Repository<Directeur>,
  ) {}

  async findAll(actorId?: string, actorRole?: Role) {
    if (actorRole === Role.DIRECTEUR && actorId) {
      const directeur = await this.directeurs.findOne({
        where: { idUtilisateur: actorId },
        relations: ['etablissement'],
      });
      const efp = directeur?.etablissement?.idEtablissement;
      if (efp) {
        return this.repo
          .createQueryBuilder('a')
          .leftJoinAndSelect('a.classe', 'classe')
          .leftJoinAndSelect('a.cours', 'cours')
          .leftJoinAndSelect('a.formateur', 'formateur')
          .leftJoinAndSelect('formateur.utilisateur', 'utilisateur')
          .leftJoinAndSelect('a.creneaux', 'creneaux')
          .where('classe.id_etablissement = :efp', { efp })
          .andWhere('cours.id_etablissement = :efp', { efp })
          .andWhere('formateur.id_etablissement = :efp', { efp })
          .getMany();
      }
    }

    return this.repo.find({
      relations: ['classe', 'cours', 'formateur', 'formateur.utilisateur', 'creneaux'],
    });
  }

  async findOne(id: string, actorId?: string, actorRole?: Role) {
    const affectation = await this.repo.findOne({
      where: { idAffectation: id },
      relations: ['classe', 'cours', 'formateur', 'formateur.utilisateur', 'creneaux'],
    });
    if (!affectation) throw new NotFoundException('Affectation introuvable');

    if (actorRole === Role.DIRECTEUR && actorId) {
      const directeur = await this.directeurs.findOne({
        where: { idUtilisateur: actorId },
        relations: ['etablissement'],
      });
      if (
        affectation.classe?.idEtablissement !==
        directeur?.etablissement?.idEtablissement
      ) {
        throw new ForbiddenException('Affectation hors de votre établissement');
      }
    }
    return affectation;
  }

  private async validate(
    dto: CreateAffectationDto,
    currentId?: string,
    actorId?: string,
    actorRole?: Role,
  ) {
    const normalized = normalizeAffectation(dto);
    if (!normalized.idClasse || !normalized.idCours || !normalized.idFormateur) {
      throw new BadRequestException('idClasse, idCours et idFormateur sont requis');
    }

    const classe = await this.classes.findOne({ where: { idClasse: normalized.idClasse } });
    const cours = await this.courses.findOne({ where: { idCours: normalized.idCours } });
    const formateur = await this.formateurs.findOne({ where: { idUtilisateur: normalized.idFormateur } });

    if (!classe) throw new NotFoundException('Classe introuvable');
    if (!cours) throw new NotFoundException('Cours introuvable');
    if (!formateur) {
      throw new BadRequestException("L'utilisateur sélectionné n'est pas un formateur");
    }

    if (actorRole === Role.DIRECTEUR && actorId) {
      const directeur = await this.directeurs.findOne({
        where: { idUtilisateur: actorId },
        relations: ['etablissement'],
      });
      const efp = directeur?.etablissement?.idEtablissement;
      if (
        classe.idEtablissement !== efp ||
        cours.idEtablissement !== efp ||
        formateur.idEtablissement !== efp
      ) {
        throw new ForbiddenException('Affectation hors de votre établissement');
      }
    }

    const existing = await this.repo.findOne({
      where: { idClasse: normalized.idClasse, idCours: normalized.idCours },
    });
    if (existing && existing.idAffectation !== currentId) {
      throw new BadRequestException('Cette classe a déjà un formateur pour ce cours');
    }

    return normalized;
  }

  async create(dto: CreateAffectationDto, actorId?: string, actorRole?: Role) {
    const normalized = await this.validate(dto, undefined, actorId, actorRole);
    return this.repo.save(this.repo.create(normalized));
  }

  async update(
    id: string,
    dto: UpdateAffectationDto,
    actorId?: string,
    actorRole?: Role,
  ) {
    const affectation = await this.findOne(id, actorId, actorRole);
    const incoming = normalizeAffectation(dto);
    const normalized = {
      idClasse: incoming.idClasse ?? affectation.idClasse,
      idCours: incoming.idCours ?? affectation.idCours,
      idFormateur: incoming.idFormateur ?? affectation.idFormateur,
    };

    const validated = await this.validate(normalized as CreateAffectationDto, id, actorId, actorRole);
    Object.assign(affectation, validated);
    return this.repo.save(affectation);
  }

  async remove(id: string, actorId?: string, actorRole?: Role) {
    await this.findOne(id, actorId, actorRole);
    const result = await this.repo.delete(id);
    if (!result.affected) throw new NotFoundException('Affectation introuvable');
    return { success: true };
  }
}
