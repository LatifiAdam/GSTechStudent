import { Body, Controller, Get, Param, Patch, Post, Query, Req, UploadedFile, UseGuards, UseInterceptors } from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { memoryStorage } from 'multer';
import { JustificationsService } from './justifications.service';
import { CreateJustificationDto } from './dto/create-justification.dto';
import { RefuseJustificationDto } from './dto/refuse-justification.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('justifications')
@UseGuards(JwtAuthGuard, RolesGuard)
export class JustificationsController {
  constructor(private readonly service: JustificationsService) {}

  // POST /justifications/upload — Téléverse la pièce jointe (PDF/image) d'un
  // justificatif ; l'URL retournée est ensuite passée dans pieceJointe lors
  // de la création du justificatif (POST /justifications).
  // Rôle(s) autorisé(s) : Étudiant
  @Roles(Role.stagiaire)
  @Post('upload')
  @UseInterceptors(FileInterceptor('file', {
    storage: memoryStorage(),
    limits: {
      fileSize: 5 * 1024 * 1024,
      fieldNameSize: 100,
      fields: 10,
      files: 1,
      parts: 11,
    },
  }))
  uploadAttachment(@UploadedFile() file: Express.Multer.File) {
    return this.service.uploadAttachment(file);
  }

  // POST /justifications — Dépose un justificatif d'absence avec pièce jointe
  // Rôle(s) autorisé(s) : Étudiant
  @Roles(Role.stagiaire)
  @Post()
  create(@Body() dto: CreateJustificationDto, @Req() req: any) {
    return this.service.create(dto, req.user.userId);
  }

  // GET /justifications — Liste les justificatifs, filtrables par statut et par cours
  // Rôle(s) autorisé(s) : Formateur, Administrateur
  @Roles(Role.FORMATEUR, Role.DF)
  @Get()
  findAll(@Req() req: any, @Query('statut') statut?: string, @Query('coursId') coursId?: string) {
    return this.service.findAll(statut, coursId, req.user.userId, req.user.role);
  }

  // PATCH /justifications/:id/accept — Accepte un justificatif
  // Rôle(s) autorisé(s) : Formateur (du cours concerné)
  @Roles(Role.FORMATEUR)
  @Patch(':id/accept')
  accept(@Param('id') id: string, @Req() req: any) {
    return this.service.accept(id, req.user.userId, req.user.role);
  }

  // PATCH /justifications/:id/refuse — Refuse un justificatif, avec motif
  // Rôle(s) autorisé(s) : Formateur (du cours concerné)
  @Roles(Role.FORMATEUR)
  @Patch(':id/refuse')
  refuse(@Param('id') id: string, @Body() dto: RefuseJustificationDto, @Req() req: any) {
    return this.service.refuse(id, dto, req.user.userId, req.user.role);
  }
}
