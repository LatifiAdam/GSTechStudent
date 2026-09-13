import {
  Entity,
  PrimaryColumn,
  Column,
  OneToOne,
  JoinColumn,
} from 'typeorm';

import { Utilisateur } from './utilisateur.entity';

@Entity('administrateur')
export class Administrateur {
  @PrimaryColumn('uuid', { name: 'id_utilisateur' })
  idUtilisateur: string;

  @Column({ name: 'niveau_acces', type: 'varchar', length: 30 })
  niveauAcces: string;

  // Relation avec la table utilisateur
  @OneToOne(() => Utilisateur, (utilisateur) => utilisateur.administrateur, {
    onDelete: 'CASCADE',
  })
  @JoinColumn({ name: 'id_utilisateur' })
  utilisateur: Utilisateur;
}
