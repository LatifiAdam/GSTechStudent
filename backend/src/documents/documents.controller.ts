import { BadRequestException, Body, Controller, Get, Param, Patch, Post, Query, Req, Res, UploadedFile, UseGuards, UseInterceptors } from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { Response } from 'express';
import { DocumentsService } from './documents.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';
import { DocumentStatus } from '../entities/document.entity';

@Controller('documents')
@UseGuards(JwtAuthGuard, RolesGuard)
export class DocumentsController {
  constructor(private readonly service: DocumentsService) {}

  @Roles(Role.DF, Role.DIRECTEUR)
  @Get('pending')
  pending(@Req() req: any) { return req.user.role === Role.DIRECTEUR ? this.service.pendingForDirector(req.user.userId) : this.service.list(DocumentStatus.PENDING); }

  @Roles(Role.DF, Role.DIRECTEUR)
  @Get('all')
  all(@Query('statut') statut?: DocumentStatus) { return this.service.list(statut); }

  @Roles(Role.GESTIONNAIRE)
  @Get('mine')
  mine(@Req() req: any) { return this.service.mine(req.user.userId); }

  @Roles(Role.stagiaire)
  @Get('approved')
  approved() { return this.service.approved(); }

  @Roles(Role.GESTIONNAIRE)
  @Post('upload')
  @UseInterceptors(FileInterceptor('file', {
limits: {
  fileSize: 5 * 1024 * 1024,
  fieldNameSize: 100,
  fields: 10,
  files: 1,
  parts: 11,
},
}))
  upload(@UploadedFile() file: Express.Multer.File, @Body('nomDocument') nomDocument: string, @Req() req: any) {
    return this.service.upload(file, nomDocument, req.user.userId);
  }

  @Roles(Role.DIRECTEUR)
  @Patch(':id/approve')
  approve(@Param('id') id: string, @Req() req: any) { return this.service.approve(id, req.user.userId); }

  @Roles(Role.DIRECTEUR)
  @Patch(':id/refuse')
  refuse(@Param('id') id: string, @Body('motif') motif: string, @Req() req: any) { return this.service.refuse(id, req.user.userId, motif); }

  @Roles(Role.DIRECTEUR, Role.stagiaire)
  @Get(':id/file')
  async file(@Param('id') id: string, @Req() req: any, @Res() res: Response) {
    const doc = await this.service.file(id, req.user.userId, req.user.role);
    res.set({
      'Content-Type': doc.type === 'pdf' ? 'application/pdf' : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'Content-Disposition': `inline; filename="${doc.filename.replace(/"/g, '')}"`,
    });
    res.send(doc.buffer);
  }
}
