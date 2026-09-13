import { IsUUID } from 'class-validator';
export class AssignUserDto { @IsUUID() idUtilisateur: string; }
