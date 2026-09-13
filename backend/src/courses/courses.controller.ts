// src/courses/courses.controller.ts

import {
  Body,
  Req,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';

import { CoursesService } from './courses.service';
import { CreateCourseDto } from './dto/create-course.dto';
import { UpdateCourseDto } from './dto/update-course.dto';

import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';

@Controller('courses')
@UseGuards(JwtAuthGuard, RolesGuard)
export class CoursesController {
  constructor(
    private readonly service: CoursesService,
  ) {}

  @Roles(
    Role.FORMATEUR,
    Role.stagiaire,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Get()
  findAll(
    @Query('formateurId') formateurId?: string, @Req() req?: any) {
    return this.service.findAll(formateurId, req?.user?.userId, req?.user?.role);
  }

  @Roles(Role.DF, Role.DIRECTEUR)
  @Post()
  create(@Body() dto: CreateCourseDto, @Req() req:any) { return this.service.create(dto, req.user.userId, req.user.role);
  }

  @Roles(Role.DF, Role.DIRECTEUR)
  @Patch(':id')
  update(@Param('id') id: string, @Body() dto: UpdateCourseDto, @Req() req:any) { return this.service.update(id, dto, req.user.userId, req.user.role);
  }

  @Roles(Role.DF, Role.DIRECTEUR)
  @Delete(':id')
  remove(@Param('id') id: string, @Req() req:any) { return this.service.remove(id, req.user.userId, req.user.role);
  }

  @Roles(
    Role.FORMATEUR,
    Role.stagiaire,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Get(':id/student-count')
  getStudentCount(@Param('id') id: string, @Req() req:any) { return this.service.getStudentCount(id, req.user.userId, req.user.role);
  }

  @Roles(
    Role.FORMATEUR,
    Role.stagiaire,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Get(':id/affectations')
  getAffectations(@Param('id') id: string, @Req() req:any) { return this.service.getAffectations(id, req.user.userId, req.user.role);
  }

  @Roles(
    Role.FORMATEUR,
    Role.stagiaire,
    Role.DF,
    Role.DIRECTEUR,
  )
  @Get(':id')
  findOne(@Param('id') id: string, @Req() req:any) { return this.service.findOne(id, req.user.userId, req.user.role);
  }
}