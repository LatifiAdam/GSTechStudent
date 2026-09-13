import { Injectable, UnauthorizedException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Utilisateur } from '../../entities/utilisateur.entity';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';

/**
 * Décode et valide le JWT signé lors de la connexion (AuthService.login).
 * Le payload retourné ici est injecté dans req.user et utilisé par RolesGuard.
 * Cahier des charges - Phase 2, section 5.1 Authentification JWT.
 */
@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(@InjectRepository(Utilisateur) private readonly utilisateurRepo: Repository<Utilisateur>) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: process.env.JWT_SECRET || 'change-me-in-.env',
      issuer: 'gstech-api',
      audience: 'gstech-mobile',
      algorithms: ['HS256'],
    });
  }

  async validate(payload: { sub: string; email: string; role: string; tokenType?: string }) {
    if (payload.tokenType !== 'access') {
      throw new UnauthorizedException('Jeton d’accès invalide');
    }
    const user = await this.utilisateurRepo.findOne({ where: { idUtilisateur: payload.sub } });
    if (!user || !user.isActive) throw new UnauthorizedException('Compte désactivé');
    return { userId: payload.sub, email: payload.email, role: String(user.role).toLowerCase() };
  }
}
