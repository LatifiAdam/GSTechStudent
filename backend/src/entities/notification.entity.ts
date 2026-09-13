import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { Utilisateur } from './utilisateur.entity';

@Entity('notification')
export class Notification {
  @PrimaryGeneratedColumn('uuid', { name: 'id_notification' })
  idNotification: string;

  @Column({ type: 'varchar', length: 50 })
  type: string;

  @Column({ type: 'text' })
  message: string;

  @Column({ type: 'boolean', default: false })
  lue: boolean;

  @CreateDateColumn({ name: 'date_envoi', type: 'datetime' })
  dateEnvoi: Date;

  @Column({ name: 'id_utilisateur', type: 'char', length: 36 })
  idUtilisateur: string;

  // Relation

  @ManyToOne(() => Utilisateur, (utilisateur) => utilisateur.notifications, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'id_utilisateur' })
  utilisateur: Utilisateur;
}
