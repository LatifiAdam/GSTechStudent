import { IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';
export class CreateClasseDto { @IsNotEmpty() @IsString() @MaxLength(100) nomClasse:string; @IsOptional() @IsString() @MaxLength(255) description?:string; }
