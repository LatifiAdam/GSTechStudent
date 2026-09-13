import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

/** PATCH /document-requests/:id/refuse — Phase 2, section 4.8 (RG10). */
export class RefuseDocumentRequestDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(1000)
  motif: string;
}
