import { IsEnum, IsNotEmpty, IsString, MaxLength } from 'class-validator';
import { Plateforme } from '../../entities/device-token.entity';

/** POST /notifications/device-tokens — Phase 2, section 4.10 (nouveau). */
export class RegisterDeviceTokenDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(4096)
  token: string;

  @IsEnum(Plateforme)
  plateforme: Plateforme;
}
