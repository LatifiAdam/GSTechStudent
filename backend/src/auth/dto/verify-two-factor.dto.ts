import { IsNotEmpty, IsString, Length, MaxLength } from 'class-validator';

export class VerifyTwoFactorDto {
  @IsNotEmpty()
  @IsString()
  @MaxLength(4096)
  challengeToken: string;

  @IsNotEmpty()
  @IsString()
  @Length(6, 6)
  code: string;
}
