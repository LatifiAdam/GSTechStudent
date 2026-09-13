import { IsOptional, IsUUID } from 'class-validator';

/**
 * Accept both the canonical camelCase API names and the legacy lowercase
 * names emitted by older Android builds. The service normalizes them.
 */
export class CreateAffectationDto {
  @IsOptional()
  @IsUUID()
  idClasse?: string;

  @IsOptional()
  @IsUUID()
  idCours?: string;

  @IsOptional()
  @IsUUID()
  idFormateur?: string;

  @IsOptional()
  @IsUUID()
  idclasse?: string;

  @IsOptional()
  @IsUUID()
  idcours?: string;

  @IsOptional()
  @IsUUID()
  idformateur?: string;

}
