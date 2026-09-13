import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { JustificationsController } from './justifications.controller';
import { JustificationsService } from './justifications.service';
import { Justification } from '../entities/justification.entity';
import { Presence } from '../entities/presence.entity';
import { NotificationsModule } from '../notifications/notifications.module';
import { FileStorageService } from '../common/services/file-storage.service';

@Module({
  imports: [TypeOrmModule.forFeature([Justification, Presence]), NotificationsModule],
  controllers: [JustificationsController],
  providers: [JustificationsService, FileStorageService],
  exports: [JustificationsService],
})
export class JustificationsModule {}
