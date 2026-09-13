import { Body, Controller, Delete, Get, Param, Patch, Post, Query, Req, UseGuards } from '@nestjs/common';
import { ScheduleService } from './schedule.service';
import { CreateCreneauDto } from './dto/create-creneau.dto';
import { UpdateCreneauDto } from './dto/update-creneau.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('schedule')
@UseGuards(JwtAuthGuard, RolesGuard)
export class ScheduleController {
  constructor(private readonly service: ScheduleService) {}

  // GET /schedule — Consulte l'emploi du temps, filtrable par cours ou par étudiant
  // Rôle(s) autorisé(s) : Tous rôles authentifiés
  @Roles(Role.FORMATEUR, Role.stagiaire, Role.DF, Role.DIRECTEUR)
  @Get()
  findAll(@Query('coursId') coursId?: string, @Query('stagiaireId') stagiaireId?: string, @Query('formateurId') formateurId?: string, @Query('classeId') classeId?: string) {
    return this.service.findAll(coursId, stagiaireId, formateurId, classeId);
  }

  // POST /schedule — Crée un créneau horaire pour un cours (RG8)
  // Rôle(s) autorisé(s) : Administrateur uniquement
  @Roles(Role.DF, Role.DIRECTEUR)
  @Post()
  create(@Body() dto: CreateCreneauDto, @Req() req:any) { return this.service.create(dto, req.user.userId, req.user.role);
  }

  // PATCH /schedule/:id — Modifie un créneau (jour, horaires, salle) (RG8)
  // Rôle(s) autorisé(s) : Administrateur uniquement
  @Roles(Role.DF, Role.DIRECTEUR)
  @Patch(':id')
  update(@Param('id') id: string, @Body() dto: UpdateCreneauDto, @Req() req:any) { return this.service.update(id, dto, req.user.userId, req.user.role);
  }

  // DELETE /schedule/:id — Supprime un créneau (RG8)
  // Rôle(s) autorisé(s) : Administrateur uniquement
  @Roles(Role.DF, Role.DIRECTEUR)
  @Delete(':id')
  remove(@Param('id') id: string, @Req() req:any) { return this.service.remove(id, req.user.userId, req.user.role);
  }
}
