import { NestFactory } from '@nestjs/core';
import { BadRequestException, ValidationError, ValidationPipe } from '@nestjs/common';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Vérification des variables d'environnement critiques
  if (!process.env.CORS_ORIGIN) {
    throw new Error('CORS_ORIGIN is not defined');
  }

  // URL de base /api/v1
  app.setGlobalPrefix('api/v1');
  app.use((req: any, res: any, next: () => void) => {
    res.setHeader('X-Content-Type-Options', 'nosniff');
    res.setHeader('X-Frame-Options', 'DENY');
    res.setHeader('Referrer-Policy', 'no-referrer');
    res.setHeader('Permissions-Policy', 'camera=(), microphone=(), geolocation=()');
    if (process.env.NODE_ENV === 'production') {
      res.setHeader('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
    }
    next();
  });


  // Validation systématique des DTO
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: {
        enableImplicitConversion: true,
      },
      forbidUnknownValues: true,
      exceptionFactory: (errors: ValidationError[]) => {
        const labels: Record<string, string> = {
          nom: 'Le nom',
          prenom: 'Le prénom',
          email: "L’adresse e-mail",
          password: 'Le mot de passe',
          numerostagiaire: 'Le numéro de stagiaire',
          promotion: 'La promotion',
          telephone: 'Le téléphone',
          cin: 'Le CIN',
          adresse: "L’adresse",
          role: 'Le rôle',
          region: 'La région',
          idEtablissement: "L’établissement",
          module: 'Le module',
          nomDocument: 'Le nom du document',
          description: 'La description',
        };

        const messages: string[] = [];
        for (const error of errors) {
          const field = labels[error.property] ?? `Le champ ${error.property}`;
          const constraints = error.constraints ?? {};
          if (constraints.isEmail) messages.push("L’adresse e-mail est invalide.");
          else if (constraints.isNotEmpty) messages.push(`${field} est obligatoire.`);
          else if (constraints.minLength) messages.push(`${field} est trop court.`);
          else if (constraints.maxLength) messages.push(`${field} est trop long.`);
          else if (constraints.isString) messages.push(`${field} doit être du texte.`);
          else if (constraints.isNumber) messages.push(`${field} doit être un nombre.`);
          else if (constraints.matches) messages.push(`${field} contient un format invalide.`);
          else if (constraints.isUUID) messages.push(`${field} est invalide.`);
          else if (constraints.isEnum) messages.push(`${field} contient une valeur invalide.`);
          else messages.push(`${field} est invalide.`);
        }

        return new BadRequestException({
          statusCode: 400,
          message: messages.length ? messages : ['Les données envoyées sont invalides.'],
          error: 'ValidationError',
        });
      },
    }),
  );

  // CORS restreint à l'application cliente
  const corsOrigin = process.env.CORS_ORIGIN || '*';
  app.enableCors({
    origin: corsOrigin,
    credentials: corsOrigin !== '*',
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization'],
  });

  const port = Number(process.env.PORT) || 3000;

  await app.listen(port, '0.0.0.0');

  console.log(`API démarrée sur http://localhost:${port}/api/v1`);
}

bootstrap();