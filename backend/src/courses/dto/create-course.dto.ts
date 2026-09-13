// src/courses/dto/create-course.dto.ts

import {
  IsNotEmpty,
  IsString,
  MaxLength,
} from 'class-validator';

export class CreateCourseDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(100)
  nomCours: string;
}