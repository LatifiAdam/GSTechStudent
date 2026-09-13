import { Body, Controller, Delete, Get, Param, Patch, Post, Req, UseGuards } from '@nestjs/common';
import { AffectationsService } from './affectations.service';
import { CreateAffectationDto } from './dto/create-affectation.dto';
import { UpdateAffectationDto } from './dto/update-affectation.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';
@Controller('affectations')
@UseGuards(JwtAuthGuard, RolesGuard)
export class AffectationsController {
 constructor(private readonly service:AffectationsService){}
 @Roles(Role.DF,Role.DIRECTEUR,Role.FORMATEUR,Role.stagiaire) @Get() findAll(@Req() req:any){return this.service.findAll(req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.DIRECTEUR,Role.FORMATEUR,Role.stagiaire) @Get(':id') findOne(@Param('id')id:string,@Req()req:any){return this.service.findOne(id,req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.DIRECTEUR) @Post() create(@Body()d:CreateAffectationDto,@Req()req:any){return this.service.create(d,req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.DIRECTEUR) @Patch(':id') update(@Param('id')id:string,@Body()d:UpdateAffectationDto,@Req()req:any){return this.service.update(id,d,req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.DIRECTEUR) @Delete(':id') remove(@Param('id')id:string,@Req()req:any){return this.service.remove(id,req.user.userId,req.user.role);}
}
