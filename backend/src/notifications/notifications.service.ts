import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

import { Notification } from '../entities/notification.entity';
import { DeviceToken } from '../entities/device-token.entity';

import { FirebaseService } from '../common/services/firebase.service';

import { RegisterDeviceTokenDto } from './dto/register-device-token.dto';

@Injectable()
export class NotificationsService {
  constructor(
    @InjectRepository(Notification)
    private readonly notificationRepo: Repository<Notification>,

    @InjectRepository(DeviceToken)
    private readonly deviceTokenRepo: Repository<DeviceToken>,

    private readonly firebase: FirebaseService,
  ) {}

  // ============================================================
  // GET /notifications
  // ============================================================

  async findAll(userId: string) {
    return this.notificationRepo.find({
      where: {
        utilisateur: {
          idUtilisateur: userId,
        },
      },

      order: {
        dateEnvoi: 'DESC',
      },
    });
  }

  // ============================================================
  // PATCH /notifications/:id/read
  // ============================================================

  async markAsRead(
    id: string,
    userId: string,
  ) {
    const notif =
      await this.notificationRepo.findOne({
        where: {
          idNotification: id,
        },

        relations: [
          'utilisateur',
        ],
      });

    if (
      !notif ||
      notif.utilisateur.idUtilisateur !==
        userId
    ) {
      throw new Error(
        'Notification introuvable ou non autorisée',
      );
    }

    notif.lue = true;

    return this.notificationRepo.save(
      notif,
    );
  }

  // ============================================================
  // POST /notifications/device-tokens
  // ============================================================

  async registerToken(
    dto: RegisterDeviceTokenDto,
    userId: string,
  ) {
    let deviceToken =
      await this.deviceTokenRepo.findOne({
        where: {
          token: dto.token,
        },
      });

    if (deviceToken) {
      deviceToken.plateforme =
        dto.plateforme;

      deviceToken.utilisateur = {
        idUtilisateur: userId,
      } as any;
    } else {
      deviceToken =
        this.deviceTokenRepo.create({
          token: dto.token,

          plateforme:
            dto.plateforme,

          utilisateur: {
            idUtilisateur: userId,
          } as any,
        });
    }

    return this.deviceTokenRepo.save(
      deviceToken,
    );
  }

  // ============================================================
  // DELETE /notifications/device-tokens/:token
  // ============================================================

  async removeToken(
    token: string,
    userId: string,
  ) {
    const result =
      await this.deviceTokenRepo.delete({
        token,

        utilisateur: {
          idUtilisateur: userId,
        } as any,
      });

    return {
      success:
        (result.affected ?? 0) > 0,
    };
  }

  // ============================================================
  // CREATE + PUSH NOTIFICATION
  // ============================================================

  async notifyUsers(
    userIds: string[],
    type: string,
    message: string,
  ) {
    const notifications =
      userIds.map((id) =>
        this.notificationRepo.create({
          type,

          message,

          lue: false,

          utilisateur: {
            idUtilisateur: id,
          } as any,
        }),
      );

    await this.notificationRepo.save(
      notifications,
    );

    const tokens =
      await this.deviceTokenRepo.find({
        where: userIds.map(
          (id) => ({
            utilisateur: {
              idUtilisateur: id,
            } as any,
          }),
        ),
      });

    if (tokens.length > 0) {
      await this.firebase.sendToTokens(
        tokens.map(
          (token) => token.token,
        ),

        type,

        message,
      );
    }

    return {
      created:
        notifications.length,

      pushed:
        tokens.length,
    };
  }
}