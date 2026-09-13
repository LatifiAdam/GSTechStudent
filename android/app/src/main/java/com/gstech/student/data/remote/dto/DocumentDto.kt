package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocumentDto(
    @Json(name = "idDocument") val idDocument: String,
    @Json(name = "nomDocument") val nomDocument: String,
    @Json(name = "typeDocument") val typeDocument: String,
    @Json(name = "statut") val statut: String,
    @Json(name = "dateCreation") val dateCreation: String? = null,
    @Json(name = "motifRefus") val motifRefus: String? = null,
)
