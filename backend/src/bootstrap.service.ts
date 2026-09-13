import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import * as bcrypt from 'bcrypt';
import { randomUUID } from 'crypto';

import { Utilisateur } from './entities/utilisateur.entity';
import { Administrateur } from './entities/administrateur.entity';
import { Role } from './common/enums/roles.enum';

/**
 * Creates a temporary Super Admin required to access a completely empty
 * installation for the first time.
 *
 * The account is marked is_bootstrap=true and is automatically removed by
 * UsersService when the first real Super Admin is created.
 */
@Injectable()
export class BootstrapService implements OnModuleInit {
  private readonly logger = new Logger(BootstrapService.name);

  constructor(
    @InjectRepository(Utilisateur)
    private readonly utilisateurRepo: Repository<Utilisateur>,
    @InjectRepository(Administrateur)
    private readonly administrateurRepo: Repository<Administrateur>,
  ) {}

  async onModuleInit() {
    const userCount = await this.utilisateurRepo.count();

    // Never create a bootstrap account once any user exists.
    if (userCount > 0) return;

    const email = (process.env.BOOTSTRAP_SUPERADMIN_EMAIL || 'bootstrap@gstech.ma').trim();
    const password = process.env.BOOTSTRAP_SUPERADMIN_PASSWORD || 'ChangeMe!123456';
    const niveauAcces = process.env.BOOTSTRAP_SUPERADMIN_ACCESS_LEVEL || 'technical';

    const existing = await this.utilisateurRepo.findOne({ where: { email } });
    if (existing) return;

    const user = this.utilisateurRepo.create({
      idUtilisateur: randomUUID(),
      nom: 'Bootstrap',
      prenom: 'Super Admin',
      email,
      motDePasse: await bcrypt.hash(password, 12),
      role: Role.SUPER_ADMIN,
      region: null,
      isActive: true,
      isBootstrap: true,
    });

    await this.utilisateurRepo.manager.transaction(async (manager) => {
      await manager.save(Utilisateur, user);
      await manager.save(
        Administrateur,
        manager.create(Administrateur, {
          idUtilisateur: user.idUtilisateur,
          niveauAcces,
        }),
      );
    });

    this.logger.warn(
      `Bootstrap Super Admin created: ${email}. Create the permanent Super Admin immediately; the bootstrap account will then be deleted automatically.`,
    );
  }
}
