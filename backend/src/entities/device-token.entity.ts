import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { Utilisateur } from './utilisateur.entity';

export enum Plateforme {
  IOS = 'ios',
  ANDROID = 'android',
}

/**
 * Token FCM d'un appareil mobile (nouveau - complète le MCD/MLD de la Phase 1,
 * section 6.12 / RG13). Un utilisateur peut posséder plusieurs tokens (un par
 * appareil) ; la suppression du compte entraîne leur suppression en cascade.
 */
@Entity('device_token')
export class DeviceToken {
  @PrimaryGeneratedColumn('uuid', { name: 'id_device_token' })
  idDeviceToken: string;

  @Column({ type: 'varchar', length: 255, unique: true })
  token: string;

  @Column({ type: 'enum', enum: Plateforme })
  plateforme: Plateforme;

  @CreateDateColumn({ name: 'date_creation', type: 'datetime' })
  dateCreation: Date;

  @UpdateDateColumn({ name: 'date_derniere_utilisation', type: 'datetime' })
  dateDerniereUtilisation: Date;

  @Column({ name: 'id_utilisateur', type: 'char', length: 36 })
  idUtilisateur: string;

  @ManyToOne(() => Utilisateur, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'id_utilisateur' })
  utilisateur: Utilisateur;
}
