import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  OneToOne,
  JoinColumn,
} from 'typeorm';

import { Presence } from './presence.entity';

export enum StatutJustification {
  EN_ATTENTE = 'en_attente',
  ACCEPTEE = 'acceptee',
  REFUSEE = 'refusee',
}

@Entity('justification')
export class Justification {
  @PrimaryGeneratedColumn('uuid', { name: 'id_justification' })
  idJustification: string;

  @Column({ type: 'text' })
  motif: string;

  @Column({ name: 'piece_jointe', type: 'varchar', length: 512, nullable: true })
  pieceJointe: string | null;

  @Column({
    name: 'statut_justification',
    type: 'enum',
    enum: StatutJustification,
    default: StatutJustification.EN_ATTENTE,
  })
  statutJustification: StatutJustification;

  @CreateDateColumn({ name: 'date_envoi', type: 'datetime' })
  dateEnvoi: Date;

  @Column({ name: 'id_presence', type: 'char', length: 36, unique: true })
  idPresence: string;

  // Relation

  @OneToOne(() => Presence, (presence) => presence.justification, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'id_presence' })
  presence: Presence;
}
