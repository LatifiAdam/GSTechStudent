import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Document } from '../entities/document.entity';
import { DocumentsController } from './documents.controller';
import { DocumentsService } from './documents.service';
import { FileStorageService } from '../common/services/file-storage.service';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Etablissement } from '../entities/etablissement.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Document, Gestionnaire, Etablissement])],
  controllers: [DocumentsController],
  providers: [DocumentsService, FileStorageService],
  exports: [DocumentsService],
})
export class DocumentsModule {}
