import { Body, Controller, Delete, Get, Param, Patch, Post, Req, UseGuards } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { RegisterDeviceTokenDto } from './dto/register-device-token.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('notifications')
@UseGuards(JwtAuthGuard, RolesGuard)
export class NotificationsController {
  constructor(private readonly service: NotificationsService) {}

  // GET /notifications — Liste les notifications de l'utilisateur connecté
  // Rôle(s) autorisé(s) : Utilisateur authentifié
  @Roles(Role.FORMATEUR, Role.stagiaire, Role.DF)
  @Get()
  findAll(@Req() req: any) {
    return this.service.findAll(req.user.userId);
  }

  // PATCH /notifications/:id/read — Marque une notification comme lue
  // Rôle(s) autorisé(s) : Utilisateur authentifié (destinataire)
  @Roles(Role.FORMATEUR, Role.stagiaire, Role.DF)
  @Patch(':id/read')
  markAsRead(@Param('id') id: string, @Req() req: any) {
    return this.service.markAsRead(id, req.user.userId);
  }

  // POST /notifications/device-tokens — Enregistre le token FCM de l'appareil (nouveau)
  // Rôle(s) autorisé(s) : Utilisateur authentifié
  @Roles(Role.FORMATEUR, Role.stagiaire, Role.DF)
  @Post('device-tokens')
  registerToken(@Body() dto: RegisterDeviceTokenDto, @Req() req: any) {
    return this.service.registerToken(dto, req.user.userId);
  }

  // DELETE /notifications/device-tokens/:token — Retire un token (nouveau)
  // Rôle(s) autorisé(s) : Utilisateur authentifié (propriétaire)
  @Roles(Role.FORMATEUR, Role.stagiaire, Role.DF)
  @Delete('device-tokens/:token')
  removeToken(@Param('token') token: string, @Req() req: any) {
    return this.service.removeToken(token, req.user.userId);
  }
}
