import { Body, Controller, Delete, Get, Param, Patch, Post, Req, UseGuards } from '@nestjs/common';
import { EtablissementsService } from './etablissements.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Role } from '../common/enums/roles.enum';
import { CreateEtablissementDto } from './dto/create-etablissement.dto';
import { AssignUserDto } from './dto/assign-user.dto';
import { UpdateEtablissementDto } from './dto/update-etablissement.dto';

@Controller('etablissements')
@UseGuards(JwtAuthGuard, RolesGuard)
export class EtablissementsController {
 constructor(private readonly service:EtablissementsService){}
 @Roles(Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ) @Get() list(@Req() req:any){return this.service.list(req.user.userId, req.user.role);}
 @Roles(Role.SUPER_ADMIN, Role.DF) @Post() create(@Body() dto:CreateEtablissementDto){return this.service.create(dto.nomEtablissement, dto.region);}
 @Roles(Role.SUPER_ADMIN, Role.DF) @Patch(':id') update(@Param('id') id:string,@Body() dto:UpdateEtablissementDto){return this.service.update(id,dto.nomEtablissement);}
 @Roles(Role.SUPER_ADMIN, Role.DF) @Delete(':id') remove(@Param('id') id:string){return this.service.remove(id);}
 @Roles(Role.DF, Role.SCQ) @Post(':id/directeur') assignDirector(@Param('id') id:string,@Body() dto:AssignUserDto,@Req() req:any){return this.service.assignDirector(id,dto.idUtilisateur,req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.SRIO) @Post(':id/gestionnaires') addGestionnaire(@Param('id') id:string,@Body() dto:AssignUserDto,@Req() req:any){return this.service.addGestionnaire(id,dto.idUtilisateur,req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.SRIO) @Delete(':id/gestionnaires/:gestionnaireId') removeGestionnaire(@Param('id') id:string,@Param('gestionnaireId') gid:string,@Req() req:any){return this.service.removeGestionnaire(id,gid,req.user.userId,req.user.role);}
 @Roles(Role.DF,Role.SCQ) @Delete(':id/directeur') removeDirector(@Param('id') id:string,@Req() req:any){return this.service.removeDirector(id,req.user.userId,req.user.role);}
 @Roles(Role.DIRECTEUR) @Get('mine') mine(@Req() req:any){return this.service.myEstablishment(req.user.userId);}
}
