package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// POST /users — Phase 2 section 4.3. Role-specific fields are validated
// conditionally server-side (create-user.dto.ts).
@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    val idUtilisateur: String? = null,
    val nom: String,
    val prenom: String,
    val email: String,
    val password: String,
    val role: String, // superadmin | df | srio | scq | directeur | gestionnaire | formateur | stagiaire
    val module: String? = null,
    @Json(name = "numerostagiaire")
    val numeroEtudiant: String? = null,
    val promotion: String? = null,
    val niveauAcces: String? = null,
    val cin: String? = null,
    val telephone: String? = null,
    val adresse: String? = null,
    val region: String? = null,
    val idEtablissement: String? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateUserRequest(
    val nom: String? = null,
    val prenom: String? = null,
    val email: String? = null,
    val cin: String? = null,
    val telephone: String? = null,
    val adresse: String? = null,
    val module: String? = null,
    @Json(name = "numerostagiaire")
    val numeroEtudiant: String? = null,
    val promotion: String? = null,
    val niveauAcces: String? = null,
    val region: String? = null,
    val idEtablissement: String? = null,
)

// POST /courses — Phase 2 section 4.4.
@JsonClass(generateAdapter = true)
data class CreateCourseRequest(
    val nomCours: String,
)

// GET /reports/attendance — Phase 2 section 4.11. Backs the admin dashboard's
// attendance-rate figure (there's no dedicated admin stats endpoint).
@JsonClass(generateAdapter = true)
data class AttendanceStatRow(
    val statut: String,
    val total: Int,
    val pourcentage: Double,
)

@JsonClass(generateAdapter = true)
data class AttendanceReportDto(
    val periode: String,
    val total: Int,
    val parStatut: List<AttendanceStatRow> = emptyList(),
)
