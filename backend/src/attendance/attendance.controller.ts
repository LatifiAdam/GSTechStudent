import { Body, Controller, Get, Param, Patch, Post, Req, UseGuards } from '@nestjs/common';
import { AttendanceService } from './attendance.service';
import { OpenCallDto } from './dto/open-call.dto';
import { UpdateRecordsDto } from './dto/update-records.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('attendance')
@UseGuards(JwtAuthGuard, RolesGuard)
export class AttendanceController {
  constructor(private readonly service: AttendanceService) {}

  // POST /attendance/calls — Ouvre un appel ; statut "présent" par défaut
  // Rôle(s) autorisé(s) : Formateur
  @Roles(Role.FORMATEUR)
  @Post('calls')
  openCall(@Body() dto: OpenCallDto, @Req() req: any) {
    return this.service.openCall(dto, req.user.userId);
  }

  // PATCH /attendance/calls/:id/records — Met à jour les étudiants en exception
  // Rôle(s) autorisé(s) : Formateur (auteur de l'appel)
  @Roles(Role.FORMATEUR)
  @Patch('calls/:id/records')
  updateRecords(@Param('id') id: string, @Body() dto: UpdateRecordsDto, @Req() req: any) {
    return this.service.updateRecords(id, dto, req.user.userId);
  }

  // POST /attendance/calls/:id/validate — Valide et verrouille définitivement un appel
  // Rôle(s) autorisé(s) : Formateur (auteur de l'appel)
  @Roles(Role.FORMATEUR)
  @Post('calls/:id/validate')
  validateCall(@Param('id') id: string, @Req() req: any) {
    return this.service.validateCall(id, req.user.userId);
  }

  // GET /attendance/calls/:id — Consulte le détail d'un appel et de ses présences
  // Rôle(s) autorisé(s) : Formateur, Administrateur
  @Roles(Role.FORMATEUR, Role.DF)
  @Get('calls/:id')
  findCall(@Param('id') id: string) {
    return this.service.findCall(id);
  }

  // GET /attendance/stagieres/:id/history — Consulte l'historique de présence d'un étudiant
  // Rôle(s) autorisé(s) : Étudiant (soi-même), Formateur, Administrateur
  @Roles(Role.stagiaire, Role.FORMATEUR, Role.DF)
  @Get('stagieres/:id/history')
  studentHistory(@Param('id') id: string, @Req() req: any) {
    return this.service.studentHistory(id, req.user.userId, req.user.role);
  }
}
