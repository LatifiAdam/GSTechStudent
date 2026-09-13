import { Entity, PrimaryColumn, OneToOne, JoinColumn } from 'typeorm';
import { Utilisateur } from './utilisateur.entity';
import { Etablissement } from './etablissement.entity';

@Entity('directeur')
export class Directeur {
  @PrimaryColumn('uuid', { name: 'id_utilisateur' })
  idUtilisateur: string;

  @OneToOne(() => Utilisateur, (utilisateur) => utilisateur.directeur, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'id_utilisateur' })
  utilisateur: Utilisateur;

  @OneToOne(() => Etablissement, (etablissement) => etablissement.directeur)
  etablissement: Etablissement | null;
}
