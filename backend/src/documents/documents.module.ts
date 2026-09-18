import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CommonModule } from '../common/common.module';
import { Document } from '../entities/document.entity';
import { DocumentsController } from './documents.controller';
import { DocumentsService } from './documents.service';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Etablissement } from '../entities/etablissement.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Document, Gestionnaire, Etablissement]), CommonModule],
  controllers: [DocumentsController],
  providers: [DocumentsService],
  exports: [DocumentsService],
})
export class DocumentsModule {}
