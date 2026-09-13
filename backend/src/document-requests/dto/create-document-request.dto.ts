import { IsEnum, IsOptional, IsUUID } from 'class-validator';
import { TypeDocument } from '../../entities/demande-document.entity';

export class CreateDocumentRequestDto {
  @IsOptional()
  @IsEnum(TypeDocument)
  typeDocument?: TypeDocument;

  @IsOptional()
  @IsUUID()
  idDocument?: string;
}
