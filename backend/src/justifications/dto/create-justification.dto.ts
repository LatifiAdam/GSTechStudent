import { IsNotEmpty, IsOptional, IsString, IsUUID, MaxLength } from 'class-validator';

/** POST /justifications — Phase 2, section 4.7. */
export class CreateJustificationDto {
  @IsUUID()
  idPresence: string;

  @IsNotEmpty()
  @IsString()
  @MaxLength(1000)
  motif: string;

  @IsOptional()
  @IsString()
  @MaxLength(1024)
  pieceJointe?: string; // URL du fichier PDF/image déjà uploadé
}
