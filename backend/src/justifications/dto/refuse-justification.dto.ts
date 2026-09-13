import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

/** PATCH /justifications/:id/refuse — Phase 2, section 4.7. */
export class RefuseJustificationDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(1000)
  motif: string;
}
