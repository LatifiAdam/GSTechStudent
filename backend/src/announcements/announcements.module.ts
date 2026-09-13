// src/announcements/announcements.module.ts

import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';

import { AnnouncementsController } from './announcements.controller';
import { AnnouncementsService } from './announcements.service';

import { Annonce } from '../entities/annonce.entity';
import { Cours } from '../entities/cours.entity';
import { Affectation } from '../entities/affectation.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Utilisateur } from '../entities/utilisateur.entity';

import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      Annonce,
      Cours,
      Affectation,
      stagiaire,
      Utilisateur,
    ]),
    NotificationsModule,
  ],

  controllers: [
    AnnouncementsController,
  ],

  providers: [
    AnnouncementsService,
  ],

  exports: [
    AnnouncementsService,
  ],
})
export class AnnouncementsModule {}