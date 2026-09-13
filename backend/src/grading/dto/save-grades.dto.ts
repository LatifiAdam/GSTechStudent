import { IsNumber, IsOptional, IsUUID, Max, Min } from 'class-validator';

export class SaveGradesDto {
  @IsUUID()
  idstagiaire: string;

  @IsOptional()
  @IsNumber()
  @Min(0)
  @Max(20)
  note1?: number | null;

  @IsOptional()
  @IsNumber()
  @Min(0)
  @Max(20)
  note2?: number | null;

  @IsOptional()
  @IsNumber()
  @Min(0)
  @Max(20)
  note3?: number | null;
}
