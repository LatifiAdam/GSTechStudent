import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  OneToMany,
  JoinColumn,
} from 'typeorm';

import { Creneau } from './creneau.entity';
import { Formateur } from './formateur.entity';
import { Presence } from './presence.entity';

@Entity('appel')
export class Appel {
  @PrimaryGeneratedColumn('uuid', { name: 'id_appel' })
  idAppel: string;

  @Column({
    name: 'date_heure',
    type: 'datetime',
    default: () => 'CURRENT_TIMESTAMP',
  })
  dateHeure: Date;

  @Column({
    type: 'boolean',
    default: false,
  })
  valide: boolean;

  @Column({
    name: 'id_creneau',
    type: 'char',
    length: 36,
  })
  idCreneau: string;

  @Column({
    name: 'id_formateur',
    type: 'char',
    length: 36,
  })
  idFormateur: string;

  @ManyToOne(
    () => Creneau,
    { onDelete: 'CASCADE' },
  )
  @JoinColumn({ name: 'id_creneau' })
  creneau: Creneau;

  @ManyToOne(
    () => Formateur,
    (formateur) => formateur.appels,
    { onDelete: 'RESTRICT' },
  )
  @JoinColumn({ name: 'id_formateur' })
  formateur: Formateur;

  @OneToMany(
    () => Presence,
    (presence) => presence.appel,
  )
  presences: Presence[];
}