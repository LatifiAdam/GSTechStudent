import { Injectable, UnauthorizedException } from '@nestjs/common';
import { randomInt } from 'crypto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';

import { Utilisateur } from '../entities/utilisateur.entity';
import { Formateur } from '../entities/formateur.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Administrateur } from '../entities/administrateur.entity';
import { Directeur } from '../entities/directeur.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Role } from '../common/enums/roles.enum';
import { LoginDto } from './dto/login.dto';
import { VerifyTwoFactorDto } from './dto/verify-two-factor.dto';
import { EmailService } from '../common/services/email.service';

@Injectable()
export class AuthService {
  /**
   * Liste noire des refresh tokens révoqués.
   *
   * Les tokens sont conservés uniquement en mémoire.
   *
   * Limitations :
   * - perdus au redémarrage de l'application ;
   * - non partagés entre plusieurs instances de l'API.
   *
   * Pour une application en production, utiliser Redis
   * ou une base de données pour stocker les Refresh Tokens.
   */
  private readonly revokedRefreshTokens = new Set<string>();

  constructor(
    @InjectRepository(Utilisateur)
    private readonly utilisateurRepo: Repository<Utilisateur>,

    @InjectRepository(Formateur)
    private readonly formateurRepo: Repository<Formateur>,

    @InjectRepository(stagiaire)
    private readonly stagiaireRepo: Repository<stagiaire>,

    @InjectRepository(Administrateur)
    private readonly administrateurRepo: Repository<Administrateur>,

    @InjectRepository(Directeur)
    private readonly directeurRepo: Repository<Directeur>,

    @InjectRepository(Gestionnaire)
    private readonly gestionnaireRepo: Repository<Gestionnaire>,

    private readonly jwtService: JwtService,

    private readonly emailService: EmailService,
  ) {}

  /**
   * Détermine le rôle réel de l'utilisateur.
   */
  private async resolveRole(idUtilisateur: string): Promise<Role> {
    const user = await this.utilisateurRepo.findOne({ where: { idUtilisateur } });
    if (!user) {
      throw new UnauthorizedException('Utilisateur sans rôle associé (compte incomplet)');
    }

    // The role-specific tables are the authoritative source. This prevents
    // an old/stale utilisateur.role value from sending an stagiaire to the
    // ADMIN area (or vice versa) after migrations.
    const genericRoles = [Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ];
    if (genericRoles.includes(user.role as Role)) return user.role as Role;

    const [administrateur, directeur, gestionnaire, formateur, stagiaire] =
      await Promise.all([
        this.administrateurRepo.findOne({ where: { idUtilisateur } }),
        this.directeurRepo.findOne({ where: { idUtilisateur } }),
        this.gestionnaireRepo.findOne({ where: { idUtilisateur } }),
        this.formateurRepo.findOne({ where: { idUtilisateur } }),
        this.stagiaireRepo.findOne({ where: { idUtilisateur } }),
      ]);

    // A user must belong to exactly one role-specific table. The order here
    // only resolves corrupted legacy data; normal accounts match one table.
    const matches = [
      administrateur ? Role.SUPER_ADMIN : null,
      directeur ? Role.DIRECTEUR : null,
      gestionnaire ? Role.GESTIONNAIRE : null,
      formateur ? Role.FORMATEUR : null,
      stagiaire ? Role.stagiaire : null,
    ].filter((r): r is Role => r !== null);

    if (matches.length === 0) {
      throw new UnauthorizedException('Utilisateur sans rôle associé (compte incomplet)');
    }

    if (matches.length > 1) {
      // Prefer the explicit role only when it is one of the matching tables.
      // Otherwise use the safest deterministic fallback and repair the column.
      const explicit = user.role as Role | null | undefined;
      if (explicit && matches.includes(explicit)) {
        user.role = explicit;
        await this.utilisateurRepo.save(user);
        return explicit;
      }
    }

    const effectiveRole = matches[0];
    if (user.role !== effectiveRole) {
      user.role = effectiveRole;
      await this.utilisateurRepo.save(user);
    }
    return effectiveRole;
  }

  /**
   * Authentification utilisateur
   */
  async login(dto: LoginDto) {
    const user = await this.utilisateurRepo
      .createQueryBuilder('u')
      .addSelect('u.motDePasse')
      .where('u.email = :email', {
        email: dto.email,
      })
      .getOne();

    if (!user) {
      throw new UnauthorizedException('Identifiants invalides');
    }
    if (!user.isActive) {
      throw new UnauthorizedException('Compte désactivé');
    }

    // Décommente si ton entité possède un champ estActif
    /*
    if (!user.estActif) {
      throw new UnauthorizedException('Compte désactivé');
    }
    */

    const passwordValid = await bcrypt.compare(
      dto.password,
      user.motDePasse,
    );

    if (!passwordValid) {
      throw new UnauthorizedException('Identifiants invalides');
    }

    const role = await this.resolveRole(user.idUtilisateur);

    const payload = {
      sub: user.idUtilisateur,
      email: user.email,
      role,
    };

    // 2FA is disabled by default. When enabled, do not issue session tokens
    // until the email code is successfully verified.
    if (user.twoFactorEnabled) {
      const code = String(randomInt(100000, 1000000));
      user.twoFactorCodeHash = await bcrypt.hash(code, 10);
      user.twoFactorCodeExpiresAt = new Date(Date.now() + 10 * 60 * 1000);
      await this.utilisateurRepo.save(user);

      const challengeToken = this.jwtService.sign(
        { ...payload, tokenType: '2fa_challenge' },
        { expiresIn: '10m' },
      );
      await this.emailService.sendTwoFactorCode(user.email, code);
      return { requiresTwoFactor: true, challengeToken };
    }

    return { ...this.issueTokens(payload), requiresTwoFactor: false };
  }

  private issueTokens(payload: { sub: string; email: string; role: string }) {
    return {
      accessToken: this.jwtService.sign({ ...payload, tokenType: 'access' }, { expiresIn: '15m' }),
      refreshToken: this.jwtService.sign({ ...payload, tokenType: 'refresh' }, { expiresIn: '7d' }),
    };
  }

  async verifyTwoFactor(dto: VerifyTwoFactorDto) {
    let payload: any;
    try {
      payload = this.jwtService.verify(dto.challengeToken);
    } catch {
      throw new UnauthorizedException('Code de vérification expiré ou session invalide');
    }
    if (payload.tokenType !== '2fa_challenge') throw new UnauthorizedException('Challenge 2FA invalide');

    const user = await this.utilisateurRepo.createQueryBuilder('u')
      .addSelect(['u.twoFactorCodeHash', 'u.twoFactorCodeExpiresAt'])
      .where('u.id_utilisateur = :id', { id: payload.sub })
      .getOne();
    if (!user || !user.twoFactorEnabled || !user.twoFactorCodeHash || !user.twoFactorCodeExpiresAt) {
      throw new UnauthorizedException('Vérification 2FA indisponible');
    }
    if (user.twoFactorCodeExpiresAt.getTime() < Date.now()) throw new UnauthorizedException('Code de vérification expiré');
    if (!(await bcrypt.compare(dto.code, user.twoFactorCodeHash))) throw new UnauthorizedException('Code de vérification incorrect');

    user.twoFactorCodeHash = null;
    user.twoFactorCodeExpiresAt = null;
    await this.utilisateurRepo.save(user);
    const role = await this.resolveRole(user.idUtilisateur);
    return { ...this.issueTokens({ sub: user.idUtilisateur, email: user.email, role }), requiresTwoFactor: false };
  }

  /**
   * Renouvellement du token d'accès.
   *
   * Rotation des Refresh Tokens : chaque appel à /auth/refresh invalide
   * immédiatement le refresh token utilisé et en émet un nouveau. Cela
   * limite la fenêtre d'exploitation d'un refresh token volé — un jeton
   * rejoué après usage légitime est automatiquement rejeté.
   */
  async refresh(refreshToken: string) {
    if (this.revokedRefreshTokens.has(refreshToken)) {
      throw new UnauthorizedException('Refresh token révoqué');
    }

    try {
      const payload = this.jwtService.verify(refreshToken);
      if (payload.tokenType !== 'refresh') {
        throw new UnauthorizedException('Refresh token invalide');
      }
      this.revokedRefreshTokens.add(refreshToken);

      const user = await this.utilisateurRepo.findOne({
        where: { idUtilisateur: payload.sub },
      });
      if (!user) {
        throw new UnauthorizedException('Utilisateur introuvable');
      }
      const role = await this.resolveRole(user.idUtilisateur);
      const newPayload = {
        sub: user.idUtilisateur,
        email: user.email,
        role,
      };

      const accessToken = this.jwtService.sign({ ...newPayload, tokenType: 'access' }, {
        expiresIn: '15m',
      });

      const newRefreshToken = this.jwtService.sign({ ...newPayload, tokenType: 'refresh' }, {
        expiresIn: '7d',
      });

      return {
        accessToken,
        refreshToken: newRefreshToken,
      };
    } catch {
      throw new UnauthorizedException(
        'Refresh token invalide ou expiré',
      );
    }
  }

  /**
   * Déconnexion.
   */
  async logout(refreshToken: string) {
    if (refreshToken) {
      this.revokedRefreshTokens.add(refreshToken);
    }

    return {
      success: true,
    };
  }

  /**
   * Hachage d'un mot de passe.
   */
  async hashPassword(plain: string): Promise<string> {
    return bcrypt.hash(plain, 12);
  }
}
