import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { cert, getApps, initializeApp, App } from 'firebase-admin/app';
import { getMessaging, Messaging } from 'firebase-admin/messaging';

/**
 * Firebase Cloud Messaging wrapper.
 *
 * Compatible with the current Firebase Admin SDK API.
 * Push notifications remain optional: if FIREBASE_* is not configured,
 * database notifications still work and the service stays in degraded mode.
 */
@Injectable()
export class FirebaseService implements OnModuleInit {
  private readonly logger = new Logger(FirebaseService.name);
  private enabled = false;
  private app?: App;
  private messaging?: Messaging;

  onModuleInit() {
    const projectId = process.env.FIREBASE_PROJECT_ID;
    const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
    const privateKey = process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n');

    if (!projectId || !clientEmail || !privateKey) {
      this.logger.warn(
        'Firebase non configure (FIREBASE_* absents dans .env) — les notifications push sont desactivees.',
      );
      return;
    }

    try {
      this.app =
        getApps()[0] ??
        initializeApp({
          credential: cert({ projectId, clientEmail, privateKey }),
        });

      this.messaging = getMessaging(this.app);
      this.enabled = true;
      this.logger.log('Firebase Cloud Messaging initialise.');
    } catch (err) {
      this.logger.error("Echec de l'initialisation de Firebase", err as Error);
    }
  }

  async sendToTokens(
    tokens: string[],
    title: string,
    body: string,
    data: Record<string, string> = {},
  ) {
    if (!this.enabled || !this.messaging || tokens.length === 0) {
      return { sent: 0, skipped: true };
    }

    try {
      // sendMulticast was replaced by sendEachForMulticast in current
      // Firebase Admin SDK versions.
      const res = await this.messaging.sendEachForMulticast({
        tokens,
        notification: { title, body },
        data,
      });

      return {
        sent: res.successCount,
        failed: res.failureCount,
        skipped: false,
      };
    } catch (err) {
      this.logger.error("Echec de l'envoi FCM", err as Error);
      return { sent: 0, failed: tokens.length, skipped: false, error: true };
    }
  }
}
