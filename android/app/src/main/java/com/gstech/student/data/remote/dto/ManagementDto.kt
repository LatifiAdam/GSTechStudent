package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============================================================
// CLASS
// ============================================================

@JsonClass(generateAdapter = true)
data class ClassDto(
    @Json(name = "idClasse")
    val idClasse: String,

    @Json(name = "nomClasse")
    val nomClasse: String,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "etudiants")
    val etudiants: List<StudentUserDto>? = null,

    @Json(name = "affectations")
    val affectations: List<AffectationDto>? = null,
)

@JsonClass(generateAdapter = true)
data class StudentUserDto(
    @Json(name = "idUtilisateur")
    val idUtilisateur: String,

    @Json(name = "numeroEtudiant")
    val numeroEtudiant: String? = null,

    @Json(name = "promotion")
    val promotion: String? = null,

    @Json(name = "utilisateur")
    val utilisateur: UserDto? = null,
)

@JsonClass(generateAdapter = true)
data class CreateClassRequest(
    @Json(name = "nomClasse")
    val nomClasse: String,

    @Json(name = "description")
    val description: String? = null,
)


// ============================================================
// AFFECTATIONS
// Class <-> Course <-> Teacher
// ============================================================

@JsonClass(generateAdapter = true)
data class AffectationDto(
    @Json(name = "idAffectation")
    val idAffectation: String,

    @Json(name = "idClasse")
    val idClasse: String = "",

    @Json(name = "idCours")
    val idCours: String = "",

    @Json(name = "idFormateur")
    val idFormateur: String = "",

    @Json(name = "classe")
    val classe: ClassDto? = null,

    @Json(name = "nomClasse")
    val nomClasse: String? = null,

    @Json(name = "cours")
    val cours: CourseDto? = null,

    @Json(name = "formateur")
    val formateur: TeacherDto? = null,

    @Json(name = "creneaux")
    val creneaux: List<CreneauDto>? = null,
)

@JsonClass(generateAdapter = true)
data class CreateAffectationRequest(
    @Json(name = "idclasse")
    val idClasse: String,

    @Json(name = "idcours")
    val idCours: String,

    @Json(name = "idformateur")
    val idFormateur: String,
)


// ============================================================
// SCHEDULE / CRENEAUX
// ============================================================

@JsonClass(generateAdapter = true)
data class CreneauDto(
    @Json(name = "idCreneau")
    val idCreneau: String,

    @Json(name = "jourSemaine")
    val jourSemaine: String,

    @Json(name = "heureDebut")
    val heureDebut: String,

    @Json(name = "heureFin")
    val heureFin: String,

    @Json(name = "salle")
    val salle: String? = null,

    @Json(name = "dateDebut")
    val dateDebut: String? = null,

    @Json(name = "dateFin")
    val dateFin: String? = null,

    @Json(name = "idAffectation")
    val idAffectation: String? = null,

    @Json(name = "cours")
    val cours: CourseDto? = null,

    @Json(name = "affectation")
    val affectation: AffectationDto? = null,
)


// ============================================================
// CREATE SCHEDULE SLOT
// ============================================================

@JsonClass(generateAdapter = true)
data class CreateCreneauRequest(
    @Json(name = "jourSemaine")
    val jourSemaine: String,

    @Json(name = "heureDebut")
    val heureDebut: String,

    @Json(name = "heureFin")
    val heureFin: String,

    @Json(name = "salle")
    val salle: String,

    @Json(name = "idAffectation")
    val idAffectation: String,

    @Json(name = "dateDebut")
    val dateDebut: String? = null,

    @Json(name = "dateFin")
    val dateFin: String? = null,
)


// ============================================================
// UPDATE SCHEDULE SLOT
// PATCH /schedule/:id
//
// All fields are optional because this is a PATCH request.
// ============================================================

@JsonClass(generateAdapter = true)
data class UpdateCreneauRequest(
    @Json(name = "jourSemaine")
    val jourSemaine: String? = null,

    @Json(name = "heureDebut")
    val heureDebut: String? = null,

    @Json(name = "heureFin")
    val heureFin: String? = null,

    @Json(name = "salle")
    val salle: String? = null,

    @Json(name = "idAffectation")
    val idAffectation: String? = null,

    @Json(name = "dateDebut")
    val dateDebut: String? = null,

    @Json(name = "dateFin")
    val dateFin: String? = null,
)