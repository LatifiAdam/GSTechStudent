import { Injectable, Logger } from '@nestjs/common';
import * as nodemailer from 'nodemailer';

@Injectable()
export class EmailService {
  private readonly logger = new Logger(EmailService.name);

  async sendTwoFactorCode(to: string, code: string) {
    const host = process.env.SMTP_HOST;
    const port = Number(process.env.SMTP_PORT || 587);
    const user = process.env.SMTP_USER;
    const pass = process.env.SMTP_PASSWORD;
    const from = process.env.SMTP_FROM || user;

    if (!host || !user || !pass || !from) {
      this.logger.warn('SMTP non configuré: impossible d’envoyer le code 2FA. Configurez SMTP_HOST/SMTP_USER/SMTP_PASSWORD/SMTP_FROM.');
      throw new Error('Service email 2FA non configuré');
    }

    const transporter = nodemailer.createTransport({
      host,
      port,
      secure: port === 465,
      auth: { user, pass },
    });

    await transporter.sendMail({
      from,
      to,
      subject: 'Votre code de vérification GSTech',
      text: `Votre code de vérification GSTech est ${code}. Il expire dans 10 minutes.`,
      html: `<p>Votre code de vérification GSTech est :</p><h2>${code}</h2><p>Il expire dans 10 minutes.</p>`,
    });
  }
}
