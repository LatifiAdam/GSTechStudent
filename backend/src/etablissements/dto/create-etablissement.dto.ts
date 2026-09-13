import { IsNotEmpty, IsString, MaxLength, IsOptional } from 'class-validator';
export class CreateEtablissementDto {
  @IsOptional()
  @IsString()
  @MaxLength(120)
  region?: string; @IsString() @IsNotEmpty() @MaxLength(200) nomEtablissement: string; }
