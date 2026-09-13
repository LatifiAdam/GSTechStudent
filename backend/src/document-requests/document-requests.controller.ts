import { Body, Controller, Get, Param, Patch, Post, Query, Req, Res, UseGuards } from '@nestjs/common';
import { Response } from 'express';
import { DocumentRequestsService } from './document-requests.service';
import { CreateDocumentRequestDto } from './dto/create-document-request.dto';
import { RefuseDocumentRequestDto } from './dto/refuse-document-request.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('document-requests')
@UseGuards(JwtAuthGuard, RolesGuard)
export class DocumentRequestsController {
  constructor(private readonly service: DocumentRequestsService) {}
  @Roles(Role.stagiaire) @Post() create(@Body() dto: CreateDocumentRequestDto, @Req() req: any) { return this.service.create(dto, req.user.userId); }
  @Roles(Role.DF, Role.stagiaire, Role.GESTIONNAIRE) @Get() findAll(@Query('statut') statut: string | undefined, @Req() req: any) { return this.service.findAll(statut, req.user.userId, req.user.role); }
  @Roles(Role.GESTIONNAIRE) @Patch(':id/generate') generate(@Param('id') id: string, @Req() req: any) { return this.service.generateForGestionnaire(id, req.user.userId); }
  @Roles(Role.GESTIONNAIRE) @Patch(':id/refuse') refuse(@Param('id') id: string, @Body() dto: RefuseDocumentRequestDto, @Req() req: any) { return this.service.refuseForGestionnaire(id, dto, req.user.userId); }
  @Roles(Role.stagiaire) @Get(':id/file') async getFile(@Param('id') id: string, @Req() req: any, @Res() res: Response) { const {buffer,filename}=await this.service.getFile(id,req.user.userId,req.user.role); res.set({'Content-Type':'application/pdf','Content-Disposition':`attachment; filename="${filename}"`}); res.send(buffer); }
}
