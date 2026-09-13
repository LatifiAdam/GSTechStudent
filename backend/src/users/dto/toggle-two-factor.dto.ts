import { IsBoolean } from 'class-validator';

export class ToggleTwoFactorDto {
  @IsBoolean()
  enabled: boolean;
}
