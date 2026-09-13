import { IsDateString, IsUUID } from 'class-validator';

/**
 * POST /attendance/calls
 */
export class OpenCallDto {
  @IsUUID()
  idCreneau: string;

  @IsDateString()
  dateHeure: string;
}