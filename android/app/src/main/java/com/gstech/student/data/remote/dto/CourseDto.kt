package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Formateur entity.
@JsonClass(generateAdapter = true)
data class TeacherDto(
    @Json(name = "idUtilisateur")
    val idUtilisateur: String? = null,

    @Json(name = "module")
    val module: String? = null,

    @Json(name = "utilisateur")
    val utilisateur: UserDto? = null,
)

// Cours entity.
@JsonClass(generateAdapter = true)
data class CourseDto(
    @Json(name = "idCours")
    val idCours: String,

    @Json(name = "nomCours")
    val nomCours: String,

    @Json(name = "salle")
    val salle: String? = null,

    @Json(name = "horaire")
    val horaire: String? = null,

    @Json(name = "formateur")
    val formateur: TeacherDto? = null,

    @Json(name = "formateurNom")
    val formateurNom: String? = null,

    @Json(name = "affectations")
    val affectations: List<AffectationDto>? = null,
) {
    val teacherDisplayName: String
        get() {
            if (!formateurNom.isNullOrBlank()) {
                return formateurNom
            }

            val utilisateur = formateur?.utilisateur

            val prenom = utilisateur?.prenom?.trim().orEmpty()
            val nom = utilisateur?.nom?.trim().orEmpty()

            val fullName = "$prenom $nom".trim()

            return fullName.ifBlank { "N/A" }
        }
}