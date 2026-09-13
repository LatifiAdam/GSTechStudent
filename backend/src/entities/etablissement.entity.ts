import { Entity, PrimaryGeneratedColumn, Column, OneToOne, JoinColumn, OneToMany } from 'typeorm';
import { Directeur } from './directeur.entity';
import { Gestionnaire } from './gestionnaire.entity';

@Entity('etablissement')
export class Etablissement {
  @PrimaryGeneratedColumn('uuid', { name: 'id_etablissement' })
  idEtablissement: string;

  @Column({ name: 'nom_etablissement', type: 'varchar', length: 200 })
  nomEtablissement: string;

  @Column({ name: 'region', type: 'varchar', length: 120, nullable: true, })
  region: string | null;

  @Column({ name: 'id_directeur', type: 'char', length: 36, nullable: true, unique: true })
  idDirecteur: string | null;

  @OneToOne(() => Directeur, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'id_directeur', referencedColumnName: 'idUtilisateur' })
  directeur: Directeur | null;

  @OneToMany(() => Gestionnaire, (gestionnaire) => gestionnaire.etablissement)
  gestionnaires: Gestionnaire[];
}
