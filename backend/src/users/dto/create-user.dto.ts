import {
  IsEmail,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUUID,
  MinLength,
  MaxLength,
  ValidateIf,
  Matches,
} from 'class-validator';
import { Role } from '../../common/enums/roles.enum';

export class CreateUserDto {
  @IsOptional()
  @IsUUID()
  idUtilisateur?: string;

  @IsNotEmpty({ message: 'Le nom est obligatoire' })
  @IsString({ message: 'Le nom doit être du texte' })
  @MaxLength(100, { message: 'Le nom est trop long' })
  nom: string;

  @IsNotEmpty({ message: 'Le prénom est obligatoire' })
  @IsString({ message: 'Le prénom doit être du texte' })
  @MaxLength(100, { message: 'Le prénom est trop long' })
  prenom: string;

  @IsEmail({}, { message: "L'adresse e-mail est invalide" })
  email: string;

  @MinLength(8)
  @MaxLength(128)
  password: string;

  @IsEnum(Role)
  role: Role;

  @ValidateIf((o) => o.role === Role.FORMATEUR)
  @IsOptional()
  @IsString()
  @MaxLength(100)
  module?: string;

  @ValidateIf((o) => o.role === Role.stagiaire)
  @IsNotEmpty()
  @IsString()
  @MaxLength(30)
  numerostagiaire!: string;

  @ValidateIf((o) => o.role === Role.stagiaire)
  @IsNotEmpty()
  @IsString()
  @MaxLength(50)
  promotion!: string;

  @ValidateIf((o) => [Role.SUPER_ADMIN].includes(o.role))
  @IsNotEmpty()
  @IsString()
  @MaxLength(50)
  niveauAcces?: string;

  @ValidateIf((o) => [Role.SRIO, Role.SCQ].includes(o.role))
  @IsNotEmpty()
  @IsString()
  @MaxLength(120)
  region?: string;


  @ValidateIf((o) => [Role.SRIO, Role.SCQ, Role.DIRECTEUR, Role.GESTIONNAIRE, Role.FORMATEUR, Role.stagiaire].includes(o.role))
  @IsOptional()
  @IsUUID()
  idEtablissement?: string;

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
}
