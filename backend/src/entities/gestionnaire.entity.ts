import { Entity, PrimaryColumn, Column, OneToOne, JoinColumn, ManyToOne } from 'typeorm';
import { Utilisateur } from './utilisateur.entity';
import { Etablissement } from './etablissement.entity';

@Entity('gestionnaire')
export class Gestionnaire {
  @PrimaryColumn('uuid', { name: 'id_utilisateur' })
  idUtilisateur: string;

  @OneToOne(() => Utilisateur, (utilisateur) => utilisateur.gestionnaire, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'id_utilisateur' })
  utilisateur: Utilisateur;

  @Column({ name: 'id_etablissement', type: 'char', length: 36, nullable: true })
  idEtablissement: string | null;

  @ManyToOne(() => Etablissement, (etablissement) => etablissement.gestionnaires, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'id_etablissement' })
  etablissement: Etablissement | null;
}
