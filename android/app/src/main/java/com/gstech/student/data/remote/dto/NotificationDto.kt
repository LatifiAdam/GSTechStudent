package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// NOTIFICATION entity — GET /notifications — Phase 2 section 4.10.
@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "idNotification") val idNotification: String,
    @Json(name = "type") val type: String,
    @Json(name = "message") val message: String,
    @Json(name = "lue") val lue: Boolean,
    @Json(name = "dateEnvoi") val dateEnvoi: String,
)

// JUSTIFICATION entity — POST /justifications — Phase 2 section 4.7.
@JsonClass(generateAdapter = true)
data class CreateJustificationRequest(
    @Json(name = "idPresence") val idPresence: String,
    @Json(name = "motif") val motif: String,
    @Json(name = "pieceJointe") val pieceJointe: String? = null,
)

// DEMANDE_DOCUMENT entity — Phase 2 section 4.8 (RG9/RG10).
// typeDocument: 'certificat_scolarite' | 'releve_notes' | 'attestation_reussite'
@JsonClass(generateAdapter = true)
data class DocumentRequestDto(
    @Json(name = "idDemande") val idDemande: String,
    @Json(name = "typeDocument") val typeDocument: String?,
    @Json(name = "statut") val statut: String,
    @Json(name = "dateDemande") val dateDemande: String,
    @Json(name = "dateTraitement") val dateTraitement: String? = null,
    @Json(name = "idDocument") val idDocument: String? = null,
    @Json(name = "idEtudiant") val idEtudiant: String? = null,
    @Json(name = "idGestionnaire") val idGestionnaire: String? = null,
    @Json(name = "document") val document: DocumentDto? = null,
    @Json(name = "etudiant") val etudiant: StudentUserBriefDto? = null,
)

@JsonClass(generateAdapter = true)
data class CreateDocumentRequest(
    @Json(name = "typeDocument") val typeDocument: String? = null,
    @Json(name = "idDocument") val idDocument: String? = null,
)

@JsonClass(generateAdapter = true)
data class StudentUserBriefDto(
    @Json(name = "utilisateur") val utilisateur: UserBriefDto? = null,
    @Json(name = "numeroEtudiant") val numeroEtudiant: String? = null,
)
@JsonClass(generateAdapter = true)
data class UserBriefDto(
    @Json(name = "nom") val nom: String? = null,
    @Json(name = "prenom") val prenom: String? = null,
)
