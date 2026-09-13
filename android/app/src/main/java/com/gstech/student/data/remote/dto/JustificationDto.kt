package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Nested presence -> etudiant -> utilisateur chain as returned by
// JustificationsService#findAll (Phase 2 section 4.7).
@JsonClass(generateAdapter = true)
data class JustificationStudentDto(
    @Json(name = "idUtilisateur") val idUtilisateur: String? = null,
    @Json(name = "nom") val nom: String? = null,
    @Json(name = "prenom") val prenom: String? = null,
)

@JsonClass(generateAdapter = true)
data class JustificationPresenceDto(
    @Json(name = "idPresence") val idPresence: String,
    @Json(name = "etudiant") val etudiant: JustificationStudentDto? = null,
    @Json(name = "appel") val appel: AppelDto? = null,
)

@JsonClass(generateAdapter = true)
data class JustificationDto(
    @Json(name = "idJustification") val idJustification: String,
    @Json(name = "motif") val motif: String,
    @Json(name = "pieceJointe") val pieceJointe: String? = null,
    @Json(name = "statutJustification") val statutJustification: String,
    @Json(name = "dateEnvoi") val dateEnvoi: String,
    @Json(name = "presence") val presence: JustificationPresenceDto? = null,
)

@JsonClass(generateAdapter = true)
data class RefuseJustificationRequest(val motif: String)

// POST /attendance/calls — Phase 2 section 4.6.
@JsonClass(generateAdapter = true)
data class OpenCallRequest(
    @Json(name = "idCreneau") val idCreneau: String,
    @Json(name = "dateHeure") val dateHeure: String,
)

// PATCH /attendance/calls/:id/records — only exceptions (absent/retard) need
// to be sent; everyone else stays "present" per the RG1 default.
@JsonClass(generateAdapter = true)
data class AttendanceRecordEntry(
    @Json(name = "idEtudiant") val idEtudiant: String,
    @Json(name = "statut") val statut: String, // present | absent | retard
)

@JsonClass(generateAdapter = true)
data class UpdateRecordsRequest(val records: List<AttendanceRecordEntry>)

@JsonClass(generateAdapter = true)
data class CallDto(
    @Json(name = "idAppel") val idAppel: String,
    @Json(name = "dateHeure") val dateHeure: String,
    @Json(name = "valide") val valide: Boolean = false,
    @Json(name = "cours") val cours: CourseDto? = null,
    @Json(name = "creneau") val creneau: CreneauDto? = null,
    @Json(name = "presences") val presences: List<PresenceWithStudentDto>? = null,
    @Json(name = "nbEtudiants") val nbEtudiants: Int? = null,
)

@JsonClass(generateAdapter = true)
data class PresenceWithStudentDto(
    @Json(name = "idPresence") val idPresence: String,
    @Json(name = "statut") val statut: String,
    @Json(name = "etudiant") val etudiant: JustificationStudentDto? = null,
)
