// src/entities/creneau.entity.ts

import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
  Check,
} from 'typeorm';

import { Affectation } from './affectation.entity';

export enum JourSemaine {
  LUNDI = 'lundi',
  MARDI = 'mardi',
  MERCREDI = 'mercredi',
  JEUDI = 'jeudi',
  VENDREDI = 'vendredi',
  SAMEDI = 'samedi',
  DIMANCHE = 'dimanche',
}

@Entity('creneau')
@Check(
  'chk_creneau_heures',
  '`heure_fin` > `heure_debut`',
)
export class Creneau {
  @PrimaryGeneratedColumn('uuid', {
    name: 'id_creneau',
  })
  idCreneau: string;

  @Column({
    name: 'jour_semaine',
    type: 'enum',
    enum: JourSemaine,
  })
  jourSemaine: JourSemaine;

  @Column({
    name: 'heure_debut',
    type: 'time',
  })
  heureDebut: string;

  @Column({
    name: 'heure_fin',
    type: 'time',
  })
  heureFin: string;

  @Column({
    type: 'varchar',
    length: 30,
  })
  salle: string;

  @Column({
    name: 'id_affectation',
    type: 'char',
    length: 36,
  })
  idAffectation: string;

  @Column({
    name: 'date_debut',
    type: 'date',
    nullable: true,
  })
  dateDebut: string | null;

  @Column({
    name: 'date_fin',
    type: 'date',
    nullable: true,
  })
  dateFin: string | null;

  @ManyToOne(
    () => Affectation,
    (affectation) =>
      affectation.creneaux,
    {
      onDelete: 'CASCADE',
    },
  )
  @JoinColumn({
    name: 'id_affectation',
  })
  affectation: Affectation;
}