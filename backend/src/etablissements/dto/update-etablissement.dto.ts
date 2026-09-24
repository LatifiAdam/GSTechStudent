import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

export class UpdateEtablissementDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(200)
  nomEtablissement: string;
}
