import { BadRequestException, Body, Controller, Get, Post, Req, UseGuards } from '@nestjs/common';
import { DataSource, Repository } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
import { AppService } from './app.service';
import { AuditLog } from './entities/audit-log.entity';
import { JwtAuthGuard } from './common/guards/jwt-auth.guard';
import { RolesGuard } from './common/guards/roles.guard';
import { Roles } from './common/decorators/roles.decorator';
import { Role } from './common/enums/roles.enum';

@Controller()
export class AppController {
  constructor(
    private readonly appService: AppService,
    private readonly dataSource: DataSource,
    @InjectRepository(AuditLog) private readonly auditRepo: Repository<AuditLog>,
  ) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Get('health')
  health() {
    return {
      status: 'UP',
      api: 'GSTech API',
      uptimeSeconds: Math.floor(process.uptime()),
      timestamp: new Date().toISOString(),
    };
  }

  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(Role.SUPER_ADMIN)
  @Get('system/database')
  async database() {
    const started = Date.now();
    const options = this.dataSource.options;
    const mysqlOptions = options.type === 'mysql' ? options : null;
    const host = mysqlOptions?.host ?? 'unknown';
    const port = mysqlOptions?.port ?? 3306;
    const database = mysqlOptions?.database ?? process.env.DB_NAME ?? 'gestion_stagiaires';

    try {
      await this.dataSource.query('SELECT 1');
      return {
        status: 'CONNECTED',
        database,
        host,
        port,
        responseMs: Date.now() - started,
      };
    } catch (error: any) {
      return {
        status: 'ERROR',
        database,
        host,
        port,
        responseMs: Date.now() - started,
        error: error?.message ?? 'Connexion MySQL impossible',
      };
    }
  }

  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(Role.SUPER_ADMIN)
  @Get('system/account-counts')
  async accountCounts() {
    const roles = ['superadmin', 'df', 'srio', 'scq', 'directeur', 'gestionnaire', 'formateur', 'stagiaire'];
    const counts: Record<string, number> = {};
    for (const role of roles) {
      counts[role] = await this.dataSource.getRepository('utilisateur').count({ where: { role } });
    }
    const total = roles.reduce((sum, role) => sum + counts[role], 0);
    return { total, ...counts };
  }

  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(Role.SUPER_ADMIN)
  @Get('system/logs')
  async logs() {
    const rows = await this.auditRepo.find({
      where: { entityType: 'superadmin_report' },
      order: { createdAt: 'DESC' },
      take: 50,
    });
    return rows.map((row) => ({
      id: row.idAudit,
      report: typeof row.newValue === 'object' && row.newValue?.report ? row.newValue.report : String(row.newValue ?? ''),
      date: row.createdAt,
    }));
  }

  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(Role.SUPER_ADMIN)
  @Post('system/logs')
  async addLog(@Body() body: { report: string }, @Req() req: any) {
    if (!body?.report?.trim()) {
      throw new BadRequestException('Le rapport est vide');
    }
    const row = this.auditRepo.create({
      actorId: req.user.userId ?? null,
      actorRole: req.user.role ?? Role.SUPER_ADMIN,
      action: 'SUPERVISOR_REPORT',
      entityType: 'superadmin_report',
      entityId: null,
      region: null,
      idEtablissement: null,
      oldValue: null,
      newValue: { report: body.report.trim() },
      ipAddress: req.ip ?? null,
    });
    const saved = await this.auditRepo.save(row);
    return { id: saved.idAudit, report: body.report.trim(), date: saved.createdAt };
  }
}
