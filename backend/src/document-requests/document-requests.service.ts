import { BadRequestException, ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { v4 as uuid } from 'uuid';
import { DemandeDocument, StatutDemande, TypeDocument } from '../entities/demande-document.entity';
import { CreateDocumentRequestDto } from './dto/create-document-request.dto';
import { RefuseDocumentRequestDto } from './dto/refuse-document-request.dto';
import { NotificationsService } from '../notifications/notifications.service';
import { Document, DocumentStatus } from '../entities/document.entity';
import { PdfService } from '../common/services/pdf.service';
import { FileStorageService } from '../common/services/file-storage.service';
import { stagiaire } from '../entities/stagiaire.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';

const DOCUMENT_TITLES: Record<TypeDocument, string> = {
  [TypeDocument.CERTIFICAT_SCOLARITE]: 'Certificat de scolarité',
  [TypeDocument.RELEVE_NOTES]: 'Relevé de notes',
  [TypeDocument.ATTESTATION_REUSSITE]: 'Attestation de réussite',
  [TypeDocument.BULLETIN]: 'Bulletin',
  [TypeDocument.ATTESTATION_INSCRIPTION]: 'Attestation de poursuite de formation',
};

@Injectable()
export class DocumentRequestsService {
  constructor(
    @InjectRepository(DemandeDocument) private readonly demandeRepo: Repository<DemandeDocument>,
    @InjectRepository(Document) private readonly documentRepo: Repository<Document>,
    @InjectRepository(stagiaire) private readonly stagiaireRepo: Repository<stagiaire>,
    @InjectRepository(Gestionnaire) private readonly gestionnaireRepo: Repository<Gestionnaire>,
    private readonly notifications: NotificationsService,
    private readonly pdf: PdfService,
    private readonly storage: FileStorageService,
  ) {}

  async create(dto: CreateDocumentRequestDto, stagiaireId: string) {
    if (!dto.typeDocument && !dto.idDocument) throw new BadRequestException('typeDocument ou idDocument requis');
    const student = await this.stagiaireRepo.findOne({ where: { idUtilisateur: stagiaireId } });
    if (!student?.idEtablissement) throw new BadRequestException('L’étudiant n’est affecté à aucun établissement');
    const gestionnaires = await this.gestionnaireRepo.find({ where: { idEtablissement: student.idEtablissement } });
    const gestionnaire = gestionnaires[0];
    if (!gestionnaire) throw new BadRequestException('Aucun Gestionnaire n’est affecté à cet établissement');
    if (dto.idDocument) {
      const doc = await this.documentRepo.findOne({ where: { idDocument: dto.idDocument, statut: DocumentStatus.APPROVED } });
      if (!doc) throw new BadRequestException('Document non disponible');
      if (doc.idGestionnaire !== gestionnaire.idUtilisateur) throw new BadRequestException('Document non disponible pour votre établissement');
      const demande = this.demandeRepo.create({ typeDocument: null, idDocument: doc.idDocument, statut: StatutDemande.EN_ATTENTE, idstagiaire: stagiaireId, idGestionnaire: gestionnaire.idUtilisateur, idEtablissement: student.idEtablissement, idAdministrateur: null, fichierGenere: null });
      return this.demandeRepo.save(demande);
    }
    return this.demandeRepo.save(this.demandeRepo.create({ typeDocument: dto.typeDocument ?? null, idDocument: null, statut: StatutDemande.EN_ATTENTE, idstagiaire: stagiaireId, idGestionnaire: gestionnaire.idUtilisateur, idEtablissement: student.idEtablissement, idAdministrateur: null, fichierGenere: null }));
  }

  async findAll(statut: string | undefined, userId: string, role: string) {
    const where: any = {};
    if (statut) where.statut = statut as StatutDemande;
    if (role === 'stagiaire') where.idstagiaire = userId;
    if (role === 'gestionnaire') where.idGestionnaire = userId;
    return this.demandeRepo.find({ where, relations: ['stagiaire', 'stagiaire.utilisateur', 'document'], order: { dateDemande: 'DESC' } });
  }

  private async ensureGestionnaireRequest(id: string, gestionnaireId: string) {
    const demande = await this.demandeRepo.findOne({ where: { idDemande: id }, relations: ['stagiaire', 'stagiaire.utilisateur', 'document'] });
    if (!demande) throw new NotFoundException('Demande introuvable');
    if (demande.idGestionnaire !== gestionnaireId) throw new ForbiddenException('Cette demande ne relève pas de votre établissement');
    if (demande.statut !== StatutDemande.EN_ATTENTE) throw new BadRequestException('Cette demande a déjà été traitée');
    return demande;
  }

  async generateForGestionnaire(id: string, gestionnaireId: string) {
    const demande = await this.ensureGestionnaireRequest(id, gestionnaireId);
    const student = await this.stagiaireRepo.findOne({
      where: { idUtilisateur: demande.idstagiaire },
      relations: ['utilisateur', 'etablissement', 'classe'],
    });
    if (!student) throw new NotFoundException('Étudiant introuvable');
    const title = demande.typeDocument ? DOCUMENT_TITLES[demande.typeDocument] : demande.document?.nomDocument ?? 'Document administratif';
    const user = student.utilisateur;
    let buffer: Buffer;
    if (demande.typeDocument === TypeDocument.ATTESTATION_INSCRIPTION) {
      const region = user?.region ?? '';
      buffer = await this.pdf.generateAttestationInscription({
        nomComplet: `${user?.nom ?? ''} ${user?.prenom ?? ''}`.trim(),
        dateNaissance: 'Non renseignée',
        lieuNaissance: region || '—',
        niveau: 'Technicien spécialisé',
        specialite: student.classe?.nomClasse || 'Infrastructure Digitale',
        annee: student.promotion || '2025/2026',
        numeroInscription: student.numerostagiaire || '—',
        etablissement: student.etablissement?.nomEtablissement || 'Établissement non renseigné',
        poursuiteDepuis: '—',
        ville: 'Rabat',
        dateEdition: new Date().toLocaleDateString('fr-FR'),
      });
    } else {
      buffer = await this.pdf.generateDocument(title, [
        `Nom : ${user?.nom ?? ''} ${user?.prenom ?? ''}`,
        `Numéro étudiant : ${student.numerostagiaire ?? '—'}`,
        `Promotion : ${student.promotion ?? '—'}`,
        `Établissement : ${student.etablissement?.nomEtablissement ?? '—'}`,
        '',
        `Document demandé : ${title}`,
        `Ce document est généré pour l'étudiant identifié ci-dessus.`,
      ]);
    }
    const filePath = await this.storage.save('generated-documents', `${demande.idDemande}-${uuid()}.pdf`, buffer, 'application/pdf');
    demande.statut = StatutDemande.DELIVREE; demande.dateTraitement = new Date(); demande.fichierGenere = filePath;
    const saved = await this.demandeRepo.save(demande);
    await this.notifications.notifyUsers([demande.idstagiaire], 'document_delivre', `Votre document "${title}" a été accepté et est disponible.`);
    return saved;
  }

  async refuseForGestionnaire(id: string, dto: RefuseDocumentRequestDto, gestionnaireId: string) {
    const demande = await this.ensureGestionnaireRequest(id, gestionnaireId);
    demande.statut = StatutDemande.REFUSEE; demande.dateTraitement = new Date();
    const saved = await this.demandeRepo.save(demande);
    await this.notifications.notifyUsers([demande.idstagiaire], 'document_refuse', `Votre demande de document a été refusée.${dto.motif ? ` Motif : ${dto.motif}` : ''}`);
    return saved;
  }

  async getFile(id: string, userId: string, role: string) {
    const demande = await this.demandeRepo.findOne({ where: { idDemande: id } });
    if (!demande) throw new NotFoundException('Demande introuvable');
    if (role === 'stagiaire' && demande.idstagiaire !== userId) throw new ForbiddenException('Accès non autorisé');
    if (role === 'gestionnaire' && demande.idGestionnaire !== userId) throw new ForbiddenException('Accès non autorisé');
    if (!demande.fichierGenere) throw new BadRequestException('Aucun fichier généré pour cette demande');
    return { buffer: await this.storage.read(demande.fichierGenere), filename: `${demande.typeDocument ?? 'document'}-${demande.idDemande}.pdf` };
  }
}
