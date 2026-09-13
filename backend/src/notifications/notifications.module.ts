import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { NotificationsController } from './notifications.controller';
import { NotificationsService } from './notifications.service';
import { FirebaseService } from '../common/services/firebase.service';
import { Notification } from '../entities/notification.entity';
import { DeviceToken } from '../entities/device-token.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Notification, DeviceToken])],
  controllers: [NotificationsController],
  providers: [NotificationsService, FirebaseService],
  exports: [NotificationsService],
})
export class NotificationsModule {}
