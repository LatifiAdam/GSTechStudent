import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { ROLES_KEY } from '../decorators/roles.decorator';
import { Role } from '../enums/roles.enum';

/**
 * Vérifie que le rôle de l'utilisateur authentifié (req.user.role) figure
 * parmi les rôles autorisés déclarés via @Roles(...) sur le contrôleur/route.
 * Implémente les règles de gestion RG8 à RG12 (emploi du temps, documents, annonces).
 * Cahier des charges - Phase 2, section 5.3 Autorisation par rôle.
 */
@Injectable()
export class RolesGuard implements CanActivate {
  constructor(private reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    const requiredRoles = this.reflector.getAllAndOverride<Role[]>(ROLES_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }
    const { user } = context.switchToHttp().getRequest();
    const currentRole = typeof user?.role === 'string'
      ? user.role.toLowerCase() as Role
      : user?.role;
    return requiredRoles.includes(currentRole);
  }
}
