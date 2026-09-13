import { Body, Controller, Get, Param, Post, Req, UseGuards } from '@nestjs/common';
import { GradingService } from './grading.service';
import { SaveGradesDto } from './dto/save-grades.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('grading')
@UseGuards(JwtAuthGuard, RolesGuard)
export class GradingController {
  constructor(private readonly service: GradingService) {}

  @Roles(Role.FORMATEUR)
  @Get('formateur/classes')
  formateurClasses(@Req() req: any) { return this.service.formateurClasses(req.user.userId); }

  @Roles(Role.FORMATEUR)
  @Get('formateur/classes/:idAffectation')
  classGrades(@Param('idAffectation') idAffectation: string, @Req() req: any) { return this.service.getClassGrades(idAffectation, req.user.userId); }

  @Roles(Role.FORMATEUR)
  @Post('formateur/classes/:idAffectation')
  saveGrades(@Param('idAffectation') idAffectation: string, @Body() dto: SaveGradesDto, @Req() req: any) { return this.service.saveGrades(idAffectation, dto, req.user.userId); }

  @Roles(Role.stagiaire)
  @Get('student')
  studentGrades(@Req() req: any) { return this.service.studentGrades(req.user.userId); }
}
