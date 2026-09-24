import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Etablissement } from '../entities/etablissement.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Directeur } from '../entities/directeur.entity';
import { Utilisateur } from '../entities/utilisateur.entity';
import { Role } from '../common/enums/roles.enum';

@Injectable()
export class EtablissementsService {
  constructor(
    @InjectRepository(Etablissement) private readonly repo: Repository<Etablissement>,
    @InjectRepository(Gestionnaire) private readonly gestionnaireRepo: Repository<Gestionnaire>,
    @InjectRepository(Directeur) private readonly directeurRepo: Repository<Directeur>,
    @InjectRepository(Utilisateur) private readonly userRepo: Repository<Utilisateur>,
  ) {}

  private regionName(value: string | number | null | undefined): string | null {
    if (value === null || value === undefined || value === '') return null;
    const normalized = String(value).trim().toLowerCase();
    const names: Record<string, string> = {
      '1': 'Rabat-Salé-Kénitra', 'rsk': 'Rabat-Salé-Kénitra',
      '2': 'Casablanca-Settat', 'cs': 'Casablanca-Settat',
      '3': 'Tanger-Tétouan-Al Hoceïma', 'tta': 'Tanger-Tétouan-Al Hoceïma',
      '4': 'Fès-Meknès', 'fm': 'Fès-Meknès',
      '5': 'Marrakech-Safi', 'm': 'Marrakech-Safi',
      '6': 'Oriental', 'or': 'Oriental',
      '7': 'Béni Mellal-Khénifra', 'bs': 'Béni Mellal-Khénifra',
      '8': 'Drâa-Tafilalet', 'd': 'Drâa-Tafilalet',
      '9': 'Souss-Massa', 'smd': 'Souss-Massa',
      '10': 'Guelmim-Oued Noun', 'gon': 'Guelmim-Oued Noun',
    };
    return names[normalized] ?? String(value); 
  }

  private normalizeRegionCode(value: string | number | null | undefined): string | null {
    if (value === null || value === undefined || String(value).trim() === '') return null;
    const raw = String(value).trim();
    const normalized = raw.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    const aliases: Record<string, string> = {
      '1': 'RSK', 'rsk': 'RSK', 'rabat-sale-kenitra': 'RSK',
      '2': 'CS', 'cs': 'CS', 'casablanca-settat': 'CS',
      '3': 'TTA', 'tta': 'TTA', 'tanger-tetouan-al hoceima': 'TTA', 'tanger-tetouan-al-hoceima': 'TTA',
      '4': 'FM', 'fm': 'FM', 'fes-meknes': 'FM',
      '5': 'M', 'm': 'M', 'marrakech-safi': 'M',
      '6': 'OR', 'or': 'OR', 'oriental': 'OR',
      '7': 'BS', 'bs': 'BS', 'beni mellal-khenifra': 'BS',
      '8': 'D', 'd': 'D', 'draa-tafilalet': 'D',
      '9': 'SMD', 'smd': 'SMD', 'souss-massa': 'SMD',
      '10': 'GON', 'gon': 'GON', 'guelmim-oued noun': 'GON',
    };
    return aliases[normalized] ?? null;
  }

  private sameRegion(a: string | number | null | undefined, b: string | number | null | undefined): boolean {
    return !!a && !!b && this.regionName(a)?.toLowerCase() === this.regionName(b)?.toLowerCase();
  }

  private map(e: Etablissement) {
    return {
      idEtablissement: e.idEtablissement,
      nomEtablissement: e.nomEtablissement,
      region: this.regionName(e.region),
      idDirecteur: e.idDirecteur,
      directeur: e.directeur?.utilisateur ? { idUtilisateur: e.directeur.idUtilisateur, nom: e.directeur.utilisateur.nom, prenom: e.directeur.utilisateur.prenom, email: e.directeur.utilisateur.email, role: Role.DIRECTEUR } : null,
      gestionnaires: (e.gestionnaires || []).filter(g => !!g.utilisateur).map(g => ({ idUtilisateur: g.idUtilisateur, nom: g.utilisateur!.nom, prenom: g.utilisateur!.prenom, email: g.utilisateur!.email, role: Role.GESTIONNAIRE })),
    };
  }

  async list(actorId?: string, actorRole?: Role) {
    const actor = actorId ? await this.userRepo.findOne({ where: { idUtilisateur: actorId } }) : null;
    const rows = await this.repo.find({ relations: ['directeur','directeur.utilisateur','gestionnaires','gestionnaires.utilisateur'], order: { nomEtablissement: 'ASC' } });
    const scoped = [Role.SRIO, Role.SCQ].includes(actorRole as Role) && actor?.region
      ? rows.filter(e => this.sameRegion(e.region, actor.region))
      : rows;
    return scoped.map(e => this.map(e));
  }

  async create(nomEtablissement: string, region?: string | number) {
    if (!nomEtablissement?.trim()) throw new BadRequestException('Le nom de l’établissement est obligatoire');
    if (region === undefined || region === null || String(region).trim() === '') {
      throw new BadRequestException('La région est obligatoire');
    }
    const normalizedName = nomEtablissement.trim();
    const normalizedRegion = this.normalizeRegionCode(region);
    if (!normalizedRegion) throw new BadRequestException('Région invalide. Sélectionnez une des dix régions officielles.');
    const existing = await this.repo.findOne({ where: { nomEtablissement: normalizedName, region: normalizedRegion } });
    if (existing) {
      throw new BadRequestException(`Un établissement nommé « ${normalizedName} » existe déjà dans cette région`);
    }
    try {
      return await this.repo.save(this.repo.create({
        nomEtablissement: normalizedName,
        region: normalizedRegion,
        idDirecteur: null,
      }));
    } catch (error: any) {
      if (error?.code === 'ER_DUP_ENTRY' || error?.driverError?.code === 'ER_DUP_ENTRY') {
        throw new BadRequestException(`Un établissement nommé « ${normalizedName} » existe déjà dans cette région`);
      }
      throw error;
    }
  }
  async get(id: string) {
    const e = await this.repo.findOne({
      where: { idEtablissement: id },
      relations: ['directeur', 'directeur.utilisateur', 'gestionnaires', 'gestionnaires.utilisateur'],
    });
    if (!e) throw new NotFoundException('Établissement introuvable');
    return e;
  }

  async update(idEtablissement: string, nomEtablissement: string) {
    const name = nomEtablissement?.trim();
    if (!name) throw new BadRequestException('Le nom de l’établissement est obligatoire');
    const etab = await this.get(idEtablissement);
    const duplicate = await this.repo.findOne({
      where: { nomEtablissement: name, region: etab.region ?? undefined },
    });
    if (duplicate && duplicate.idEtablissement !== idEtablissement) {
      throw new BadRequestException(`Un établissement nommé « ${name} » existe déjà dans cette région`);
    }
    etab.nomEtablissement = name;
    return this.repo.save(etab);
  }

  async remove(idEtablissement: string) {
    const etab = await this.get(idEtablissement);
    await this.repo.remove(etab);
    return { deleted: true, idEtablissement };
  }

  async assignDirector(idEtablissement:string,idUtilisateur:string,actorId?:string,actorRole?:Role){
    const etab = await this.get(idEtablissement);
    if (actorRole === Role.SCQ) {
      const actor = await this.userRepo.findOne({ where: { idUtilisateur: actorId } });
      if (!actor || !this.sameRegion(actor.region, etab.region)) {
        throw new ForbiddenException('Cet établissement est hors de votre région');
      }
    }
    const user = await this.userRepo.findOne({ where: { idUtilisateur } });
    if (!user || user.role !== Role.DIRECTEUR) {
      throw new BadRequestException('Utilisateur invalide : un Directeur est requis');
    }
    if (actorRole === Role.SCQ && !this.sameRegion(user.region, etab.region)) {
      throw new BadRequestException('Ce Directeur n’appartient pas à la région de cet établissement');
    }
    const director = await this.directeurRepo.findOne({ where: { idUtilisateur } });
    if (!director) throw new BadRequestException('Compte Directeur incomplet');
    const existing = await this.repo.findOne({ where: { idDirecteur: idUtilisateur } });
    if (existing && existing.idEtablissement !== idEtablissement) {
      throw new BadRequestException('Ce Directeur est déjà affecté à un établissement');
    }

    // Update only the FK. This avoids re-validating unrelated établissement fields.
    await this.repo.update({ idEtablissement }, { idDirecteur: idUtilisateur });
    return this.get(idEtablissement).then(e => this.map(e));
  }

  async removeDirector(idEtablissement:string,actorId?:string,actorRole?:Role){
    const etab = await this.get(idEtablissement);
    if (actorRole === Role.SCQ) {
      const actor = await this.userRepo.findOne({ where: { idUtilisateur: actorId } });
      if (!actor || !this.sameRegion(actor.region, etab.region)) throw new ForbiddenException('Cet établissement est hors de votre région');
    }
    await this.repo.update({ idEtablissement }, { idDirecteur: null });
    return this.get(idEtablissement).then(e => this.map(e));
  }

  async addGestionnaire(idEtablissement:string,idGestionnaire:string,actorId:string,actorRole:Role){
    const etab=await this.get(idEtablissement); if(actorRole===Role.DIRECTEUR) throw new ForbiddenException('Le Directeur ne gère pas les Gestionnaires'); if([Role.DIRECTEUR].includes(actorRole!) && etab.idDirecteur!==actorId) throw new ForbiddenException('Cet établissement ne dépend pas de ce Directeur');
    const user=await this.userRepo.findOne({where:{idUtilisateur:idGestionnaire}}); if(!user || user.role!==Role.GESTIONNAIRE) throw new BadRequestException('Utilisateur invalide : un Gestionnaire est requis');
    const g=await this.gestionnaireRepo.findOne({where:{idUtilisateur:idGestionnaire}}); if(!g) throw new BadRequestException('Compte Gestionnaire incomplet');
    if(actorRole===Role.SRIO){
      const actor=await this.userRepo.findOne({where:{idUtilisateur:actorId}});
      if(!actor || !this.sameRegion(actor.region, etab.region)) throw new ForbiddenException('Cet établissement est hors de votre région');
      if(!user.region || !this.sameRegion(user.region, etab.region)) {
        throw new BadRequestException("Ce Gestionnaire n'appartient pas à la région de cet établissement");
      }
    }
    if(g.idEtablissement && g.idEtablissement!==idEtablissement) throw new BadRequestException('Ce Gestionnaire est déjà affecté à un autre établissement');
    g.idEtablissement=idEtablissement; return this.gestionnaireRepo.save(g);
  }

  async removeGestionnaire(idEtablissement:string,idGestionnaire:string,actorId:string,actorRole:Role){
    const etab=await this.get(idEtablissement); if(actorRole===Role.DIRECTEUR) throw new ForbiddenException('Le Directeur ne gère pas les Gestionnaires'); if([Role.DIRECTEUR].includes(actorRole!) && etab.idDirecteur!==actorId) throw new ForbiddenException('Cet établissement ne dépend pas de ce Directeur');
    const g=await this.gestionnaireRepo.findOne({where:{idUtilisateur:idGestionnaire,idEtablissement}}); if(!g) throw new NotFoundException('Gestionnaire introuvable dans cet établissement');
    g.idEtablissement=null; return this.gestionnaireRepo.save(g);
  }

  async myEstablishment(directorId:string){ const e=await this.repo.findOne({where:{idDirecteur:directorId},relations:['directeur','directeur.utilisateur','gestionnaires','gestionnaires.utilisateur']}); return e ? this.map(e) : null; }
}
