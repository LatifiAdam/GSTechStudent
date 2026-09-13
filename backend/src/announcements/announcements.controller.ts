// src/announcements/announcements.controller.ts

import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  Query,
  Req,
  UseGuards,
} from '@nestjs/common';

import { AnnouncementsService } from './announcements.service';
import { CreateAnnouncementDto } from './dto/create-announcement.dto';

import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('announcements')
@UseGuards(JwtAuthGuard, RolesGuard)
export class AnnouncementsController {
  constructor(
    private readonly service: AnnouncementsService,
  ) {}

  @Roles(
    Role.FORMATEUR,
    Role.stagiaire,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Get()
  findAll(@Req() req: any, @Query('classId') classId?: string, @Query('authorRole') authorRole?: Role, @Query('year') year?: string) {
    return this.service.findAll(req.user.userId, req.user.role, classId, authorRole, year ? Number(year) : undefined);
  }

  @Roles(
    Role.FORMATEUR,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Post()
  create(
    @Body() dto: CreateAnnouncementDto,
    @Req() req: any,
  ) {
    return this.service.create(
      dto,
      req.user.userId,
      req.user.role,
    );
  }

  @Roles(
    Role.FORMATEUR,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Delete(':id')
  remove(
    @Param('id') id: string,
    @Req() req: any,
  ) {
    return this.service.remove(
      id,
      req.user.userId,
      req.user.role === Role.DF,
    );
  }
}