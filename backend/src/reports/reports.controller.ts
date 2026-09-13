import {
  Controller,
  Get,
  Param,
  Query,
  Req,
  Res,
  UseGuards,
} from '@nestjs/common';

import { Response } from 'express';

import { ReportsService } from './reports.service';

import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('reports')
@UseGuards(JwtAuthGuard, RolesGuard)
export class ReportsController {
  constructor(
    private readonly service: ReportsService,
  ) {}

  // ============================================================
  // ATTENDANCE
  // ============================================================

  // GET /reports/attendance
  //
  // Rôles :
  // - Formateur
  // - Administrateur

  @Roles(
    Role.FORMATEUR,
    Role.DF,
  )
  @Get('attendance')
  attendance(
    @Query('periode') periode?: string,
  ) {
    return this.service.attendance(periode);
  }

  // ============================================================
  // COURSE REPORT
  // ============================================================

  // GET /reports/courses/:id

  @Roles(
    Role.FORMATEUR,
    Role.DF,
  )
  @Get('courses/:id')
  courseReport(
    @Param('id') id: string,
    @Req() req: any,
  ) {
    return this.service.courseReport(id, req.user.userId, req.user.role);
  }

  // ============================================================
  // STUDENT REPORT
  // ============================================================

  // GET /reports/stagieres/:id

  @Roles(
    Role.stagiaire,
    Role.FORMATEUR,
    Role.DF,
  )
  @Get('stagieres/:id')
  studentReport(
    @Param('id') id: string,
    @Req() req: any,
  ) {
    return this.service.studentReport(id, req.user.userId, req.user.role);
  }

  // ============================================================
  // RECENT ACTIVITY
  // ============================================================

  // GET /reports/recent-activity?limit=10
  //
  // Utilisé par le dashboard administrateur.

@Roles(Role.DF)
@Get('recent-activity')
recentActivity(@Query('limit') limit?: string) {
  const parsedLimit = limit ? Number(limit) : 10;

  return this.service.recentActivity(
    Number.isFinite(parsedLimit) && parsedLimit > 0
      ? Math.min(parsedLimit, 50)
      : 10,
  );
}
  // ============================================================
  // EXPORT
  // ============================================================

  // GET /reports/export?format=pdf
  // GET /reports/export?format=xlsx

  @Roles(
    Role.FORMATEUR,
    Role.DF,
  )
  @Get('export')
  async export(
    @Query('format')
    format: 'pdf' | 'xlsx' = 'pdf',

    @Res()
    res: Response,
  ) {
    const {
      buffer,
      filename,
      contentType,
    } = await this.service.export(format);

    res.set({
      'Content-Type': contentType,

      'Content-Disposition':
        `attachment; filename="${filename}"`,
    });

    res.send(buffer);
  }
}