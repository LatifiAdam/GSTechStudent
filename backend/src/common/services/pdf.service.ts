import { Injectable } from '@nestjs/common';
import * as PDFDocument from 'pdfkit';

/**
 * Génération de PDF (certificats de scolarité, relevés de notes,
 * attestations de réussite - Phase 1 section 4.8 ; export de rapports -
 * Phase 2 section 4.11).
 */
@Injectable()
export class PdfService {
  generateDocument(title: string, lines: string[]): Promise<Buffer> {
    return new Promise((resolve, reject) => {
      const doc = new PDFDocument({ margin: 50 });
      const chunks: Buffer[] = [];
      doc.on('data', (chunk) => chunks.push(chunk));
      doc.on('end', () => resolve(Buffer.concat(chunks)));
      doc.on('error', reject);

      doc.fontSize(18).text(title, { align: 'center' });
      doc.moveDown(2);
      doc.fontSize(12);
      lines.forEach((line) => {
        doc.text(line, { align: 'left' });
        doc.moveDown();
      });
      doc.moveDown(2);
      doc.fontSize(10).text(`Document généré le ${new Date().toLocaleDateString('fr-FR')}`, {
        align: 'right',
      });
      doc.end();
    });
  }
}
