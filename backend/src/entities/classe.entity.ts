import {
  Entity,
  Unique,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  OneToMany,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { stagiaire } from './stagiaire.entity';
import { Affectation } from './affectation.entity';
import { Etablissement } from './etablissement.entity';

@Entity('classe')
@Unique('uq_classe_etablissement_nom', ['idEtablissement', 'nomClasse'])
export class Classe {
  @PrimaryGeneratedColumn('uuid', { name: 'id_classe' })
  idClasse: string;

  @Column({ name: 'id_etablissement', type: 'char', length: 36, nullable: true, })
  idEtablissement: string | null;

  @ManyToOne(() => Etablissement, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'id_etablissement' })
  etablissement: Etablissement | null;

  @Column({
    name: 'nom_classe',
    type: 'varchar',
    length: 100,
    unique: false,
  })
  nomClasse: string;

  @Column({
    type: 'varchar',
    length: 255,
    nullable: true,
  })
  description: string | null;

  @CreateDateColumn({
    name: 'date_creation',
    type: 'datetime',
  })
  dateCreation: Date;

  @OneToMany(() => stagiaire, (stagiaire) => stagiaire.classe)
  stagiaires: stagiaire[];

  @OneToMany(() => Affectation, (affectation) => affectation.classe)
  affectations: Affectation[];
}