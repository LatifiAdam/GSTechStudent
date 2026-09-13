import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
  OneToMany,
  Unique,
} from 'typeorm';

import { Classe } from './classe.entity';
import { Cours } from './cours.entity';
import { Formateur } from './formateur.entity';
import { Creneau } from './creneau.entity';

@Entity('affectation')
@Unique('uq_affectation_classe_cours', ['idClasse', 'idCours'])
export class Affectation {
  @PrimaryGeneratedColumn('uuid', { name: 'id_affectation' })
  idAffectation: string;

  @Column({ name: 'id_classe', type: 'char', length: 36 })
  idClasse: string;

  @Column({ name: 'id_cours', type: 'char', length: 36 })
  idCours: string;

  @Column({ name: 'id_formateur', type: 'char', length: 36 })
  idFormateur: string;

  @CreateDateColumn({
    name: 'date_creation',
    type: 'datetime',
  })
  dateCreation: Date;

  @ManyToOne(() => Classe, (classe) => classe.affectations, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'id_classe' })
  classe: Classe;

  @ManyToOne(() => Cours, (cours) => cours.affectations, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'id_cours' })
  cours: Cours;

  @ManyToOne(() => Formateur, (formateur) => formateur.affectations, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'id_formateur' })
  formateur: Formateur;

  @OneToMany(() => Creneau, (creneau) => creneau.affectation)
  creneaux: Creneau[];
}