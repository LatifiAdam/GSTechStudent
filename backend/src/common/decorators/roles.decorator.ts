import { SetMetadata } from '@nestjs/common';
import { Role } from '../enums/roles.enum';

export const ROLES_KEY = 'roles';

/**
 * Usage: @Roles(Role.DF)
 * Restreint l'accès à l'endpoint aux rôles listés.
 * Voir cahier des charges section 4 / Phase 2 section 4 pour le mapping
 * endpoint -> rôle(s) autorisé(s).
 */
export const Roles = (...roles: Role[]) => SetMetadata(ROLES_KEY, roles);
