// src/entities/presence.entity.ts

import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  OneToOne,
  JoinColumn,
  Unique,
} from 'typeorm';

import { Appel } from './appel.entity';
import { stagiaire } from './stagiaire.entity';
import { Justification } from './justification.entity';

export enum StatutPresence {
  PRESENT = 'present',
  ABSENT = 'absent',
  RETARD = 'retard',
}

@Entity('presence')
@Unique(
  'uq_presence_appel_stagiaire',
  ['idAppel', 'idstagiaire'],
)
export class Presence {
  @PrimaryGeneratedColumn('uuid', {
    name: 'id_presence',
  })
  idPresence: string;

  @Column({
    type: 'enum',
    enum: StatutPresence,
    default: StatutPresence.PRESENT,
  })
  statut: StatutPresence;

  @Column({
    name: 'id_appel',
    type: 'char',
    length: 36,
  })
  idAppel: string;

  @Column({
    name: 'id_stagiaire',
    type: 'char',
    length: 36,
  })
  idstagiaire: string;

  @ManyToOne(
    () => Appel,
    (appel) => appel.presences,
    {
      onDelete: 'CASCADE',
    },
  )
  @JoinColumn({
    name: 'id_appel',
  })
  appel: Appel;

  @ManyToOne(
    () => stagiaire,
    (stagiaire) => stagiaire.presences,
    {
      onDelete: 'CASCADE',
    },
  )
  @JoinColumn({
    name: 'id_stagiaire',
  })
  stagiaire: stagiaire;

  @OneToOne(
    () => Justification,
    (justification) =>
      justification.presence,
  )
  justification: Justification;
}