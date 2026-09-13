import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';

import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

import { Appel } from '../entities/appel.entity';
import {
  Presence,
  StatutPresence,
} from '../entities/presence.entity';

import { Justification } from '../entities/justification.entity';
import { DemandeDocument } from '../entities/demande-document.entity';
import { Annonce } from '../entities/annonce.entity';
import { Affectation } from '../entities/affectation.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { Role } from '../common/enums/roles.enum';

import { PdfService } from '../common/services/pdf.service';
import { ExcelService } from '../common/services/excel.service';

@Injectable()
export class ReportsService {
  constructor(
    @InjectRepository(Appel)
    private readonly appelRepo: Repository<Appel>,

    @InjectRepository(Presence)
    private readonly presenceRepo: Repository<Presence>,

    @InjectRepository(Justification)
    private readonly justificationRepo: Repository<Justification>,

    @InjectRepository(DemandeDocument)
    private readonly demandeRepo: Repository<DemandeDocument>,

    @InjectRepository(Annonce)
    private readonly annonceRepo: Repository<Annonce>,

    @InjectRepository(Affectation)
    private readonly affectationRepo: Repository<Affectation>,

    @InjectRepository(stagiaire)
    private readonly stagiaireRepo: Repository<stagiaire>,

    private readonly pdf: PdfService,

    private readonly excel: ExcelService,
  ) {}

  // ============================================================
  // GET /reports/attendance
  // ============================================================

  async attendance(periode?: string) {
    const qb = this.presenceRepo
      .createQueryBuilder('p')
      .select('p.statut', 'statut')
      .addSelect('COUNT(*)', 'total')
      .groupBy('p.statut');

    if (periode) {
      qb.leftJoin(
        'p.appel',
        'appel',
      ).andWhere(
        'DATE_FORMAT(appel.date_heure, "%Y-%m") = :periode',
        { periode },
      );
    }

    const rows =
      await qb.getRawMany();

    const total =
      rows.reduce(
        (sum, r) =>
          sum + Number(r.total),
        0,
      );

    return {
      periode:
        periode ?? 'toutes périodes',

      total,

      parStatut:
        rows.map((r) => ({
          statut: r.statut,

          total: Number(
            r.total,
          ),

          pourcentage: total
            ? Math.round(
                (Number(r.total) /
                  total) *
                  1000,
              ) / 10
            : 0,
        })),
    };
  }

  // ============================================================
  // GET /reports/courses/:id
  // ============================================================

  async courseReport(id: string, requesterId: string, requesterRole: Role) {
    if (requesterRole === Role.FORMATEUR) {
      const allowed = await this.affectationRepo.exist({ where: { idCours: id, idFormateur: requesterId } });
      if (!allowed) throw new ForbiddenException('Vous ne pouvez consulter que les cours qui vous sont affectés');
    }

    const appels =
      await this.appelRepo
        .createQueryBuilder('appel')
        .innerJoin(
          'appel.creneau',
          'creneau',
        )
        .innerJoin(
          'creneau.affectation',
          'affectation',
        )
        .where(
          'affectation.id_cours = :id',
          { id },
        )
        .getMany();

    const presences =
      await this.presenceRepo
        .createQueryBuilder('p')
        .innerJoin(
          'p.appel',
          'appel',
        )
        .innerJoin(
          'appel.creneau',
          'creneau',
        )
        .innerJoin(
          'creneau.affectation',
          'affectation',
        )
        .leftJoinAndSelect(
          'p.stagiaire',
          'stagiaire',
        )
        .where(
          'affectation.id_cours = :id',
          { id },
        )
        .getMany();

    return this.buildReport(
      `Rapport du cours ${id}`,

      appels.length,

      presences,
    );
  }

  // ============================================================
  // GET /reports/stagieres/:id
  // ============================================================

  async studentReport(id: string, requesterId: string, requesterRole: Role) {
    if (requesterRole === Role.stagiaire && id !== requesterId) {
      throw new ForbiddenException('Vous ne pouvez consulter que votre propre rapport');
    }

    if (requesterRole === Role.FORMATEUR) {
      const student = await this.stagiaireRepo.findOne({ where: { idUtilisateur: id } });
      if (!student) throw new NotFoundException('Étudiant introuvable');
      const allowed = await this.affectationRepo.exist({ where: { idClasse: student.idClasse ?? '', idFormateur: requesterId } });
      if (!allowed) throw new ForbiddenException('Vous ne pouvez consulter que les étudiants de vos classes');
    }

    const presences =
      await this.presenceRepo.find({
        where: {
          stagiaire: {
            idUtilisateur: id,
          } as any,
        },

        relations: [
          'appel',
          'appel.creneau',
          'appel.creneau.affectation',
          'appel.creneau.affectation.cours',
          'justification',
        ],
      });

    return this.buildReport(
      `Rapport de l'étudiant ${id}`,

      presences.length,

      presences,
    );
  }

  // ============================================================
  // GET /reports/export
  // ============================================================

  async export(
    format: 'pdf' | 'xlsx',
  ) {
    const stats =
      await this.attendance();

    if (format === 'xlsx') {
      const buffer =
        await this.excel.generateSheet(
          'Statistiques',

          [
            {
              header: 'Statut',
              key: 'statut',
              width: 20,
            },

            {
              header: 'Total',
              key: 'total',
              width: 12,
            },

            {
              header: 'Pourcentage',
              key: 'pourcentage',
              width: 15,
            },
          ],

          stats.parStatut,
        );

      return {
        buffer,

        filename:
          'rapport.xlsx',

        contentType:
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      };
    }

    const buffer =
      await this.pdf.generateDocument(
        'Rapport de présence',

        stats.parStatut.map(
          (s) =>
            `${s.statut} : ${s.total} (${s.pourcentage}%)`,
        ),
      );

    return {
      buffer,

      filename:
        'rapport.pdf',

      contentType:
        'application/pdf',
    };
  }

  // ============================================================
  // GET /reports/recent-activity
  // ============================================================

  async recentActivity(
    limit = 10,
  ) {
    const [
      appels,
      justifications,
      demandes,
      annonces,
    ] = await Promise.all([
      this.appelRepo.find({
        order: {
          dateHeure: 'DESC',
        },

        take: limit,

        relations: [
          'creneau',
          'creneau.affectation',
          'creneau.affectation.cours',
          'creneau.affectation.classe',
          'formateur',
          'formateur.utilisateur',
        ],
      }),

      this.justificationRepo.find({
        order: {
          dateEnvoi: 'DESC',
        },

        take: limit,
      }),

      this.demandeRepo.find({
        order: {
          dateDemande: 'DESC',
        },

        take: limit,
      }),

      this.annonceRepo.find({
        order: {
          datePublication: 'DESC',
        },

        take: limit,
      }),
    ]);

    const items = [
      ...appels.map(
        (appel) => ({
          type: 'appel',

          message:
            `Appel effectué pour ${
              appel.creneau
                ?.affectation
                ?.cours
                ?.nomCours ??
              'un cours'
            }`,

          date:
            appel.dateHeure,
        }),
      ),

      ...justifications.map(
        (justification) => ({
          type: 'justification',

          message:
            'Nouvelle justification déposée',

          date:
            justification.dateEnvoi,
        }),
      ),

      ...demandes.map(
        (demande) => ({
          type: 'document',

          message:
            `Demande de document (${demande.typeDocument})`,

          date:
            demande.dateDemande,
        }),
      ),

      ...annonces.map(
        (annonce) => ({
          type: 'annonce',

          message:
            `Annonce publiée : ${annonce.titre}`,

          date:
            annonce.datePublication,
        }),
      ),
    ];

    return items
      .sort(
        (a, b) =>
          new Date(b.date).getTime() -
          new Date(a.date).getTime(),
      )
      .slice(0, limit);
  }

  // ============================================================
  // BUILD REPORT
  // ============================================================

  private buildReport(
    titre: string,
    nbAppels: number,
    presences: Presence[],
  ) {
    const nbPresent =
      presences.filter(
        (p) =>
          p.statut ===
          StatutPresence.PRESENT,
      ).length;

    const nbAbsent =
      presences.filter(
        (p) =>
          p.statut === StatutPresence.ABSENT &&
          p.justification?.statutJustification !== 'acceptee',
      ).length;

    const nbRetard =
      presences.filter(
        (p) =>
          p.statut ===
          StatutPresence.RETARD,
      ).length;

    const total =
      presences.length || 1;

    return {
      titre,

      nbAppels,

      nbPresences:
        presences.length,

      tauxPresence:
        Math.round(
          (nbPresent / total) *
            1000,
        ) / 10,

      detail: {
        present: nbPresent,
        absent: nbAbsent,
        retard: nbRetard,
      },
    };
  }
}