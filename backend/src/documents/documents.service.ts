import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Document, DocumentStatus } from '../entities/document.entity';
import { FileStorageService } from '../common/services/file-storage.service';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Etablissement } from '../entities/etablissement.entity';
import { stagiaire } from '../entities/stagiaire.entity';

@Injectable()
export class DocumentsService {
  constructor(
    @InjectRepository(Document) private readonly repo: Repository<Document>,
    private readonly storage: FileStorageService,
    @InjectRepository(Gestionnaire) private readonly gestionnaireRepo: Repository<Gestionnaire>,
    @InjectRepository(Etablissement) private readonly etablissementRepo: Repository<Etablissement>,
    @InjectRepository(stagiaire) private readonly stagiaireRepo: Repository<stagiaire>,
  ) {}

  list(status?: DocumentStatus) {
    return this.repo.find({ where: status ? { statut: status } : {}, order: { dateCreation: 'DESC' } });
  }

  async pendingForDirector(idDirecteur: string) {
    return this.repo.createQueryBuilder('d')
      .where('d.statut = :status', { status: DocumentStatus.PENDING })
      .andWhere('d.id_directeur = :idDirecteur', { idDirecteur })
      .orderBy('d.date_creation', 'DESC')
      .getMany();
  }

  mine(idGestionnaire: string) {
    return this.repo.find({ where: { idGestionnaire }, order: { dateCreation: 'DESC' } });
  }

  async approvedForStudent(studentId: string) {
    const student = await this.stagiaireRepo.findOne({ where: { idUtilisateur: studentId } });
    if (!student?.idEtablissement) return [];

    const gestionnaires = await this.gestionnaireRepo.find({
      where: { idEtablissement: student.idEtablissement },
    });
    const ids = gestionnaires.map((g) => g.idUtilisateur);
    if (!ids.length) return [];

    return this.repo
      .createQueryBuilder('d')
      .where('d.statut = :status', { status: DocumentStatus.APPROVED })
      .andWhere('d.id_gestionnaire IN (:...gestionnaires)', { gestionnaires: ids })
      .orderBy('d.nom_document', 'ASC')
      .getMany();
  }

  async upload(file: Express.Multer.File, nomDocument: string, idGestionnaire: string) {
    if (!file) throw new BadRequestException('Fichier PDF requis');
    if (file.mimetype !== 'application/pdf') throw new BadRequestException('Seuls les fichiers PDF sont autorisés');
    if (!file.buffer?.length) throw new BadRequestException('Le fichier est vide');
    if (file.buffer.length > 10 * 1024 * 1024) throw new BadRequestException('Le fichier ne doit pas dépasser 10 Mo');
    const signature = Buffer.from('%PDF-');
    if (!file.buffer.subarray(0, signature.length).equals(signature)) throw new BadRequestException('Le contenu ne correspond pas à un PDF valide');
    const gestionnaire = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: idGestionnaire } });
    if (!gestionnaire?.idEtablissement) throw new BadRequestException('Le Gestionnaire doit être affecté à un établissement');
    const etab = await this.etablissementRepo.findOne({ where: { idEtablissement: gestionnaire.idEtablissement } });
    if (!etab?.idDirecteur) throw new BadRequestException('Aucun Directeur n’est affecté à cet établissement');
    const safe = file.originalname.replace(/[^a-zA-Z0-9._-]/g, '_');
    const filename = `${Date.now()}-${safe}`;
    const stored = await this.storage.save('documents', filename, file.buffer, 'application/pdf');
    return this.repo.save(this.repo.create({
      nomDocument: nomDocument?.trim() || file.originalname,
      typeDocument: 'pdf',
      fichier: stored,
      statut: DocumentStatus.PENDING,
      dateValidation: null,
      motifRefus: null,
      idGestionnaire,
      idDirecteur: etab.idDirecteur,
    }));
  }

  async approve(id: string, idDirecteur: string) {
    const doc = await this.repo.findOne({ where: { idDocument: id } });
    if (!doc) throw new NotFoundException('Document introuvable');
    const etab = await this.etablissementRepo.findOne({ where: { idDirecteur } });
    const gestionnaire = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: doc.idGestionnaire } });
    if (!etab || !gestionnaire || gestionnaire.idEtablissement !== etab.idEtablissement || doc.idDirecteur !== idDirecteur) throw new BadRequestException('Ce document ne relève pas de votre établissement');
    doc.statut = DocumentStatus.APPROVED;
    doc.idDirecteur = idDirecteur;
    doc.dateValidation = new Date();
    doc.motifRefus = null;
    return this.repo.save(doc);
  }

  async refuse(id: string, idDirecteur: string, motif?: string) {
    const doc = await this.repo.findOne({ where: { idDocument: id } });
    if (!doc) throw new NotFoundException('Document introuvable');
    const etab = await this.etablissementRepo.findOne({ where: { idDirecteur } });
    const gestionnaire = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: doc.idGestionnaire } });
    if (!etab || !gestionnaire || gestionnaire.idEtablissement !== etab.idEtablissement || doc.idDirecteur !== idDirecteur) throw new BadRequestException('Ce document ne relève pas de votre établissement');
    doc.statut = DocumentStatus.REFUSED;
    doc.idDirecteur = idDirecteur;
    doc.dateValidation = new Date();
    doc.motifRefus = motif || null;
    return this.repo.save(doc);
  }

  async file(id: string, requesterId?: string, role?: string) {
    const doc = await this.repo.findOne({ where: { idDocument: id } });
    if (!doc) throw new NotFoundException('Document introuvable');
    if (role === 'gestionnaire' && doc.idGestionnaire !== requesterId) throw new NotFoundException('Document introuvable');
    if ((role === 'etudiant' || role === 'stagiaire') && doc.statut !== DocumentStatus.APPROVED) throw new BadRequestException('Document non approuvé');
    const buffer = await this.storage.read(doc.fichier);
    return { buffer, filename: doc.nomDocument, type: doc.typeDocument };
  }
}
