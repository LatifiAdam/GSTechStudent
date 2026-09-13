import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { JwtModule } from '@nestjs/jwt';
import { PassportModule } from '@nestjs/passport';
import { TypeOrmModule } from '@nestjs/typeorm';

import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { JwtStrategy } from '../common/strategies/jwt.strategy';

import { Utilisateur } from '../entities/utilisateur.entity';
import { Formateur } from '../entities/formateur.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Administrateur } from '../entities/administrateur.entity';
import { Directeur } from '../entities/directeur.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { EmailService } from '../common/services/email.service';

@Module({
  imports: [
    TypeOrmModule.forFeature([
      Utilisateur,
      Formateur,
      stagiaire,
      Administrateur,
      Directeur,
      Gestionnaire,
    ]),

    PassportModule,

    JwtModule.registerAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => {
        const secret = config.get<string>('JWT_SECRET');

        if (!secret) {
          throw new Error('JWT_SECRET is not defined');
        }

        return {
          secret,
          signOptions: {
            algorithm: 'HS256',
            issuer: 'gstech-api',
            audience: 'gstech-mobile',
            expiresIn: '15m',
          },
        };
      },
    }),
  ],

  controllers: [AuthController],

  providers: [AuthService, JwtStrategy, EmailService],

  exports: [AuthService],
})
export class AuthModule {}