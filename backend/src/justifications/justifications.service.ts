import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';

import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { v4 as uuid } from 'uuid';

import {
  Justification,
  StatutJustification,
} from '../entities/justification.entity';

import {
  Presence,
  StatutPresence,
} from '../entities/presence.entity';

import { CreateJustificationDto } from './dto/create-justification.dto';
import { RefuseJustificationDto } from './dto/refuse-justification.dto';

import { NotificationsService } from '../notifications/notifications.service';
import { FileStorageService } from '../common/services/file-storage.service';
import { Role } from '../common/enums/roles.enum';

@Injectable()
export class JustificationsService {
  constructor(
    @InjectRepository(Justification)
    private readonly justificationRepo: Repository<Justification>,

    @InjectRepository(Presence)
    private readonly presenceRepo: Repository<Presence>,

    private readonly notifications: NotificationsService,

    private readonly storage: FileStorageService,
  ) {}

  // ============================================================
  // POST /justifications/upload
  // ============================================================

  async uploadAttachment(file: Express.Multer.File) {
    if (!file) {
      throw new BadRequestException(
        'Aucun fichier reçu',
      );
    }

    const allowed = [
      'application/pdf',
      'image/png',
      'image/jpeg',
    ];

    if (!allowed.includes(file.mimetype)) {
      throw new BadRequestException(
        'Format non supporté (PDF, PNG ou JPEG uniquement)',
      );
    }

    const signatures: Record<string, Buffer> = {
      'application/pdf': Buffer.from('%PDF-'),
      'image/png': Buffer.from([0x89, 0x50, 0x4e, 0x47]),
      'image/jpeg': Buffer.from([0xff, 0xd8, 0xff]),
    };
    const signature = signatures[file.mimetype];
    if (!signature || file.buffer.length < signature.length || !file.buffer.subarray(0, signature.length).equals(signature)) {
      throw new BadRequestException('Contenu de fichier invalide');
    }

    const ext = file.mimetype === 'application/pdf' ? 'pdf' : file.mimetype === 'image/png' ? 'png' : 'jpg';

    const path = await this.storage.save(
      'justifications',
      `${uuid()}.${ext}`,
      file.buffer,
    );

    return {
      pieceJointe: path,
    };
  }

  // ============================================================
  // POST /justifications
  // ============================================================

  async create(
    dto: CreateJustificationDto,
    stagiaireId: string,
  ) {
    const presence =
      await this.presenceRepo.findOne({
        where: {
          idPresence: dto.idPresence,
        },

        relations: [
          'stagiaire',
        ],
      });

    if (!presence) {
      throw new NotFoundException(
        'Présence introuvable',
      );
    }

    if (
      presence.stagiaire.idUtilisateur !==
      stagiaireId
    ) {
      throw new ForbiddenException(
        'Vous ne pouvez justifier que vos propres absences',
      );
    }

    if (
      presence.statut ===
      StatutPresence.PRESENT
    ) {
      throw new BadRequestException(
        'Seule une présence "absent" ou "retard" peut être justifiée',
      );
    }

    const existing =
      await this.justificationRepo.findOne({
        where: {
          presence: {
            idPresence: dto.idPresence,
          } as any,
        },
      });

    if (existing) {
      throw new BadRequestException(
        'Cette présence a déjà un justificatif',
      );
    }

    const justification =
      this.justificationRepo.create({
        motif: dto.motif,

        pieceJointe:
          dto.pieceJointe,

        statutJustification:
          StatutJustification.EN_ATTENTE,

        presence: {
          idPresence: dto.idPresence,
        } as any,
      });

    return this.justificationRepo.save(
      justification,
    );
  }

  // ============================================================
  // GET /justifications
  // ============================================================

  async findAll(
    statut?: string,
    coursId?: string,
    requesterId?: string,
    requesterRole?: Role,
  ) {
    const qb =
      this.justificationRepo
        .createQueryBuilder('j')

        .leftJoinAndSelect(
          'j.presence',
          'presence',
        )

        .leftJoinAndSelect(
          'presence.appel',
          'appel',
        )

        .leftJoinAndSelect(
          'presence.stagiaire',
          'stagiaire',
        )

        .leftJoinAndSelect(
          'appel.creneau',
          'creneau',
        )

        .leftJoinAndSelect(
          'creneau.affectation',
          'affectation',
        )

        .leftJoinAndSelect(
          'affectation.cours',
          'cours',
        )

        .leftJoinAndSelect(
          'affectation.classe',
          'classe',
        );

    if (requesterRole === Role.FORMATEUR) {
      qb.andWhere('affectation.id_formateur = :requesterId', { requesterId });
    }

    if (statut) {
      qb.andWhere(
        'j.statut_justification = :statut',
        {
          statut,
        },
      );
    }

    if (coursId) {
      qb.andWhere(
        'affectation.id_cours = :coursId',
        {
          coursId,
        },
      );
    }

    return qb.getMany();
  }

  // ============================================================
  // PATCH /justifications/:id/accept
  // ============================================================

  async accept(
    id: string,
    requesterId: string,
    requesterRole: Role,
  ) {
    const justification =
      await this.findOwnedJustification(
        id,
        requesterId,
        requesterRole,
      );

    if (justification.statutJustification !== StatutJustification.EN_ATTENTE) {
      throw new BadRequestException('Ce justificatif a déjà été traité');
    }

    justification.statutJustification =
      StatutJustification.ACCEPTEE;
    const attachment = justification.pieceJointe;
    justification.pieceJointe = null;

    await this.justificationRepo.save(
      justification,
    );
    await this.storage.remove(attachment);

    await this.notifications.notifyUsers(
      [
        justification.presence.stagiaire
          .idUtilisateur,
      ],

      'justification_traitee',

      'Votre justificatif d\'absence a été accepté.',
    );

    return justification;
  }

  // ============================================================
  // PATCH /justifications/:id/refuse
  // ============================================================

  async refuse(
    id: string,
    dto: RefuseJustificationDto,
    requesterId: string,
    requesterRole: Role,
  ) {
    const justification =
      await this.findOwnedJustification(
        id,
        requesterId,
        requesterRole,
      );

    if (justification.statutJustification !== StatutJustification.EN_ATTENTE) {
      throw new BadRequestException('Ce justificatif a déjà été traité');
    }

    justification.statutJustification =
      StatutJustification.REFUSEE;
    const attachment = justification.pieceJointe;
    justification.pieceJointe = null;

    await this.justificationRepo.save(
      justification,
    );
    await this.storage.remove(attachment);

    await this.notifications.notifyUsers(
      [
        justification.presence.stagiaire
          .idUtilisateur,
      ],

      'justification_traitee',

      `Votre justificatif d'absence a été refusé. Motif : ${dto.motif}`,
    );

    return justification;
  }

  // ============================================================
  // Vérification de l'formateur responsable
  // ============================================================

  private async findOwnedJustification(
    id: string,
    requesterId: string,
    requesterRole: Role,
  ) {
    const justification =
      await this.justificationRepo.findOne({
        where: {
          idJustification: id,
        },

        relations: [
          'presence',
          'presence.stagiaire',

          'presence.appel',

          'presence.appel.creneau',

          'presence.appel.creneau.affectation',

          'presence.appel.creneau.affectation.cours',

          'presence.appel.creneau.affectation.classe',
        ],
      });

    if (!justification) {
      throw new NotFoundException(
        'Justificatif introuvable',
      );
    }

    const affectation =
      justification.presence
        ?.appel
        ?.creneau
        ?.affectation;

    if (!affectation) {
      throw new NotFoundException(
        'Affectation du créneau introuvable',
      );
    }

    if (requesterRole === Role.FORMATEUR && affectation.idFormateur !== requesterId) {
      throw new ForbiddenException(
        'Seul l\'formateur responsable de cette affectation peut traiter ce justificatif',
      );
    }
    if (requesterRole !== Role.FORMATEUR && requesterRole !== Role.GESTIONNAIRE) {
      throw new ForbiddenException('Rôle non autorisé');
    }

    return justification;
  }
}