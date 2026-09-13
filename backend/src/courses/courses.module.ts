import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CoursesController } from './courses.controller';
import { CoursesService } from './courses.service';
import { Cours } from '../entities/cours.entity';
import { Affectation } from '../entities/affectation.entity';
import { Directeur } from '../entities/directeur.entity';
import { Etablissement } from '../entities/etablissement.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Cours, Affectation, Directeur, Etablissement])],
  controllers: [CoursesController],
  providers: [CoursesService],
  exports: [CoursesService],
})
export class CoursesModule {}
