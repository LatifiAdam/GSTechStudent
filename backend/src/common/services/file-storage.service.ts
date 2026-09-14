import { Injectable, OnModuleInit } from '@nestjs/common';
import { promises as fs } from 'fs';
import * as path from 'path';
import { createHash, createHmac } from 'crypto';

/**
 * Unified file storage abstraction.
 *
 * STORAGE_DRIVER=local (default): keeps the historical filesystem behavior.
 * STORAGE_DRIVER=s3: stores objects in an S3-compatible service such as MinIO.
 * MySQL stores only the returned storage key/path.
 */
@Injectable()
export class FileStorageService implements OnModuleInit {
  private readonly driver = (process.env.STORAGE_DRIVER || 'local').toLowerCase();
  private readonly baseDir = process.env.UPLOAD_DIR || './uploads';

  private readonly endpoint = process.env.STORAGE_ENDPOINT || 'http://localhost:9000';
  private readonly accessKey = process.env.STORAGE_ACCESS_KEY || '';
  private readonly secretKey = process.env.STORAGE_SECRET_KEY || '';
  private readonly bucket = process.env.STORAGE_BUCKET || 'gstech';
  private readonly region = process.env.STORAGE_REGION || 'us-east-1';

  async onModuleInit(): Promise<void> {
    if (this.driver !== 's3') return;
    if (!this.accessKey || !this.secretKey) {
      throw new Error('STORAGE_ACCESS_KEY and STORAGE_SECRET_KEY are required when STORAGE_DRIVER=s3');
    }
    await this.ensureBucket();
  }

  async save(subdir: string, filename: string, buffer: Buffer, contentType?: string): Promise<string> {
    const cleanSubdir = subdir.replace(/^\/+|\/+$/g, '');
    const cleanFilename = filename.replace(/\\/g, '/').split('/').pop() || 'file';
    const key = [cleanSubdir, cleanFilename].filter(Boolean).join('/');

    if (this.driver === 's3') {
      await this.s3Request('PUT', key, buffer, contentType || this.contentTypeFromFilename(cleanFilename));
      return key;
    }

    const dir = path.join(this.baseDir, cleanSubdir);
    await fs.mkdir(dir, { recursive: true });
    const fullPath = path.join(dir, cleanFilename);
    await fs.writeFile(fullPath, buffer);
    return fullPath;
  }

  async read(storagePath: string): Promise<Buffer> {
    if (!storagePath) throw new Error('Fichier introuvable');

    if (this.driver === 's3') {
      try {
        return await this.s3RequestBuffer('GET', this.normalizeS3Key(storagePath));
      } catch (error) {
        // Optional compatibility with files created by older local-storage versions.
        if ((process.env.STORAGE_FALLBACK_LOCAL || 'true').toLowerCase() === 'true') {
          try {
            return await fs.readFile(storagePath);
          } catch (_) {
            throw error;
          }
        }
        throw error;
      }
    }

    return fs.readFile(storagePath);
  }

  async remove(storagePath: string | null | undefined): Promise<void> {
    if (!storagePath) return;

    if (this.driver === 's3') {
      try {
        await this.s3Request('DELETE', this.normalizeS3Key(storagePath));
        return;
      } catch (error) {
        if ((process.env.STORAGE_FALLBACK_LOCAL || 'true').toLowerCase() === 'true') {
          await fs.rm(storagePath, { force: true }).catch(() => undefined);
          return;
        }
        throw error;
      }
    }

    await fs.rm(storagePath, { force: true });
  }

  private normalizeS3Key(value: string): string {
    return value
      .replace(/^\/+/, '')
      .replace(new RegExp(`^${this.escapeRegExp(this.bucket)}/`), '');
  }

  private escapeRegExp(value: string): string {
    return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  private contentTypeFromFilename(filename: string): string {
    const ext = path.extname(filename).toLowerCase();
    if (ext === '.pdf') return 'application/pdf';
    if (ext === '.png') return 'image/png';
    if (ext === '.jpg' || ext === '.jpeg') return 'image/jpeg';
    if (ext === '.webp') return 'image/webp';
    return 'application/octet-stream';
  }

  private async ensureBucket(): Promise<void> {
    const response = await this.s3RequestRaw('HEAD', '');
    if (response.status === 200) return;
    if (response.status !== 404) {
      const body = await response.text();
      throw new Error(`Impossible de vérifier le bucket ${this.bucket}: HTTP ${response.status} ${body.slice(0, 500)}`);
    }

    const create = await this.s3RequestRaw('PUT', '');
    if (![200, 201, 204, 409].includes(create.status)) {
      const body = await create.text();
      throw new Error(`Impossible de créer le bucket ${this.bucket}: HTTP ${create.status} ${body.slice(0, 500)}`);
    }
  }

  private async s3Request(
    method: string,
    key: string,
    body?: Buffer,
    contentType?: string,
  ): Promise<void> {
    const response = await this.s3RequestRaw(method, key, body, contentType);
    if (!response.ok && response.status !== 204) {
      const errorBody = await response.text().catch(() => '');
      throw new Error(`Stockage objet: HTTP ${response.status} ${errorBody.slice(0, 500)}`);
    }
  }

  private async s3RequestBuffer(
    method: string,
    key: string,
    contentType?: string,
  ): Promise<Buffer> {
    const response = await this.s3RequestRaw(method, key, undefined, contentType);
    if (!response.ok) {
      const errorBody = await response.text().catch(() => '');
      throw new Error(`Stockage objet: HTTP ${response.status} ${errorBody.slice(0, 500)}`);
    }
    return Buffer.from(await response.arrayBuffer());
  }

  private async s3RequestRaw(
    method: string,
    key: string,
    body?: Buffer,
    contentType?: string,
  ): Promise<Response> {
    const endpoint = new URL(this.endpoint);
    const encodedKey = key
      ? '/' + key.split('/').map((part) => encodeURIComponent(part)).join('/')
      : '';
    const canonicalUri = `/${encodeURIComponent(this.bucket)}${encodedKey}`;
    const url = new URL(`${endpoint.origin}${canonicalUri}`);

    const payload = body ?? Buffer.alloc(0);
    const payloadHash = createHash('sha256').update(payload).digest('hex');
    const now = new Date();
    const amzDate = now.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}Z$/, 'Z');
    const shortDate = amzDate.slice(0, 8);
    const host = endpoint.host;

    const headers: Record<string, string> = {
      host,
      'x-amz-content-sha256': payloadHash,
      'x-amz-date': amzDate,
    };
    if (contentType) headers['content-type'] = contentType;

    const canonicalHeaders = Object.keys(headers)
      .sort()
      .map((name) => `${name.toLowerCase()}:${headers[name].trim()}\n`)
      .join('');
    const signedHeaders = Object.keys(headers)
      .sort()
      .map((name) => name.toLowerCase())
      .join(';');

    const canonicalRequest = [
      method,
      canonicalUri,
      '',
      canonicalHeaders,
      signedHeaders,
      payloadHash,
    ].join('\n');

    const scope = `${shortDate}/${this.region}/s3/aws4_request`;
    const stringToSign = [
      'AWS4-HMAC-SHA256',
      amzDate,
      scope,
      createHash('sha256').update(canonicalRequest).digest('hex'),
    ].join('\n');

    const kDate = createHmac('sha256', `AWS4${this.secretKey}`).update(shortDate).digest();
    const kRegion = createHmac('sha256', kDate).update(this.region).digest();
    const kService = createHmac('sha256', kRegion).update('s3').digest();
    const kSigning = createHmac('sha256', kService).update('aws4_request').digest();
    const signature = createHmac('sha256', kSigning).update(stringToSign).digest('hex');

    const authorization =
      `AWS4-HMAC-SHA256 Credential=${this.accessKey}/${scope}, ` +
      `SignedHeaders=${signedHeaders}, Signature=${signature}`;

    const requestHeaders: Record<string, string> = {
      ...headers,
      Authorization: authorization,
    };

    return fetch(url, {
  method,
  headers: requestHeaders,
  body: ['GET', 'HEAD', 'DELETE'].includes(method)
    ? undefined
    : (payload as unknown as BodyInit),
});
  }
}
