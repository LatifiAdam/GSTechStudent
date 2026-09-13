import {
  Entity,
  PrimaryColumn,
  Column,
  OneToOne,
  OneToMany,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { Utilisateur } from './utilisateur.entity';
import { Affectation } from './affectation.entity';
import { Appel } from './appel.entity';
import { Etablissement } from './etablissement.entity';

@Entity('formateur')
export class Formateur {
  @PrimaryColumn('uuid', {
    name: 'id_utilisateur',
  })
  idUtilisateur: string;

  @Column({ name: 'id_etablissement', type: 'char', length: 36, nullable: true, })
  idEtablissement: string | null;

  @ManyToOne(() => Etablissement, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'id_etablissement' })
  etablissement: Etablissement | null;

  @Column({
    type: 'varchar',
    length: 100,
    nullable: true,
  })
  module: string | null;

  @OneToOne(
    () => Utilisateur,
    (utilisateur) => utilisateur.formateur,
    {
      onDelete: 'CASCADE',
    },
  )
  @JoinColumn({
    name: 'id_utilisateur',
  })
  utilisateur: Utilisateur;

  @OneToMany(
    () => Affectation,
    (affectation) =>
      affectation.formateur,
  )
  affectations: Affectation[];

  @OneToMany(
    () => Appel,
    (appel) => appel.formateur,
  )
  appels: Appel[];
}