import { Body, Controller, Delete, ForbiddenException, Get, Param, Patch, Post, Query, Req, UseGuards } from '@nestjs/common';
import { UsersService } from './users.service';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { QueryUsersDto } from './dto/query-users.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';
import { ChangePasswordDto } from './dto/change-password.dto';
import { ToggleTwoFactorDto } from './dto/toggle-two-factor.dto';

@Controller('users')
@UseGuards(JwtAuthGuard, RolesGuard)
export class UsersController {
  constructor(private readonly service: UsersService) {}

  // GET /users — Liste les utilisateurs, avec filtrage par rôle
  // Rôle(s) autorisé(s) : Administrateur
  @Roles(Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ, Role.DIRECTEUR, Role.GESTIONNAIRE)
  @Get()
  findAll(@Query() query: QueryUsersDto, @Req() req: any) {
    const allowedQueries: Record<string, string[]> = {
      [Role.SRIO]: [Role.GESTIONNAIRE],
      [Role.SCQ]: [Role.DIRECTEUR],
      [Role.DIRECTEUR]: [Role.GESTIONNAIRE, Role.FORMATEUR, Role.stagiaire],
      [Role.GESTIONNAIRE]: [Role.stagiaire, Role.FORMATEUR],
    };
    // Super Admin is the technical master account and may list every user type.
    // Other roles remain constrained by the role matrix below.
    const allowed = allowedQueries[req.user.role];
    if (allowed && query.role && !allowed.includes(query.role)) {
      throw new ForbiddenException('Ce rôle ne peut consulter que les utilisateurs de son périmètre fonctionnel');
    }
    return this.service.findAll(query, req.user.userId, req.user.role);
  }

  // GET /users/:id — Consulte le détail d'un utilisateur
  // Rôle(s) autorisé(s) : Administrateur, propriétaire
  @Get(':id')
  async findOne(@Param('id') id: string, @Req() req: any) {
    if (req.user.role === Role.FORMATEUR && req.user.userId !== id) {
      const allowed = await this.service.canFormateurViewStudent(req.user.userId, id);
      if (!allowed) throw new ForbiddenException('Vous ne pouvez consulter que les étudiants de vos classes');
    } else if ([Role.GESTIONNAIRE, Role.DIRECTEUR].includes(req.user.role as Role) && req.user.userId !== id) {
      const allowed = await this.service.canManageUserInEstablishment(req.user.userId, id, req.user.role);
      if (!allowed) throw new ForbiddenException('Utilisateur hors de votre établissement ou de votre périmètre');
    } else if (req.user.role !== Role.SUPER_ADMIN && req.user.role !== Role.DF && req.user.role !== Role.DIRECTEUR && req.user.role !== Role.GESTIONNAIRE && req.user.role !== Role.FORMATEUR && req.user.userId !== id) {
      throw new ForbiddenException('Vous ne pouvez consulter que votre propre profil');
    }
    return this.service.findOne(id);
  }

  // POST /users — Crée un utilisateur (formateur, étudiant ou administrateur)
  // Rôle(s) autorisé(s) : Administrateur
  @Roles(Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ, Role.DIRECTEUR, Role.GESTIONNAIRE)
  @Post()
  create(@Body() dto: CreateUserDto, @Req() req: any) {
    const allowed: Record<string, string[]> = {
      [Role.SUPER_ADMIN]: [Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ, Role.DIRECTEUR, Role.GESTIONNAIRE, Role.FORMATEUR, Role.stagiaire],
      [Role.DF]: [Role.SRIO, Role.SCQ],
      [Role.SRIO]: [Role.GESTIONNAIRE],
      [Role.SCQ]: [Role.DIRECTEUR],
      [Role.DIRECTEUR]: [Role.FORMATEUR],
      [Role.GESTIONNAIRE]: [Role.stagiaire],
    };
    if (!allowed[req.user.role]?.includes(dto.role)) throw new ForbiddenException('Ce rôle ne peut pas créer ce type de compte');
    return this.service.create(dto, req.user.role, req.user.userId);
  }

  // PATCH /users/:id — Modifie les informations d'un utilisateur
  // Rôle(s) autorisé(s) : Administrateur, propriétaire
  @Patch(':id')
  async update(@Param('id') id: string, @Body() dto: UpdateUserDto, @Req() req: any) {
    if ([Role.DIRECTEUR, Role.GESTIONNAIRE].includes(req.user.role as Role) && req.user.userId !== id) {
      const allowed = await this.service.canManageUserInEstablishment(req.user.userId, id, req.user.role);
      if (!allowed) throw new ForbiddenException('Modification non autorisée hors de votre établissement');
    } else if (req.user.role !== Role.SUPER_ADMIN && req.user.role !== Role.DF && req.user.role !== Role.DIRECTEUR && req.user.role !== Role.GESTIONNAIRE && req.user.userId !== id) {
      throw new ForbiddenException('Vous ne pouvez modifier que votre propre profil');
    }
    return this.service.update(id, dto);
  }

  @Patch(':id/two-factor')
  setTwoFactor(@Param('id') id: string, @Body() dto: ToggleTwoFactorDto, @Req() req: any) {
    if (req.user.role !== Role.SUPER_ADMIN && req.user.role !== Role.DF && req.user.role !== Role.DIRECTEUR && req.user.userId !== id) {
      throw new ForbiddenException('Vous ne pouvez modifier que votre propre paramètre 2FA');
    }
    return this.service.setTwoFactor(id, dto);
  }

  @Patch(':id/password')
  changePassword(@Param('id') id: string, @Body() dto: ChangePasswordDto, @Req() req: any) {
    if (req.user.role !== Role.SUPER_ADMIN && req.user.role !== Role.DF && req.user.role !== Role.DIRECTEUR && req.user.userId !== id) {
      throw new ForbiddenException('Vous ne pouvez modifier que votre propre mot de passe');
    }
    return this.service.changePassword(id, dto);
  }

  // DELETE /users/:id — Supprime un utilisateur
  // Rôle(s) autorisé(s) : Administrateur
  @Roles(Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ, Role.DIRECTEUR, Role.GESTIONNAIRE)
  @Delete(':id')
  async remove(@Param('id') id: string, @Req() req: any) {
    const target = await this.service.findOne(id);
    const allowed: Record<string, string[]> = {
      [Role.SUPER_ADMIN]: [Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ, Role.DIRECTEUR, Role.GESTIONNAIRE, Role.FORMATEUR, Role.stagiaire],
      [Role.DF]: [Role.SRIO, Role.SCQ],
      [Role.SRIO]: [Role.GESTIONNAIRE],
      [Role.SCQ]: [Role.DIRECTEUR],
      [Role.DIRECTEUR]: [Role.FORMATEUR],
      [Role.GESTIONNAIRE]: [Role.stagiaire],
    };
    if (!allowed[req.user.role]?.includes(target.role)) throw new ForbiddenException('Suppression non autorisée pour ce rôle');
    if (req.user.role === Role.SUPER_ADMIN && req.user.userId === id) {
      throw new ForbiddenException('Le Super Admin actuellement connecté ne peut pas supprimer son propre compte');
    }
    return this.service.remove(id, req.user.role, req.user.userId);
  }
}
