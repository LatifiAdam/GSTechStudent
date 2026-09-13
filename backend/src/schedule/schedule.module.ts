import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ScheduleController } from './schedule.controller';
import { ScheduleService } from './schedule.service';
import { Creneau } from '../entities/creneau.entity';
import { Affectation } from '../entities/affectation.entity';
import { Directeur } from '../entities/directeur.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Creneau, Affectation, Directeur])],
  controllers: [ScheduleController],
  providers: [ScheduleService],
  exports: [ScheduleService],
})
export class ScheduleModule {}
