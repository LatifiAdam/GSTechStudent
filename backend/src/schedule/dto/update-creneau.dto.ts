import {
  IsDateString,
  IsEnum,
  IsOptional,
  IsString,
  MaxLength,
  IsUUID,
  Matches,
} from 'class-validator';
import { JourSemaine } from '../../entities/creneau.entity';

export class UpdateCreneauDto {
  @IsOptional()
  @IsEnum(JourSemaine)
  jourSemaine?: JourSemaine;

  @IsOptional()
  @Matches(/^([01]\d|2[0-3]):([0-5]\d)$/, { message: 'heureDebut doit être au format HH:mm' })
  heureDebut?: string;

  @IsOptional()
  @Matches(/^([01]\d|2[0-3]):([0-5]\d)$/, { message: 'heureFin doit être au format HH:mm' })
  heureFin?: string;

  @IsOptional()
  @IsString()
  @MaxLength(30)
  salle?: string;

  @IsOptional()
  @IsUUID()
  idAffectation?: string;

  @IsOptional()
  @IsDateString()
  dateDebut?: string;

  @IsOptional()
  @IsDateString()
  dateFin?: string;
}
