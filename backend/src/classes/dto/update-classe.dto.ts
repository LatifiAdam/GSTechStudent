import { IsOptional, IsString, MaxLength } from 'class-validator';

export class UpdateClasseDto {
  @IsOptional()
  @IsString()
  @MaxLength(100)
  nomClasse?: string;

  @IsOptional()
  @IsString()
  @MaxLength(255)
  description?: string;
}
