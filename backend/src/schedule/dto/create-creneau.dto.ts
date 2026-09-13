import {
  IsDateString,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  MaxLength,
  IsUUID,
  Matches,
} from 'class-validator';

import { JourSemaine } from '../../entities/creneau.entity';

export class CreateCreneauDto {
  @IsEnum(JourSemaine)
  jourSemaine: JourSemaine;

  @Matches(
    /^([01]\d|2[0-3]):([0-5]\d)$/,
    {
      message:
        'heureDebut doit être au format HH:mm',
    },
  )
  heureDebut: string;

  @Matches(
    /^([01]\d|2[0-3]):([0-5]\d)$/,
    {
      message:
        'heureFin doit être au format HH:mm',
    },
  )
  heureFin: string;

  @IsNotEmpty()
  @IsString()
  @MaxLength(30)
  salle: string;

  @IsUUID()
  idAffectation: string;

  @IsOptional()
  @IsDateString()
  dateDebut?: string;

  @IsOptional()
  @IsDateString()
  dateFin?: string;
}