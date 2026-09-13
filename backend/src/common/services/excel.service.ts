import { Injectable } from '@nestjs/common';
import * as ExcelJS from 'exceljs';

/** Export de rapports au format Excel (Phase 2, section 4.11). */
@Injectable()
export class ExcelService {
  async generateSheet(sheetName: string, columns: { header: string; key: string; width?: number }[], rows: Record<string, any>[]): Promise<Buffer> {
    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet(sheetName);
    sheet.columns = columns;
    sheet.getRow(1).font = { bold: true };
    rows.forEach((row) => sheet.addRow(row));
    const buffer = await workbook.xlsx.writeBuffer();
    return Buffer.from(buffer);
  }
}
