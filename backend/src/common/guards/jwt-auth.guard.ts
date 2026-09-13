import { Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

/**
 * Vérifie la présence et la validité du jeton JWT (en-tête Authorization: Bearer <token>).
 * Cahier des charges - Phase 2, section 5.1 Authentification JWT.
 */
@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {}
