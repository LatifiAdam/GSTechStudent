import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { stagiaire } from './stagiaire.entity';
import { Administrateur } from './administrateur.entity';
import { Document } from './document.entity';
import { Gestionnaire } from './gestionnaire.entity';
import { Etablissement } from './etablissement.entity';

export enum TypeDocument {
  CERTIFICAT_SCOLARITE = 'certificat_scolarite',
  RELEVE_NOTES = 'releve_notes',
  ATTESTATION_REUSSITE = 'attestation_reussite',
  BULLETIN = 'bulletin',
}

export enum StatutDemande {
  EN_ATTENTE = 'en_attente',
  EN_COURS = 'en_cours',
  DELIVREE = 'delivree',
  REFUSEE = 'refusee',
}

/**
 * Demande de document administratif (nouveau - Phase 1).
 * RG9 : soumise uniquement par l'étudiant concerné.
 * RG10 : traitée (générée/refusée) uniquement par un administrateur.
 */
@Entity('demande_document')
export class DemandeDocument {
  @PrimaryGeneratedColumn('uuid', { name: 'id_demande' })
  idDemande: string;

  @Column({ name: 'type_document', type: 'enum', enum: TypeDocument, nullable: true })
  typeDocument: TypeDocument | null;

  @Column({ type: 'enum', enum: StatutDemande, default: StatutDemande.EN_ATTENTE })
  statut: StatutDemande;

  @CreateDateColumn({ name: 'date_demande', type: 'datetime' })
  dateDemande: Date;

  @Column({ name: 'date_traitement', type: 'datetime', nullable: true })
  dateTraitement: Date | null;

  @Column({ name: 'fichier_genere', type: 'varchar', length: 512, nullable: true })
  fichierGenere: string | null;

  @Column({ name: 'id_stagiaire', type: 'char', length: 36 })
  idstagiaire: string;

  @Column({ name: 'id_document', type: 'char', length: 36, nullable: true })
  idDocument: string | null;

  @Column({ name: 'id_gestionnaire', type: 'char', length: 36, nullable: true })
  idGestionnaire: string | null;

  @Column({ name: 'id_etablissement', type: 'char', length: 36, nullable: true })
  idEtablissement: string | null;

  @Column({ name: 'id_administrateur', type: 'char', length: 36, nullable: true })
  idAdministrateur: string | null;

  // Relations

  @ManyToOne(() => stagiaire, (stagiaire) => stagiaire.demandesDocuments)
  @JoinColumn({ name: 'id_stagiaire' })
  stagiaire: stagiaire;

  @ManyToOne(() => Gestionnaire, { nullable: true })
  @JoinColumn({ name: 'id_gestionnaire' })
  gestionnaire: Gestionnaire | null;

  @ManyToOne(() => Etablissement, { nullable: true })
  @JoinColumn({ name: 'id_etablissement' })
  etablissement: Etablissement | null;

  @ManyToOne(() => Administrateur, { nullable: true })
  @JoinColumn({ name: 'id_administrateur' })
  administrateur: Administrateur;

  @ManyToOne(() => Document, { nullable: true })
  @JoinColumn({ name: 'id_document' })
  document: Document;
}
