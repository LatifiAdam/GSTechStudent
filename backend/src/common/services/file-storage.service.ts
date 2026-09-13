import { Injectable } from '@nestjs/common';
import { promises as fs } from 'fs';
import * as path from 'path';

/**
 * Stockage local des fichiers (justificatifs uploadés, documents PDF générés).
 * En production, remplacer par un stockage objet (S3, GCS, etc.) : seule
 * cette classe serait à modifier, le reste du code manipule des chemins/URLs.
 */
@Injectable()
export class FileStorageService {
  private readonly baseDir = process.env.UPLOAD_DIR || './uploads';

  async save(subdir: string, filename: string, buffer: Buffer): Promise<string> {
    const dir = path.join(this.baseDir, subdir);
    await fs.mkdir(dir, { recursive: true });
    const fullPath = path.join(dir, filename);
    await fs.writeFile(fullPath, buffer);
    return fullPath;
  }

  async read(fullPath: string): Promise<Buffer> {
    return fs.readFile(fullPath);
  }

  async remove(fullPath: string | null | undefined): Promise<void> {
    if (!fullPath) return;
    await fs.rm(fullPath, { force: true });
  }
}
