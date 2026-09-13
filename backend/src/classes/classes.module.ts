import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Classe } from '../entities/classe.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Affectation } from '../entities/affectation.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Directeur } from '../entities/directeur.entity';
import { ClassesController } from './classes.controller';
import { ClassesService } from './classes.service';

@Module({
  imports: [TypeOrmModule.forFeature([Classe, stagiaire, Affectation, Gestionnaire, Directeur])],
  controllers: [ClassesController],
  providers: [ClassesService],
  exports: [ClassesService],
})
export class ClassesModule {}
