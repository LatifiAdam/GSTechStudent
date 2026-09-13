import {
  IsEmail,
  IsOptional,
  IsString,
  IsUUID,
  Matches,
  MaxLength,
} from 'class-validator';

/**
 * PATCH /users/:id.
 * All editable profile fields are optional so the Super Admin and other
 * authorized roles can update users without relying on mapped-type inference.
 * Role and password changes remain handled by dedicated flows.
 */
export class UpdateUserDto {
  @IsOptional()
  @IsString()
  @MaxLength(100)
  nom?: string;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  prenom?: string;

  @IsOptional()
  @IsEmail({}, { message: "L'adresse e-mail est invalide" })
  email?: string;

  @IsOptional()
  @IsString()
  @MaxLength(20)
  cin?: string;

  @IsOptional()
  @Matches(/^\+?[0-9]{6,15}$/, { message: 'Le téléphone doit contenir uniquement des chiffres (6 à 15 chiffres), avec + facultatif.' })
  telephone?: string;

  @IsOptional()
  @IsString()
  @MaxLength(255)
  adresse?: string;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  module?: string;

  @IsOptional()
  @IsString()
  @MaxLength(30)
  numerostagiaire?: string;

  @IsOptional()
  @IsString()
  @MaxLength(50)
  promotion?: string;

  @IsOptional()
  @IsString()
  @MaxLength(50)
  niveauAcces?: string;

  @IsOptional()
  @IsString()
  @MaxLength(120)
  region?: string;

  @IsOptional()
  @IsUUID()
  idEtablissement?: string;
}
