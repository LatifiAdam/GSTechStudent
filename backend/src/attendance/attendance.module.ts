import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';

import { AttendanceController } from './attendance.controller';
import { AttendanceService } from './attendance.service';

import { Appel } from '../entities/appel.entity';
import { Presence } from '../entities/presence.entity';
import { Creneau } from '../entities/creneau.entity';
import { Affectation } from '../entities/affectation.entity';
import { stagiaire } from '../entities/stagiaire.entity';

import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      Appel,
      Presence,
      Creneau,
      Affectation,
      stagiaire,
    ]),

    NotificationsModule,
  ],

  controllers: [
    AttendanceController,
  ],

  providers: [
    AttendanceService,
  ],

  exports: [
    AttendanceService,
  ],
})
export class AttendanceModule {}