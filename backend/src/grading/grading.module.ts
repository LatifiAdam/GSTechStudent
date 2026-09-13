import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { GradingController } from './grading.controller';
import { GradingService } from './grading.service';
import { Affectation } from '../entities/affectation.entity';
import { Note } from '../entities/note.entity';
import { stagiaire } from '../entities/stagiaire.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Affectation, Note, stagiaire])],
  controllers: [GradingController],
  providers: [GradingService],
})
export class GradingModule {}
