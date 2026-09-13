import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

export class RefreshTokenDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(4096)
  refreshToken: string;
}
