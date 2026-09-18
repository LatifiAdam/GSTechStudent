import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { APP_GUARD} from '@nestjs/core';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { CoursesModule } from './courses/courses.module';
import { ScheduleModule } from './schedule/schedule.module';
import { AttendanceModule } from './attendance/attendance.module';
import { JustificationsModule } from './justifications/justifications.module';
import { DocumentRequestsModule } from './document-requests/document-requests.module';
import { AnnouncementsModule } from './announcements/announcements.module';
import { NotificationsModule } from './notifications/notifications.module';
import { ReportsModule } from './reports/reports.module';
import { ClassesModule } from './classes/classes.module';
import { AffectationsModule } from './affectations/affectations.module';
import { GradingModule } from './grading/grading.module';
import { DocumentsModule } from './documents/documents.module';
import { EtablissementsModule } from './etablissements/etablissements.module';
import { BootstrapService } from './bootstrap.service';
import { CommonModule } from './common/common.module';

import {
  Utilisateur,
  Formateur,
  stagiaire,
  Administrateur,
  Directeur,
  Gestionnaire,
  Document,
  Classe,
  Cours,
  Affectation,
  Creneau,
  Appel,
  Presence,
  Justification,
  Notification,
  DemandeDocument,
  Annonce,
  DeviceToken,
  Note,
  Etablissement,
  AuditLog,
} from './entities';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      // Support local development from backend/ while keeping the project-level .env.
      // Docker uses environment variables from docker-compose.yml, which take precedence.
      envFilePath: ['.env', '../.env'],
    }),

    // Rate limiting global
    ThrottlerModule.forRoot([
      {
        ttl: 60000, // 1 minute
        limit: 100, // 100 requêtes par minute par IP
      },
    ]),

    TypeOrmModule.forRoot({
      type: 'mysql',
      host: process.env.DB_HOST || 'localhost',
      port: Number(process.env.DB_PORT) || 3306,
      username: process.env.DB_USER || 'root',
      password: process.env.DB_PASSWORD || '',
      database: process.env.DB_NAME || 'gestion_stagiaires',
      entities: [
          Utilisateur,
          Formateur,
          stagiaire,
          Administrateur,
          Directeur,
          Gestionnaire,
          Document,
          Classe,
          Cours,
          Affectation,
          Creneau,
          Appel,
          Presence,
          Justification,
          Notification,
          DemandeDocument,
          Annonce,
          DeviceToken,
          Note,
          Etablissement,
          AuditLog,
      ],
      synchronize: false, // le schéma est géré via schema.sql (Phase 2, section 2.2)
    }),

    TypeOrmModule.forFeature([AuditLog, Utilisateur, Administrateur]),

    AuthModule,
    UsersModule,
    CoursesModule,
    ScheduleModule,
    AttendanceModule,
    JustificationsModule,
    DocumentRequestsModule,
    AnnouncementsModule,
    NotificationsModule,
    ReportsModule,
    ClassesModule,
    AffectationsModule,
    GradingModule,
    DocumentsModule,
    EtablissementsModule,
    CommonModule,
  ],

  controllers: [AppController],

  providers: [
    AppService,
    BootstrapService,
    {
      provide: APP_GUARD,
      useClass: ThrottlerGuard,
    },
  ],
})
export class AppModule {}
