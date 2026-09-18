import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CommonModule } from '../common/common.module';
import { DocumentRequestsController } from './document-requests.controller';
import { DocumentRequestsService } from './document-requests.service';
import { DemandeDocument } from '../entities/demande-document.entity';
import { Document } from '../entities/document.entity';
import { NotificationsModule } from '../notifications/notifications.module';
import { PdfService } from '../common/services/pdf.service';
import { stagiaire } from '../entities/stagiaire.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';

@Module({
  imports: [TypeOrmModule.forFeature([DemandeDocument, Document, stagiaire, Gestionnaire]), NotificationsModule, CommonModule],
  controllers: [DocumentRequestsController],
  providers: [DocumentRequestsService, PdfService],
  exports: [DocumentRequestsService],
})
export class DocumentRequestsModule {}
