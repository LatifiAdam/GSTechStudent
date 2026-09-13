import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  OneToMany,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { Affectation } from './affectation.entity';
import { Annonce } from './annonce.entity';
import { Etablissement } from './etablissement.entity';

@Entity('cours')
export class Cours {
  @PrimaryGeneratedColumn('uuid', { name: 'id_cours' })
  idCours: string;

  @Column({ name: 'id_etablissement', type: 'char', length: 36, nullable: true, })
  idEtablissement: string | null;

  @ManyToOne(() => Etablissement, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'id_etablissement' })
  etablissement: Etablissement | null;

  @Column({
    name: 'nom_cours',
    type: 'varchar',
    length: 150,
  })
  nomCours: string;

  @Column({
    name: 'date_creation',
    type: 'datetime',
    default: () => 'CURRENT_TIMESTAMP',
  })
  dateCreation: Date;

  @OneToMany(() => Affectation, (affectation) => affectation.cours)
  affectations: Affectation[];

  @OneToMany(() => Annonce, (annonce) => annonce.cours)
  annonces: Annonce[];
}