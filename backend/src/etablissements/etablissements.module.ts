import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Etablissement } from '../entities/etablissement.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Directeur } from '../entities/directeur.entity';
import { Utilisateur } from '../entities/utilisateur.entity';
import { EtablissementsController } from './etablissements.controller';
import { EtablissementsService } from './etablissements.service';
@Module({imports:[TypeOrmModule.forFeature([Etablissement,Gestionnaire,Directeur,Utilisateur])],controllers:[EtablissementsController],providers:[EtablissementsService],exports:[EtablissementsService]})
export class EtablissementsModule {}
