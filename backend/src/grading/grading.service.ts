import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Affectation } from '../entities/affectation.entity';
import { Note } from '../entities/note.entity';
import { stagiaire } from '../entities/stagiaire.entity';
import { SaveGradesDto } from './dto/save-grades.dto';

@Injectable()
export class GradingService {
  constructor(
    @InjectRepository(Affectation) private readonly affectationRepo: Repository<Affectation>,
    @InjectRepository(Note) private readonly noteRepo: Repository<Note>,
    @InjectRepository(stagiaire) private readonly stagiaireRepo: Repository<stagiaire>,
  ) {}

  async formateurClasses(formateurId: string) {
    const rows = await this.affectationRepo.find({
      where: { idFormateur: formateurId },
      relations: ['classe', 'cours'],
    });
    return rows.map(a => ({
      idAffectation: a.idAffectation,
      idClasse: a.idClasse,
      nomClasse: a.classe?.nomClasse ?? null,
      idCours: a.idCours,
      nomCours: a.cours?.nomCours ?? null,
    }));
  }

  private async authorizedAffectation(idAffectation: string, formateurId: string) {
    const a = await this.affectationRepo.findOne({
      where: { idAffectation, idFormateur: formateurId },
      relations: ['classe', 'cours'],
    });
    if (!a) throw new ForbiddenException('Cette classe/cours ne vous est pas affecté');
    return a;
  }

  async getClassGrades(idAffectation: string, formateurId: string) {
    const a = await this.authorizedAffectation(idAffectation, formateurId);
    const stagieres = await this.stagiaireRepo.find({
      where: { idClasse: a.idClasse },
      relations: ['utilisateur'],
      order: { utilisateur: { nom: 'ASC' } },
    });
    const notes = await this.noteRepo.find({ where: { idAffectation }, relations: ['stagiaire'] });
    const byStudent = new Map(notes.map(n => [n.idstagiaire, n]));
    return {
      affectation: { idAffectation: a.idAffectation, idClasse: a.idClasse, nomClasse: a.classe?.nomClasse, idCours: a.idCours, nomCours: a.cours?.nomCours },
      stagieres: stagieres.map(s => {
        const n = byStudent.get(s.idUtilisateur);
        return {
          idstagiaire: s.idUtilisateur,
          numerostagiaire: s.numerostagiaire,
          nom: s.utilisateur.nom,
          prenom: s.utilisateur.prenom,
          note1: n?.note1 ?? null,
          note2: n?.note2 ?? null,
          note3: n?.note3 ?? null,
        };
      }),
    };
  }

  async saveGrades(idAffectation: string, dto: SaveGradesDto, formateurId: string) {
    const a = await this.authorizedAffectation(idAffectation, formateurId);
    const student = await this.stagiaireRepo.findOne({ where: { idUtilisateur: dto.idstagiaire } });
    if (!student || student.idClasse !== a.idClasse) throw new ForbiddenException('Cet étudiant n’appartient pas à cette classe');
    let note = await this.noteRepo.findOne({ where: { idstagiaire: dto.idstagiaire, idAffectation } });
    if (!note) note = this.noteRepo.create({ idstagiaire: dto.idstagiaire, idAffectation, note1: null, note2: null, note3: null });
    if (dto.note1 !== undefined) note.note1 = dto.note1 ?? null;
    if (dto.note2 !== undefined) note.note2 = dto.note2 ?? null;
    if (dto.note3 !== undefined) note.note3 = dto.note3 ?? null;
    return this.noteRepo.save(note);
  }

  async studentGrades(studentId: string) {
    const notes = await this.noteRepo.find({
      where: { idstagiaire: studentId },
      relations: ['affectation', 'affectation.cours', 'affectation.classe'],
    });
    return notes.map(n => ({
      idNote: n.idNote,
      idAffectation: n.idAffectation,
      idCours: n.affectation?.idCours,
      nomCours: n.affectation?.cours?.nomCours,
      idClasse: n.affectation?.idClasse,
      nomClasse: n.affectation?.classe?.nomClasse,
      note1: n.note1,
      note2: n.note2,
      note3: n.note3,
    }));
  }
}
