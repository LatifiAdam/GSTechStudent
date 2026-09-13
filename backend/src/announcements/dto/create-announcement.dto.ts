// src/announcements/dto/create-announcement.dto.ts

import {
  IsDateString,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUUID,
  MaxLength,
} from 'class-validator';

import { TypeAnnonce } from '../../entities/annonce.entity';

export class CreateAnnouncementDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(200)
  titre: string;

  @IsNotEmpty()
  @IsString()
  @MaxLength(5000)
  contenu: string;

  @IsOptional()
  @IsEnum(TypeAnnonce)
  typeAnnonce?: TypeAnnonce;

  @IsOptional()
  @IsDateString()
  dateEvenement?: string;

  @IsOptional()
  @IsUUID()
  idCours?: string;

  @IsOptional()
  @IsUUID()
  idClasse?: string;
}