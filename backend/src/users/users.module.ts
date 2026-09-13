import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UsersController } from './users.controller';
import { UsersService } from './users.service';
import { Utilisateur } from '../entities/utilisateur.entity';
import { Formateur } from '../entities/formateur.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Administrateur } from '../entities/administrateur.entity';
import { Directeur } from '../entities/directeur.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Etablissement } from '../entities/etablissement.entity';
import { AuditLog } from '../entities/audit-log.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Utilisateur, Formateur, stagiaire, Administrateur, Directeur, Gestionnaire, Etablissement, AuditLog])],
  controllers: [UsersController],
  providers: [UsersService],
  exports: [UsersService],
})
export class UsersModule {}
