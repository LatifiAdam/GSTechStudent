import { IsNotEmpty, IsOptional, IsString, MaxLength } from 'class-validator';

export class UpdateCourseDto {
  @IsOptional()
  @IsNotEmpty()
  @IsString()
  @MaxLength(100)
  nomCours?: string;
}
