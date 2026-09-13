package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ANNONCE entity — GET /announcements — Phase 2 section 4.9 (RG11/RG12).
// idCours is null for a "generale" announcement addressed to every user.
@JsonClass(generateAdapter = true)
data class AnnouncementDto(
    @Json(name = "idAnnonce") val idAnnonce: String,
    @Json(name = "titre") val titre: String,
    @Json(name = "contenu") val contenu: String,
    @Json(name = "typeAnnonce") val typeAnnonce: String,
    @Json(name = "datePublication") val datePublication: String,
    @Json(name = "dateEvenement") val dateEvenement: String? = null,
    @Json(name = "cours") val cours: CourseDto? = null,
)

// POST /announcements — Phase 2 section 4.9 (RG11/RG12). idCours null +
// typeAnnonce "generale" is only valid when the caller is an administrator
// (enforced server-side too).
@JsonClass(generateAdapter = true)
data class CreateAnnouncementDto(
    @Json(name = "titre") val titre: String,
    @Json(name = "contenu") val contenu: String,
    @Json(name = "typeAnnonce") val typeAnnonce: String, // "examen" | "controle" | "generale"
    @Json(name = "dateEvenement") val dateEvenement: String? = null,
    @Json(name = "idCours") val idCours: String? = null,
)
