import { Injectable } from '@nestjs/common';
import * as PDFDocument from 'pdfkit';
import { existsSync } from 'fs';
import { join } from 'path';

/**
 * Génération de PDF (certificats de scolarité, relevés de notes,
 * attestations de réussite - Phase 1 section 4.8 ; export de rapports -
 * Phase 2 section 4.11).
 */
@Injectable()
export class PdfService {
  generateAttestationInscription(data: {
    nomComplet: string;
    dateNaissance: string;
    lieuNaissance: string;
    niveau: string;
    specialite: string;
    annee: string;
    numeroInscription: string;
    etablissement: string;
    poursuiteDepuis: string;
    ville: string;
    dateEdition: string;
  }): Promise<Buffer> {
    return new Promise((resolve, reject) => {
      const doc = new PDFDocument({ size: 'A4', margin: 0 });
      const chunks: Buffer[] = [];
      const template = join(process.cwd(), 'assets', 'attestation_inscription_ofppt_2025.jpg');
      doc.on('data', (chunk) => chunks.push(chunk));
      doc.on('end', () => resolve(Buffer.concat(chunks)));
      doc.on('error', reject);

      if (existsSync(template)) {
        doc.image(template, 0, 0, { width: doc.page.width, height: doc.page.height });
      } else {
        doc.rect(0, 0, doc.page.width, doc.page.height).fill('white');
      }

      // Mask the variable fields of the sample image, then print the personalized values.
      const scaleX = doc.page.width / 624;
      const scaleY = doc.page.height / 781;
      const mask = (x: number, y: number, w: number, h: number) => {
        doc.save();
        doc.fillColor('#ffffff').rect(x * scaleX, y * scaleY, w * scaleX, h * scaleY).fill();
        doc.restore();
      };
      const text = (value: string, x: number, y: number, size = 10) => {
        doc.font('Helvetica').fontSize(size).fillColor('#111111').text(value, x * scaleX, y * scaleY, { lineBreak: false });
      };

      mask(210, 222, 365, 32);
      mask(213, 268, 320, 28);
      mask(218, 321, 300, 25);
      mask(218, 348, 300, 28);
      mask(218, 378, 320, 28);
      mask(218, 404, 120, 26);
      mask(475, 404, 110, 28);
      mask(218, 433, 300, 27);
      mask(405, 433, 180, 28);
      mask(218, 463, 300, 27);
      mask(218, 493, 260, 28);
      mask(120, 520, 420, 28);
      mask(375, 575, 190, 25);
      mask(392, 601, 170, 25);

      text(data.etablissement || 'Établissement non renseigné', 215, 223, 8.7);
      text(data.nomComplet || '—', 220, 269, 10);
      text(data.dateNaissance || 'Non renseignée', 225, 322, 9);
      text(data.niveau || 'Technicien spécialisé', 225, 350, 9);
      text(data.specialite || 'Infrastructure Digitale', 225, 379, 9);
      text(data.annee || '1ère année', 225, 406, 9);
      text('Diplômante', 478, 406, 8.5);
      text('Résidentielle', 225, 434, 9);
      text(data.numeroInscription || '—', 225, 464, 9);
      text(data.annee || '—', 225, 494, 9);
      text(`Poursuit sa formation à l’établissement depuis : ${data.poursuiteDepuis || '—'}`, 118, 521, 8.5);
      text(data.ville || 'Rabat', 380, 576, 9);
      text(data.dateEdition || new Date().toLocaleDateString('fr-FR'), 396, 602, 9);

      doc.end();
    });
  }

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
