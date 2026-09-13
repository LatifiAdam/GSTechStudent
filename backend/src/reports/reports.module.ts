import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';

import { ReportsController } from './reports.controller';
import { ReportsService } from './reports.service';

import { Appel } from '../entities/appel.entity';
import { Presence } from '../entities/presence.entity';
import { Justification } from '../entities/justification.entity';
import { DemandeDocument } from '../entities/demande-document.entity';
import { Annonce } from '../entities/annonce.entity';
import { Affectation } from '../entities/affectation.entity';
import { stagiaire } from '../entities/stagiaire.entity';

import { PdfService } from '../common/services/pdf.service';
import { ExcelService } from '../common/services/excel.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      Appel,
      Presence,
      Justification,
      DemandeDocument,
      Annonce,
      Affectation,
      stagiaire,
    ]),
  ],

  controllers: [
    ReportsController,
  ],

  providers: [
    ReportsService,
    PdfService,
    ExcelService,
  ],

  exports: [
    ReportsService,
  ],
})
export class ReportsModule {}