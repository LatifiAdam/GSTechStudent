import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, JoinColumn, Unique, CreateDateColumn, UpdateDateColumn } from 'typeorm';
import { stagiaire } from './stagiaire.entity';
import { Affectation } from './affectation.entity';

@Entity('note')
@Unique('uq_note_stagiaire_affectation', ['idstagiaire', 'idAffectation'])
export class Note {
  @PrimaryGeneratedColumn('uuid', { name: 'id_note' })
  idNote: string;

  @Column({ name: 'id_stagiaire', type: 'char', length: 36 })
  idstagiaire: string;

  @Column({ name: 'id_affectation', type: 'char', length: 36 })
  idAffectation: string;

  @Column({ name: 'note1', type: 'decimal', precision: 5, scale: 2, nullable: true })
  note1: number | null;

  @Column({ name: 'note2', type: 'decimal', precision: 5, scale: 2, nullable: true })
  note2: number | null;

  @Column({ name: 'note3', type: 'decimal', precision: 5, scale: 2, nullable: true })
  note3: number | null;

  @CreateDateColumn({ name: 'date_creation', type: 'datetime' })
  dateCreation: Date;

  @UpdateDateColumn({ name: 'date_modification', type: 'datetime' })
  dateModification: Date;

  @ManyToOne(() => stagiaire, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'id_stagiaire' })
  stagiaire: stagiaire;

  @ManyToOne(() => Affectation, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'id_affectation' })
  affectation: Affectation;
}
