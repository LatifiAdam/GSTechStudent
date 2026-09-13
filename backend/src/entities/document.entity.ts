import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';

export enum DocumentStatus {
  PENDING = 'en_attente',
  APPROVED = 'approuve',
  REFUSED = 'refuse',
}

@Entity('document')
export class Document {
  @PrimaryGeneratedColumn('uuid', { name: 'id_document' })
  idDocument: string;

  @Column({ name: 'nom_document', type: 'varchar', length: 255 })
  nomDocument: string;

  @Column({ name: 'type_document', type: 'varchar', length: 100 })
  typeDocument: string;

  @Column({ name: 'fichier', type: 'varchar', length: 512 })
  fichier: string;

  @Column({ type: 'enum', enum: DocumentStatus, default: DocumentStatus.PENDING })
  statut: DocumentStatus;

  @CreateDateColumn({ name: 'date_creation', type: 'datetime' })
  dateCreation: Date;

  @Column({ name: 'date_validation', type: 'datetime', nullable: true })
  dateValidation: Date | null;

  @Column({ name: 'motif_refus', type: 'varchar', length: 500, nullable: true })
  motifRefus: string | null;

  @Column({ name: 'id_gestionnaire', type: 'char', length: 36 })
  idGestionnaire: string;

  @Column({ name: 'id_directeur', type: 'char', length: 36, nullable: true })
  idDirecteur: string | null;
}
