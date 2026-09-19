import { Body, Controller, Delete, ForbiddenException, Get, Param, Patch, Post, Query, Req, Res, UploadedFile, UseGuards, UseInterceptors, BadRequestException } from '@nestjs/common';
import { UsersService } from './users.service';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { QueryUsersDto } from './dto/query-users.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';
import { FileInterceptor } from '@nestjs/platform-express';
import { memoryStorage } from 'multer';
import { Response } from 'express';
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
  @Get('me/profile-image')
  async getMyProfileImage(@Req() req: any, @Res() res: Response) {
    const result = await this.service.getProfileImage(req.user.userId);
    res.set({
      'Content-Type': result.contentType,
      'Content-Disposition': 'inline',
      'Cache-Control': 'private, max-age=3600',
    });
    res.send(result.buffer);
  }

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

  @Post('me/profile-image')
  @UseInterceptors(FileInterceptor('file', {
    storage: memoryStorage(),
    limits: { fileSize: 2 * 1024 * 1024 },
  }))
  async uploadProfileImage(
    @UploadedFile() file: Express.Multer.File,
    @Req() req: any,
  ) {
    return this.service.updateProfileImage(req.user.userId, file);
  }


  @Post(':id/profile-image')
  @UseInterceptors(FileInterceptor('file', {
    storage: memoryStorage(),
    limits: { fileSize: 2 * 1024 * 1024 },
  }))
  async uploadUserProfileImage(
    @Param('id') id: string,
    @UploadedFile() file: Express.Multer.File,
    @Req() req: any,
  ) {
    if (req.user.userId !== id) {
      if ([Role.SUPER_ADMIN, Role.DF].includes(req.user.role as Role)) {
        // unrestricted administrative edit
      } else if ([Role.SRIO, Role.SCQ].includes(req.user.role as Role)) {
        const [actor, target] = await Promise.all([
          this.service.findOne(req.user.userId),
          this.service.findOne(id),
        ]);
        if (actor?.region && target?.region && !this.service.regionsMatch(actor.region, target.region)) {
          throw new ForbiddenException('Utilisateur hors de votre région');
        }
        if (![Role.GESTIONNAIRE, Role.DIRECTEUR].includes(target?.role as Role)) {
          throw new ForbiddenException('Utilisateur hors de votre périmètre');
        }
      } else if ([Role.DIRECTEUR, Role.GESTIONNAIRE].includes(req.user.role as Role)) {
        const allowed = await this.service.canManageUserInEstablishment(req.user.userId, id, req.user.role);
        if (!allowed) throw new ForbiddenException('Modification non autorisée hors de votre établissement');
      } else {
        throw new ForbiddenException('Vous ne pouvez modifier que votre propre photo de profil');
      }
    }
    return this.service.updateProfileImage(id, file);
  }

  @Get(':id/profile-image')
  async getProfileImage(@Param('id') id: string, @Req() req: any, @Res() res: Response) {
    if (req.user.userId !== id && [Role.SRIO, Role.SCQ].includes(req.user.role as Role)) {
      const [actor, target] = await Promise.all([
        this.service.findOne(req.user.userId),
        this.service.findOne(id),
      ]);
      if (actor?.region && target?.region && !this.service.regionsMatch(actor.region, target.region)) {
        throw new ForbiddenException('Utilisateur hors de votre région');
      }
    } else if (req.user.userId !== id && [Role.DIRECTEUR, Role.GESTIONNAIRE].includes(req.user.role as Role)) {
      const allowed = await this.service.canManageUserInEstablishment(req.user.userId, id, req.user.role);
      if (!allowed) throw new ForbiddenException('Accès non autorisé à cette photo de profil');
    } else if (req.user.userId !== id && ![Role.SUPER_ADMIN, Role.DF].includes(req.user.role as Role)) {
      throw new ForbiddenException('Accès non autorisé à cette photo de profil');
    }
    const result = await this.service.getProfileImage(id);
    res.set({
      'Content-Type': result.contentType,
      'Content-Disposition': 'inline',
      'Cache-Control': 'private, max-age=3600',
    });
    res.send(result.buffer);
  }

}
