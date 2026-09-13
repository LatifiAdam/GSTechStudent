import { Type } from 'class-transformer';
import { ArrayMaxSize, ArrayNotEmpty, IsArray, IsEnum, IsUUID, ValidateNested } from 'class-validator';
import { StatutPresence } from '../../entities/presence.entity';

class AttendanceRecordDto {
  @IsUUID()
  idstagiaire: string;

  @IsEnum(StatutPresence)
  statut: StatutPresence;
}

/**
 * PATCH /attendance/calls/:id/records — Phase 2, section 4.6.
 * Ne transporte que les étudiants en exception (absent/retard) ; tous les
 * autres restent au statut "present" appliqué par défaut à l'ouverture de l'appel.
 */
export class UpdateRecordsDto {
  @IsArray()
  @ArrayNotEmpty()
  @ArrayMaxSize(500)
  @ValidateNested({ each: true })
  @Type(() => AttendanceRecordDto)
  records: AttendanceRecordDto[];
}
