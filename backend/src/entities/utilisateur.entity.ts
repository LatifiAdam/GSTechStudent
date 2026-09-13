import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  OneToOne,
  OneToMany,
} from 'typeorm';

import { Formateur } from './formateur.entity';
import { stagiaire } from './stagiaire.entity';
import { Administrateur } from './administrateur.entity';
import { Directeur } from './directeur.entity';
import { Gestionnaire } from './gestionnaire.entity';
import { Role } from '../common/enums/roles.enum';
import { Notification } from './notification.entity';
import { Annonce } from './annonce.entity';

@Entity('utilisateur')
export class Utilisateur {
  @PrimaryGeneratedColumn('uuid', {
    name: 'id_utilisateur',
  })
  idUtilisateur: string;

  @Column({
    type: 'varchar',
    length: 100,
  })
  nom: string;

  @Column({
    type: 'varchar',
    length: 100,
  })
  prenom: string;

  @Column({
    type: 'varchar',
    length: 255,
    unique: true,
  })
  email: string;

  @Column({
    type: 'enum',
    enum: Role,
    default: Role.stagiaire,
  })
  role: Role;

  @Column({ name: 'region', type: 'varchar', length: 120, nullable: true, })
  region: string | null;

  @Column({ name: 'is_active', type: 'boolean', default: true, })
  isActive: boolean;

  @Column({
    name: 'is_bootstrap',
    type: 'boolean',
    default: false,
  })
  isBootstrap: boolean;

  @Column({
    name: 'mot_de_passe',
    type: 'varchar',
    length: 255,
    select: false,
  })
  motDePasse: string;

  @CreateDateColumn({
    name: 'date_creation',
    type: 'datetime',
  })
  dateCreation: Date;

  @Column({
    name: 'cin',
    type: 'varchar',
    length: 20,
    nullable: true,
    unique: true,
  })
  cin: string | null;

  @Column({
    name: 'telephone',
    type: 'varchar',
    length: 30,
    nullable: true,
  })
  telephone: string | null;

  @Column({
    name: 'adresse',
    type: 'varchar',
    length: 255,
    nullable: true,
  })
  adresse: string | null;

  @Column({
    name: 'two_factor_enabled',
    type: 'boolean',
    default: false,
  })
  twoFactorEnabled: boolean;

  @Column({
    name: 'two_factor_code_hash',
    type: 'varchar',
    length: 255,
    nullable: true,
    select: false,
  })
  twoFactorCodeHash: string | null;

  @Column({
    name: 'two_factor_code_expires_at',
    type: 'datetime',
    nullable: true,
    select: false,
  })
  twoFactorCodeExpiresAt: Date | null;

  @OneToOne(
    () => Formateur,
    (formateur) => formateur.utilisateur,
  )
  formateur: Formateur;

  @OneToOne(
    () => stagiaire,
    (stagiaire) => stagiaire.utilisateur,
  )
  stagiaire: stagiaire;

  @OneToOne(
    () => Administrateur,
    (administrateur) => administrateur.utilisateur,
  )
  administrateur: Administrateur;

  @OneToOne(() => Directeur, (directeur) => directeur.utilisateur)
  directeur: Directeur;

  @OneToOne(() => Gestionnaire, (gestionnaire) => gestionnaire.utilisateur)
  gestionnaire: Gestionnaire;

  @OneToMany(
    () => Notification,
    (notification) => notification.utilisateur,
  )
  notifications: Notification[];

  @OneToMany(
    () => Annonce,
    (annonce) => annonce.auteur,
  )
  annonces: Annonce[];
}