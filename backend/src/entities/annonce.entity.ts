import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { Cours } from './cours.entity';
import { Utilisateur } from './utilisateur.entity';
import { Classe } from './classe.entity';

export enum TypeAnnonce {
  EXAMEN = 'examen',
  CONTROLE = 'controle',
  GENERALE = 'generale',
}

/**
 * Table ANNONCE.
 *
 * Correspond exactement à schema.sql:
 *
 * id_annonce
 * titre
 * contenu
 * type_annonce
 * date_publication
 * date_evenement
 * id_cours
 * id_auteur
 *
 * Une annonce liée à un cours est associée à ce cours.
 * Une annonce générale possède id_cours = NULL.
 */
@Entity('annonce')
export class Annonce {
  @PrimaryGeneratedColumn('uuid', {
    name: 'id_annonce',
  })
  idAnnonce: string;

  @Column({
    type: 'varchar',
    length: 150,
  })
  titre: string;

  @Column({
    type: 'text',
  })
  contenu: string;

  @Column({
    name: 'type_annonce',
    type: 'enum',
    enum: TypeAnnonce,
  })
  typeAnnonce: TypeAnnonce;

  @CreateDateColumn({
    name: 'date_publication',
    type: 'datetime',
  })
  datePublication: Date;

  @Column({
    name: 'date_evenement',
    type: 'datetime',
    nullable: true,
  })
  dateEvenement: Date | null;

  @Column({
    name: 'id_cours',
    type: 'char',
    length: 36,
    nullable: true,
  })
  idCours: string | null;

  @Column({
    name: 'id_classe',
    type: 'char',
    length: 36,
    nullable: true,
  })
  idClasse: string | null;

  @Column({
    name: 'id_auteur',
    type: 'char',
    length: 36,
  })
  idAuteur: string;

  @ManyToOne(
    () => Cours,
    (cours) => cours.annonces,
    {
      nullable: true,
      onDelete: 'CASCADE',
    },
  )
  @JoinColumn({
    name: 'id_cours',
  })
  cours: Cours | null;


  @ManyToOne(() => Classe, { nullable: true, onDelete: 'CASCADE' })
  @JoinColumn({ name: 'id_classe' })
  classe: Classe | null;

  @ManyToOne(
    () => Utilisateur,
    (utilisateur) => utilisateur.annonces,
    {
      onDelete: 'CASCADE',
    },
  )
  @JoinColumn({
    name: 'id_auteur',
  })
  auteur: Utilisateur;
}