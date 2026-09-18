import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CommonModule } from '../common/common.module';
import { JustificationsController } from './justifications.controller';
import { JustificationsService } from './justifications.service';
import { Justification } from '../entities/justification.entity';
import { Presence } from '../entities/presence.entity';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [TypeOrmModule.forFeature([Justification, Presence]), NotificationsModule, CommonModule],
  controllers: [JustificationsController],
  providers: [JustificationsService],
  exports: [JustificationsService],
})
export class JustificationsModule {}
