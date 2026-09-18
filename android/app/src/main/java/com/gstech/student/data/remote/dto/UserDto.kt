package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// GET /users/:id — Phase 2 section 4.3.
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "idUtilisateur")
    val idUtilisateur: String,

    @Json(name = "nom")
    val nom: String,

    @Json(name = "prenom")
    val prenom: String,

    @Json(name = "email")
    val email: String,

    @Json(name = "role")
    val role: String? = null,

    @Json(name = "idClasse")
    val idClasse: String? = null,

    @Json(name = "dateCreation")
    val dateCreation: String? = null,

    @Json(name = "numerostagiaire")
    val numeroEtudiant: String? = null,

    @Json(name = "promotion")
    val promotion: String? = null,

    @Json(name = "module")
    val module: String? = null,

    @Json(name = "niveauAcces")
    val niveauAcces: String? = null,

    // Personal information
    @Json(name = "cin")
    val cin: String? = null,

    @Json(name = "telephone")
    val telephone: String? = null,

    @Json(name = "adresse")
    val adresse: String? = null,

    @Json(name = "profileImageKey")
    val profileImageKey: String? = null,
)