import {
  Entity,
  PrimaryColumn,
  Column,
  OneToOne,
  OneToMany,
  ManyToOne,
  JoinColumn,
} from 'typeorm';

import { Utilisateur } from './utilisateur.entity';
import { Classe } from './classe.entity';
import { Presence } from './presence.entity';
import { DemandeDocument } from './demande-document.entity';
import { Etablissement } from './etablissement.entity';

@Entity('stagiaire')
export class stagiaire {
  @PrimaryColumn('uuid', { name: 'id_utilisateur' })
  idUtilisateur: string;

  @Column({
    name: 'numero_stagiaire',
    type: 'varchar',
    length: 30,
    unique: true,
    nullable: true,
  })
  numerostagiaire: string | null;

  @Column({
    type: 'varchar',
    length: 50,
    nullable: true,
  })
  promotion: string | null;

  @Column({
    name: 'id_etablissement',
    type: 'char',
    length: 36,
    nullable: true,
  })
  idEtablissement: string | null;

  @ManyToOne(() => Etablissement, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'id_etablissement' })
  etablissement: Etablissement | null;

  @Column({
    name: 'id_classe',
    type: 'char',
    length: 36,
    nullable: true,
  })
  idClasse: string | null;

  @OneToOne(
    () => Utilisateur,
    (utilisateur) => utilisateur.stagiaire,
    { onDelete: 'CASCADE' },
  )
  @JoinColumn({ name: 'id_utilisateur' })
  utilisateur: Utilisateur;

  @ManyToOne(
    () => Classe,
    (classe) => classe.stagiaires,
    { onDelete: 'SET NULL', nullable: true },
  )
  @JoinColumn({ name: 'id_classe' })
  classe: Classe | null;

  @OneToMany(
    () => Presence,
    (presence) => presence.stagiaire,
  )
  presences: Presence[];

  @OneToMany(
    () => DemandeDocument,
    (demande) => demande.stagiaire,
  )
  demandesDocuments: DemandeDocument[];
}