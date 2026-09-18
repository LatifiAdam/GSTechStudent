import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';

import {
  InjectDataSource,
  InjectRepository,
} from '@nestjs/typeorm';

import {
  DataSource,
  Repository,
  QueryFailedError,
  Not,
} from 'typeorm';

import * as bcrypt from 'bcrypt';

import { Utilisateur } from '../entities/utilisateur.entity';
import { Formateur } from '../entities/formateur.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Administrateur } from '../entities/administrateur.entity';
import { Directeur } from '../entities/directeur.entity';
import { Gestionnaire } from '../entities/gestionnaire.entity';
import { Affectation } from '../entities/affectation.entity';
import { Etablissement } from '../entities/etablissement.entity';
import { AuditLog } from '../entities/audit-log.entity';

import { Role } from '../common/enums/roles.enum';
import { FileStorageService } from '../common/services/file-storage.service';

import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { QueryUsersDto } from './dto/query-users.dto';
import { ChangePasswordDto } from './dto/change-password.dto';
import { ToggleTwoFactorDto } from './dto/toggle-two-factor.dto';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(Utilisateur)
    private readonly utilisateurRepo: Repository<Utilisateur>,

    @InjectRepository(Formateur)
    private readonly formateurRepo: Repository<Formateur>,

    @InjectRepository(stagiaire)
    private readonly stagiaireRepo: Repository<stagiaire>,

    @InjectRepository(Administrateur)
    private readonly administrateurRepo: Repository<Administrateur>,

    @InjectRepository(Directeur)
    private readonly directeurRepo: Repository<Directeur>,

    @InjectRepository(Gestionnaire)
    private readonly gestionnaireRepo: Repository<Gestionnaire>,

    @InjectRepository(Etablissement)
    private readonly etablissementRepo: Repository<Etablissement>,

    @InjectRepository(AuditLog)
    private readonly auditRepo: Repository<AuditLog>,

    @InjectDataSource()
    private readonly dataSource: DataSource,

    private readonly storage: FileStorageService,
  ) {}

  // ============================================================
  // GET /users
  // ============================================================

  async findAll(query: QueryUsersDto, actorId?: string, actorRole?: Role) {
    const actor = actorId ? await this.utilisateurRepo.findOne({ where: { idUtilisateur: actorId } }) : null;
    const actorG = actorId ? await this.gestionnaireRepo.findOne({ where: { idUtilisateur: actorId } }) : null;
    const actorD = actorId ? await this.directeurRepo.findOne({ where: { idUtilisateur: actorId } }) : null;
    const scopeRegion = [Role.SRIO, Role.SCQ].includes(actorRole as Role) ? actor?.region : null;
    const scopeEfp = [Role.DIRECTEUR, Role.GESTIONNAIRE].includes(actorRole as Role) ? (actorD?.etablissement as any)?.idEtablissement ?? actorG?.idEtablissement : null;
    if (!query.role) {
      const users = await this.utilisateurRepo.find({ where: { isActive: true } });

      const rows = await Promise.all(users.map((user) => this.buildUserResponse(user)));
      return rows.filter((u: any) => this.inScopeResponse(u, actorRole, scopeRegion, scopeEfp));
    }

    switch (query.role) {
      // --------------------------------------------------------
      // TEACHERS
      // --------------------------------------------------------

      case Role.FORMATEUR: {
        const formateurs =
          await this.formateurRepo.find({
            relations: ['utilisateur'],
          });

        const rows = formateurs.map((formateur) => ({
          idUtilisateur: formateur.idUtilisateur,

          nom: formateur.utilisateur.nom,

          prenom: formateur.utilisateur.prenom,

          email: formateur.utilisateur.email,

          dateCreation:
            formateur.utilisateur.dateCreation,

          cin: formateur.utilisateur.cin,

          telephone:
            formateur.utilisateur.telephone,

          adresse:
            formateur.utilisateur.adresse,

          role: Role.FORMATEUR,

          module: formateur.module,
          idEtablissement: formateur.idEtablissement,
        }));
        return rows.filter((u: any) => this.inScopeResponse(u, actorRole, scopeRegion, scopeEfp));
      }

      // --------------------------------------------------------
      // STUDENTS
      // --------------------------------------------------------

      case Role.stagiaire: {
        const stagiaires =
          await this.stagiaireRepo.find({
            relations: [
              'utilisateur',
              'classe',
            ],
          });

        const rows = stagiaires.map((stagiaire) => ({
          idUtilisateur:
            stagiaire.idUtilisateur,

          nom:
            stagiaire.utilisateur.nom,

          prenom:
            stagiaire.utilisateur.prenom,

          email:
            stagiaire.utilisateur.email,

          dateCreation:
            stagiaire.utilisateur.dateCreation,

          cin:
            stagiaire.utilisateur.cin,

          telephone:
            stagiaire.utilisateur.telephone,

          adresse:
            stagiaire.utilisateur.adresse,

          role: Role.stagiaire,

          numerostagiaire:
            stagiaire.numerostagiaire,

          promotion:
            stagiaire.promotion,

          idClasse:
            stagiaire.idClasse,

          idEtablissement:
            stagiaire.idEtablissement,

          classe: stagiaire.classe
            ? {
                idClasse:
                  stagiaire.classe.idClasse,

                nomClasse:
                  stagiaire.classe.nomClasse,
              }
            : null,
        }));
        return rows.filter((u: any) => this.inScopeResponse(u, actorRole, scopeRegion, scopeEfp));
      }

      // --------------------------------------------------------
      // ADMINISTRATORS
      // --------------------------------------------------------

      case Role.SUPER_ADMIN: {
        const administrateurs =
          await this.administrateurRepo.find({
            relations: ['utilisateur'],
          });

        return administrateurs.map(
          (administrateur) => ({
            idUtilisateur:
              administrateur.idUtilisateur,

            nom:
              administrateur.utilisateur.nom,

            prenom:
              administrateur.utilisateur.prenom,

            email:
              administrateur.utilisateur.email,

            dateCreation:
              administrateur.utilisateur.dateCreation,

            cin:
              administrateur.utilisateur.cin,

            telephone:
              administrateur.utilisateur.telephone,

            adresse:
              administrateur.utilisateur.adresse,

            role:
              Role.SUPER_ADMIN,

            niveauAcces:
              administrateur.niveauAcces,
          }),
        );
      }

      case Role.DIRECTEUR:
      case Role.GESTIONNAIRE: {
        const users = await this.utilisateurRepo.find({ where: { role: query.role } });
        const rows = await Promise.all(users.map((user) => this.buildUserResponse(user)));
        return rows.filter((u: any) => this.inScopeResponse(u, actorRole, scopeRegion, scopeEfp));
      }

      default:
        return [];
    }
  }

  // ============================================================
  // GET /users/:id
  // ============================================================

  async findOne(id: string) {
    const user =
      await this.utilisateurRepo.findOne({
        where: {
          idUtilisateur: id,
        },
      });

    if (!user) {
      throw new NotFoundException(
        'Utilisateur introuvable',
      );
    }

    const [
      formateur,
      stagiaire,
      administrateur,
      directeur,
      gestionnaire,
    ] = await Promise.all([
      this.formateurRepo.findOne({
        where: {
          idUtilisateur: id,
        },
      }),

      this.stagiaireRepo.findOne({
        where: {
          idUtilisateur: id,
        },
        relations: ['classe'],
      }),

      this.administrateurRepo.findOne({ where: { idUtilisateur: id } }),
      this.directeurRepo.findOne({ where: { idUtilisateur: id } }),
      this.gestionnaireRepo.findOne({ where: { idUtilisateur: id } }),
    ]);

    // Resolve role-specific accounts before generic student/formateur data.
    // A legacy account can have a stale `utilisateur.role` value, so the
    // dedicated role tables remain authoritative.
    if (administrateur) {
      return { ...user, role: Role.SUPER_ADMIN, niveauAcces: administrateur.niveauAcces };
    }
    if (directeur) {
      return { ...user, role: Role.DIRECTEUR };
    }
    if (gestionnaire) {
      return { ...user, role: Role.GESTIONNAIRE };
    }

    // --------------------------------------------------------
    // TEACHER
    // --------------------------------------------------------

    if (formateur) {
      return {
        ...user,

        role: Role.FORMATEUR,

        module:
          formateur.module,
      };
    }

    // --------------------------------------------------------
    // STUDENT
    // --------------------------------------------------------

    if (stagiaire) {
      return {
        ...user,

        role: Role.stagiaire,

        numerostagiaire:
          stagiaire.numerostagiaire,

        promotion:
          stagiaire.promotion,

        idClasse:
          stagiaire.idClasse,

        classe: stagiaire.classe
          ? {
              idClasse:
                stagiaire.classe.idClasse,

              nomClasse:
                stagiaire.classe.nomClasse,
            }
          : null,
      };
    }

    return { ...user, role: user.role ?? null, isActive: user.isActive };
  }

  // ============================================================
  // POST /users
  // ============================================================

  async create(dto: CreateUserDto, creatorRole: Role = Role.SUPER_ADMIN, creatorId?: string) {
    // Validate/sanitize before opening the transaction so invalid input never
    // reaches a NOT NULL / UNIQUE database constraint.
    dto.nom = dto.nom?.trim();
    dto.prenom = dto.prenom?.trim();
    dto.email = dto.email?.trim();
    dto.password = dto.password?.trim();

    if (!dto.nom || !dto.prenom || !dto.email || !dto.password) {
      throw new BadRequestException('Nom, prénom, email et mot de passe sont obligatoires');
    }

    if (dto.role === Role.stagiaire) {
      const numeroStagiaire = dto.numerostagiaire?.trim();
      const promotion = dto.promotion?.trim();

      if (!numeroStagiaire || !promotion) {
        throw new BadRequestException('Le numéro de stagiaire et la promotion sont obligatoires');
      }

      dto.numerostagiaire = numeroStagiaire;
      dto.promotion = promotion;
    }

    try {
      return await this.dataSource.transaction(
        async (manager) => {
        if (dto.role === Role.SUPER_ADMIN && creatorRole !== Role.SUPER_ADMIN) {
          throw new BadRequestException('Seul un Super Admin peut créer un autre Super Admin');
        }
        if (creatorRole === Role.DIRECTEUR && ![Role.FORMATEUR].includes(dto.role)) {
          throw new BadRequestException('Un Directeur peut uniquement créer un Formateur');
        }
        if (creatorRole === Role.GESTIONNAIRE && dto.role !== Role.stagiaire) {
          throw new BadRequestException('Un Gestionnaire peut uniquement créer un Stagiaire');
        }
        if (creatorRole === Role.SRIO && dto.role !== Role.GESTIONNAIRE) {
          throw new BadRequestException('Un SRIO peut uniquement créer un Gestionnaire');
        }
        if (creatorRole === Role.SCQ && dto.role !== Role.DIRECTEUR) {
          throw new BadRequestException('Un SCQ peut uniquement créer un Directeur');
        }
        if (creatorRole === Role.DF && ![Role.SRIO, Role.SCQ].includes(dto.role)) {
          throw new BadRequestException('Le DF peut uniquement créer un SRIO ou un SCQ');
        }
        if ((creatorRole === Role.SRIO && dto.role === Role.GESTIONNAIRE) || (creatorRole === Role.SCQ && dto.role === Role.DIRECTEUR)) {
          const creator = await manager.findOne(Utilisateur, { where: { idUtilisateur: creatorId } });
          if (!creator?.region) throw new BadRequestException('La région du compte créateur est introuvable');
          dto.region = creator.region;
        }
        if (creatorRole === Role.DIRECTEUR && false) {
          throw new BadRequestException('Un Directeur peut uniquement créer un Gestionnaire, un Formateur ou un Étudiant');
        }
        if ([Role.SRIO, Role.SCQ].includes(dto.role)) {
          if (!dto.region) {
            throw new BadRequestException('La région est obligatoire pour un SRIO/SCQ');
          }

          // The database stores the canonical region code (RSK, CS, TTA, ...).
          // Accept both the code and the official French region name from clients,
          // then always persist the code. This prevents FK failures when the
          // Android form sends a display name such as "Rabat-Salé-Kénitra".
          const requestedRegion = dto.region.trim();
          const regionRow = await manager.query(
            `SELECT region FROM region WHERE region = ? OR nom = ? LIMIT 1`,
            [requestedRegion, requestedRegion],
          );

          if (!regionRow?.length) {
            throw new BadRequestException('Région invalide. Sélectionnez une des dix régions officielles.');
          }

          dto.region = regionRow[0].region;

          const existingRegional = await manager.findOne(Utilisateur, {
            where: { role: dto.role, region: dto.region, isActive: true },
          });

          if (existingRegional) {
            throw new BadRequestException(`Un ${dto.role.toUpperCase()} existe déjà pour cette région`);
          }
        }

        let targetEfp: Etablissement | null = null;
        if (!dto.idEtablissement && [Role.DIRECTEUR, Role.GESTIONNAIRE].includes(creatorRole)) {
          const creatorEfp = creatorRole === Role.DIRECTEUR
            ? await manager.findOne(Directeur, { where: { idUtilisateur: creatorId }, relations: ['etablissement'] })
            : await manager.findOne(Gestionnaire, { where: { idUtilisateur: creatorId } });
          const inheritedEfpId = creatorRole === Role.DIRECTEUR ? (creatorEfp as any)?.etablissement?.idEtablissement : (creatorEfp as any)?.idEtablissement;
          if (inheritedEfpId) dto.idEtablissement = inheritedEfpId;
        }
        // SRIO and SCQ create their regional users before the EFP assignment step.
        // The establishment is assigned later from the regional EFP screen.
        if (dto.idEtablissement) {
          targetEfp = await manager.findOne(Etablissement, { where: { idEtablissement: dto.idEtablissement } });
          if (!targetEfp) throw new BadRequestException('Établissement introuvable');
          if ([Role.SRIO, Role.SCQ].includes(creatorRole) && targetEfp.region !== (await manager.findOne(Utilisateur,{where:{idUtilisateur:creatorId}}))?.region) {
            throw new BadRequestException('Établissement hors de votre région');
          }
          if ([Role.DIRECTEUR, Role.GESTIONNAIRE].includes(creatorRole)) {
            const creator = await manager.findOne(Utilisateur,{where:{idUtilisateur:creatorId}});
            const creatorEfp = creatorRole === Role.DIRECTEUR ? await manager.findOne(Directeur,{where:{idUtilisateur:creatorId}, relations:['etablissement']}) : await manager.findOne(Gestionnaire,{where:{idUtilisateur:creatorId}});
            const creatorEfpId = creatorRole === Role.DIRECTEUR ? (creatorEfp as any)?.etablissement?.idEtablissement : (creatorEfp as any)?.idEtablissement;
            if (!creator || creatorEfpId !== dto.idEtablissement) throw new BadRequestException('Établissement hors de votre périmètre');
          }
        }

        if (targetEfp && [Role.DIRECTEUR, Role.GESTIONNAIRE, Role.FORMATEUR, Role.stagiaire].includes(dto.role)) { dto.region = targetEfp.region; }

        // ------------------------------------------------------
        // EMAIL
        // ------------------------------------------------------

        const existing =
          await manager.findOne(
            Utilisateur,
            {
              where: {
                email: dto.email,
              },
            },
          );

        if (existing) {
          throw new BadRequestException(
            'Un compte existe déjà avec cet email',
          );
        }

        if (dto.role === Role.stagiaire) {
          const existingNumero = await manager.findOne(stagiaire, {
            where: { numerostagiaire: dto.numerostagiaire },
          });

          if (existingNumero) {
            throw new BadRequestException('Ce numéro de stagiaire existe déjà');
          }
        }

        // ------------------------------------------------------
        // USER
        //
        // If the admin supplies an ID, keep it; otherwise TypeORM generates the UUID.
        // ------------------------------------------------------

        const utilisateur =
          manager.create(
            Utilisateur,
            {
              ...(dto.idUtilisateur ? { idUtilisateur: dto.idUtilisateur } : {}),
              nom: dto.nom,
              prenom: dto.prenom,
              email: dto.email,

              motDePasse:
                await bcrypt.hash(
                  dto.password,
                  12,
                ),

              cin:
                dto.cin ?? null,

              telephone:
                dto.telephone ?? null,

              adresse:
                dto.adresse ?? null,

              role: dto.role,
              isActive: true,
              isBootstrap: false,
              region: dto.region ?? null,
            },
          );

        await manager.save(
          utilisateur,
        );

        // At this point TypeORM has generated:
        //
        // utilisateur.idUtilisateur
        //
        // Example:
        //
        // 550e8400-e29b-41d4-a716-446655440000

        if (
          !utilisateur.idUtilisateur ||
          !this.isUuid(
            utilisateur.idUtilisateur,
          )
        ) {
          throw new BadRequestException(
            'Le serveur n’a pas généré un UUID valide pour l’utilisateur',
          );
        }

        // ------------------------------------------------------
        // TEACHER
        // ------------------------------------------------------

        switch (dto.role) {
          case Role.FORMATEUR: {
            const formateur = manager.create(Formateur, { idUtilisateur: utilisateur.idUtilisateur, module: dto.module, idEtablissement: dto.idEtablissement ?? null });

            await manager.save(
              formateur,
            );

            break;
          }

          // ----------------------------------------------------
          // STUDENT
          // ----------------------------------------------------

          case Role.stagiaire: {
            const stagiaireEntity = manager.create(stagiaire, {
              idUtilisateur: utilisateur.idUtilisateur,
              numerostagiaire: dto.numerostagiaire!,
              promotion: dto.promotion!,
              idClasse: null,
              idEtablissement: dto.idEtablissement ?? null,
            });

            await manager.save(stagiaireEntity);
            break;
          }

          // ----------------------------------------------------
          // ADMINISTRATOR
          // ----------------------------------------------------

          case Role.DIRECTEUR: {
            const directeur = manager.create(Directeur, { idUtilisateur: utilisateur.idUtilisateur });
            await manager.save(directeur);
            if (dto.idEtablissement) {
              await manager.update(Etablissement, { idEtablissement: dto.idEtablissement }, { idDirecteur: utilisateur.idUtilisateur });
            }
            break;
          }

          case Role.SRIO:
          case Role.SCQ:
          case Role.DF: {
            break;
          }

          case Role.GESTIONNAIRE: {
            const gestionnaire = manager.create(Gestionnaire, { idUtilisateur: utilisateur.idUtilisateur, idEtablissement: dto.idEtablissement ?? null });
            await manager.save(gestionnaire);
            break;
          }

          case Role.SUPER_ADMIN: {
            if (!dto.niveauAcces) {
              throw new BadRequestException(
                'niveauAcces requis pour un administrateur',
              );
            }

            const administrateur =
              manager.create(
                Administrateur,
                {
                  idUtilisateur:
                    utilisateur.idUtilisateur,

                  niveauAcces:
                    dto.niveauAcces,
                },
              );

            await manager.save(
              administrateur,
            );

            // The temporary bootstrap account exists only until the first
            // permanent Super Admin has been created successfully.
            await manager.delete(Utilisateur, {
              role: Role.SUPER_ADMIN,
              isBootstrap: true,
              idUtilisateur: Not(utilisateur.idUtilisateur),
            });

            break;
          }

          default:
            throw new BadRequestException(
              'Rôle utilisateur invalide',
            );
        }

        await manager.save(AuditLog, manager.create(AuditLog, { actorId: creatorId ?? null, actorRole: creatorRole, action: 'CREATE', entityType: 'utilisateur', entityId: utilisateur.idUtilisateur, region: dto.region ?? targetEfp?.region ?? null, idEtablissement: dto.idEtablissement ?? null, oldValue: null, newValue: { role: dto.role }, ipAddress: null }));

        return {
          ...utilisateur,

          role:
            dto.role,
        };
      },
    );
    } catch (error) {
      if (error instanceof QueryFailedError) {
        const driverError = error.driverError as any;
        if (driverError?.code === 'ER_DUP_ENTRY') {
          const message = String(driverError?.sqlMessage ?? '');
          if (message.includes('numero_stagiaire')) {
            throw new BadRequestException('Ce numéro de stagiaire existe déjà');
          }
          if (message.includes('email')) {
            throw new BadRequestException('Un compte existe déjà avec cet email');
          }
          if (message.includes('cin')) {
            throw new BadRequestException('Ce CIN existe déjà');
          }
          throw new BadRequestException('Données déjà utilisées ou contrainte en conflit');
        }
      }
      throw error;
    }
  }

  // ============================================================
  // PATCH /users/:id
  // ============================================================

  async canFormateurViewStudent(formateurId: string, studentId: string) {
    const student = await this.stagiaireRepo.findOne({ where: { idUtilisateur: studentId } });
    if (!student) return false;
    return !!(await this.dataSource.getRepository(Affectation).findOne({ where: { idFormateur: formateurId, idClasse: student.idClasse! } }));
  }

  async canManageUserInEstablishment(actorId: string, targetId: string, actorRole: Role): Promise<boolean> {
    if (![Role.DIRECTEUR, Role.GESTIONNAIRE].includes(actorRole)) return false;

    const actorG = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: actorId } });
    const actorD = await this.directeurRepo.findOne({ where: { idUtilisateur: actorId }, relations: ['etablissement'] });
    const actorEfpId = actorRole === Role.DIRECTEUR
      ? actorD?.etablissement?.idEtablissement
      : actorG?.idEtablissement;
    if (!actorEfpId) return false;

    const targetG = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: targetId } });
    const targetF = await this.formateurRepo.findOne({ where: { idUtilisateur: targetId } });
    const targetS = await this.stagiaireRepo.findOne({ where: { idUtilisateur: targetId } });
    const targetD = await this.directeurRepo.findOne({ where: { idUtilisateur: targetId }, relations: ['etablissement'] });

    const targetEfpId = targetG?.idEtablissement
      ?? targetF?.idEtablissement
      ?? targetS?.idEtablissement
      ?? targetD?.etablissement?.idEtablissement;

    if (actorRole === Role.GESTIONNAIRE && targetS) {
      return targetEfpId === actorEfpId;
    }
    if (actorRole === Role.DIRECTEUR && targetF) {
      return targetEfpId === actorEfpId;
    }
    return targetEfpId === actorEfpId;
  }

  async update(
    id: string,
    dto: UpdateUserDto,
  ) {
    const user =
      await this.utilisateurRepo.findOne({
        where: {
          idUtilisateur: id,
        },
      });

    if (!user) {
      throw new NotFoundException(
        'Utilisateur introuvable',
      );
    }

    // --------------------------------------------------------
    // EMAIL
    // --------------------------------------------------------

    if (
      dto.email &&
      dto.email !== user.email
    ) {
      const existing =
        await this.utilisateurRepo.findOne({
          where: {
            email: dto.email,
          },
        });

      if (
        existing &&
        existing.idUtilisateur !== id
      ) {
        throw new BadRequestException(
          'Un compte existe déjà avec cet email',
        );
      }
    }

    // --------------------------------------------------------
    // BASIC USER DATA
    // --------------------------------------------------------

    Object.assign(user, {
      nom:
        dto.nom ?? user.nom,

      prenom:
        dto.prenom ?? user.prenom,

      email:
        dto.email ?? user.email,

      cin:
        dto.cin ?? user.cin,

      telephone:
        dto.telephone ??
        user.telephone,

      adresse:
        dto.adresse ??
        user.adresse,
    });

    await this.utilisateurRepo.save(
      user,
    );

    // --------------------------------------------------------
    // TEACHER
    // --------------------------------------------------------

    if (
      dto.module !== undefined
    ) {
      const formateur =
        await this.formateurRepo.findOne({
          where: {
            idUtilisateur: id,
          },
        });

      if (formateur) {
        formateur.module =
          dto.module;

        await this.formateurRepo.save(
          formateur,
        );
      }
    }

    // --------------------------------------------------------
    // STUDENT
    // --------------------------------------------------------

    if (
      dto.numerostagiaire !==
        undefined ||
      dto.promotion !==
        undefined
    ) {
      const stagiaire =
        await this.stagiaireRepo.findOne({
          where: {
            idUtilisateur: id,
          },
        });

      if (stagiaire) {
        if (
          dto.numerostagiaire !==
          undefined
        ) {
          stagiaire.numerostagiaire =
            dto.numerostagiaire;
        }

        if (
          dto.promotion !==
          undefined
        ) {
          stagiaire.promotion =
            dto.promotion;
        }

        await this.stagiaireRepo.save(
          stagiaire,
        );
      }
    }

    // --------------------------------------------------------
    // ADMINISTRATOR
    // --------------------------------------------------------

    if (
      dto.niveauAcces !==
      undefined
    ) {
      const administrateur =
        await this.administrateurRepo.findOne({
          where: {
            idUtilisateur: id,
          },
        });

      if (administrateur) {
        administrateur.niveauAcces =
          dto.niveauAcces;

        await this.administrateurRepo.save(
          administrateur,
        );
      }
    }

    return this.findOne(id);
  }

  async setTwoFactor(id: string, dto: ToggleTwoFactorDto) {
    const user = await this.utilisateurRepo.findOne({ where: { idUtilisateur: id } });
    if (!user) throw new NotFoundException('Utilisateur introuvable');
    user.twoFactorEnabled = dto.enabled;
    if (!dto.enabled) { user.twoFactorCodeHash = null; user.twoFactorCodeExpiresAt = null; }
    await this.utilisateurRepo.save(user);
    return { idUtilisateur: id, twoFactorEnabled: user.twoFactorEnabled };
  }

  async changePassword(id: string, dto: ChangePasswordDto) {
    const user = await this.utilisateurRepo.createQueryBuilder('u')
      .addSelect('u.motDePasse')
      .where('u.id_utilisateur = :id', { id })
      .getOne();
    if (!user) throw new NotFoundException('Utilisateur introuvable');
    const valid = await bcrypt.compare(dto.currentPassword, user.motDePasse);
    if (!valid) throw new BadRequestException('Mot de passe actuel incorrect');
    user.motDePasse = await bcrypt.hash(dto.newPassword, 12);
    await this.utilisateurRepo.save(user);
    return { success: true };
  }

  // ============================================================
  // DELETE /users/:id
  // ============================================================

  async remove(id: string, requesterRole: Role = Role.SUPER_ADMIN, actorId?: string) {
    const user = await this.utilisateurRepo.findOne({ where: { idUtilisateur: id } });
    if (!user) throw new NotFoundException('Utilisateur introuvable');
    if (actorId && [Role.SRIO, Role.SCQ].includes(requesterRole)) {
      const actor = await this.utilisateurRepo.findOne({ where: { idUtilisateur: actorId } });
      if (actor?.region && user.region !== actor.region) throw new BadRequestException('Utilisateur hors de votre région');
    }
    if (actorId && [Role.DIRECTEUR, Role.GESTIONNAIRE].includes(requesterRole)) {
      const actorG = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: actorId } });
      const actorD = await this.directeurRepo.findOne({ where: { idUtilisateur: actorId }, relations: ['etablissement'] });
      const actorEfp = requesterRole === Role.DIRECTEUR ? actorD?.etablissement?.idEtablissement : actorG?.idEtablissement;
      const targetG = await this.gestionnaireRepo.findOne({ where: { idUtilisateur: id } });
      const targetF = await this.formateurRepo.findOne({ where: { idUtilisateur: id } });
      const targetS = await this.stagiaireRepo.findOne({ where: { idUtilisateur: id } });
      const targetD = await this.directeurRepo.findOne({ where: { idUtilisateur: id }, relations: ['etablissement'] });
      const targetEfp = targetG?.idEtablissement ?? targetF?.idEtablissement ?? targetS?.idEtablissement ?? targetD?.etablissement?.idEtablissement;
      if (actorEfp && targetEfp !== actorEfp) throw new BadRequestException('Utilisateur hors de votre établissement');
    }
    user.isActive = false;
    await this.utilisateurRepo.save(user);
    await this.auditRepo.save(this.auditRepo.create({ actorId: actorId ?? null, actorRole: requesterRole, action: 'DEACTIVATE', entityType: 'utilisateur', entityId: id, region: user.region, idEtablissement: null, oldValue: { isActive: true, role: user.role }, newValue: { isActive: false, role: user.role }, ipAddress: null }));
    return { success: true, deactivated: true };
  }

  private inScopeResponse(user: any, actorRole?: Role, region?: string | null, efpId?: string | null): boolean {
    if (!actorRole || [Role.SUPER_ADMIN, Role.DF].includes(actorRole)) return true;
    if ([Role.SRIO, Role.SCQ].includes(actorRole)) return !region || user.region === region;
    if ([Role.DIRECTEUR, Role.GESTIONNAIRE].includes(actorRole)) return !efpId || user.idEtablissement === efpId;
    return user.role === actorRole;
  }

  // ============================================================
  // INTERNAL RESPONSE
  // ============================================================

  private async buildUserResponse(user: Utilisateur) {
    const [formateur, stagiaire, administrateur, directeur, gestionnaire] = await Promise.all([
      this.formateurRepo.findOne({ where: { idUtilisateur: user.idUtilisateur } }),
      this.stagiaireRepo.findOne({ where: { idUtilisateur: user.idUtilisateur } }),
      this.administrateurRepo.findOne({ where: { idUtilisateur: user.idUtilisateur } }),
      this.directeurRepo.findOne({ where: { idUtilisateur: user.idUtilisateur }, relations: ['etablissement'] }),
      this.gestionnaireRepo.findOne({ where: { idUtilisateur: user.idUtilisateur } }),
    ]);

    let role: Role | null = user.role ?? null;
    if (role === Role.stagiaire && !stagiaire) role = null;
    if (role === Role.FORMATEUR && !formateur) role = null;
    if (role === Role.SUPER_ADMIN && !administrateur) role = null;
    if (role === Role.DIRECTEUR && !directeur) role = null;
    if (role === Role.GESTIONNAIRE && !gestionnaire) role = null;

    if (!role) {
      if (administrateur) role = Role.SUPER_ADMIN;
      else if (directeur) role = Role.DIRECTEUR;
      else if (gestionnaire) role = Role.GESTIONNAIRE;
      else if (formateur) role = Role.FORMATEUR;
      else if (stagiaire) role = Role.stagiaire;
    }

    return {
      idUtilisateur: user.idUtilisateur,
      nom: user.nom,
      prenom: user.prenom,
      email: user.email,
      dateCreation: user.dateCreation,
      cin: user.cin,
      telephone: user.telephone,
      adresse: user.adresse,
      region: user.region,
      isActive: user.isActive,
      idEtablissement: formateur?.idEtablissement ?? stagiaire?.idEtablissement ?? gestionnaire?.idEtablissement ?? directeur?.etablissement?.idEtablissement ?? null,
      role,
      module: formateur?.module ?? null,
      numerostagiaire: stagiaire?.numerostagiaire ?? null,
      promotion: stagiaire?.promotion ?? null,
      idClasse: stagiaire?.idClasse ?? null,
      niveauAcces: administrateur?.niveauAcces ?? null,
      profileImageKey: user.profileImageKey ?? null,
    };
  }

  // ============================================================
  // UUID VALIDATION
  // ============================================================

  private isUuid(value: string): boolean {
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
      value,
    );
  }

  async updateProfileImage(id: string, file: Express.Multer.File) {
    if (!file) throw new BadRequestException('Veuillez sélectionner une image de profil');

    const allowed: Record<string, string> = {
      'image/jpeg': 'jpg',
      'image/png': 'png',
      'image/webp': 'webp',
    };
    const ext = allowed[file.mimetype];
    if (!ext) throw new BadRequestException('Format d’image non supporté (JPG, PNG ou WEBP uniquement)');
    if (!file.buffer?.length) throw new BadRequestException('L’image est vide');

    const signatures: Record<string, Buffer> = {
      'image/png': Buffer.from([0x89, 0x50, 0x4e, 0x47]),
      'image/jpeg': Buffer.from([0xff, 0xd8, 0xff]),
      'image/webp': Buffer.from('RIFF'),
    };
    const signature = signatures[file.mimetype];
    if (!file.buffer.subarray(0, signature.length).equals(signature)) {
      throw new BadRequestException('Le contenu du fichier ne correspond pas à son format déclaré');
    }
    if (file.mimetype === 'image/webp' && file.buffer.length >= 12 && file.buffer.subarray(8, 12).toString() !== 'WEBP') {
      throw new BadRequestException('Image WEBP invalide');
    }

    const user = await this.utilisateurRepo.findOne({ where: { idUtilisateur: id } });
    if (!user) throw new NotFoundException('Utilisateur introuvable');

    const key = await this.storage.save('profiles', `${id}.${ext}`, file.buffer, file.mimetype);
    const oldKey = user.profileImageKey;
    user.profileImageKey = key;
    await this.utilisateurRepo.save(user);

    if (oldKey && oldKey !== key) await this.storage.remove(oldKey).catch(() => undefined);

    return {
      success: true,
      profileImageKey: key,
    };
  }

  async getProfileImage(id: string): Promise<{ buffer: Buffer; contentType: string }> {
    const user = await this.utilisateurRepo.findOne({ where: { idUtilisateur: id } });
    if (!user) throw new NotFoundException('Utilisateur introuvable');
    if (!user.profileImageKey) throw new NotFoundException('Aucune photo de profil enregistrée');

    const contentType = user.profileImageKey.toLowerCase().endsWith('.png')
      ? 'image/png'
      : user.profileImageKey.toLowerCase().endsWith('.webp')
        ? 'image/webp'
        : 'image/jpeg';

    return {
      buffer: await this.storage.read(user.profileImageKey),
      contentType,
    };
  }

}