package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EstablishmentDto(
    @Json(name = "idEtablissement") val idEtablissement: String,
    @Json(name = "nomEtablissement") val nomEtablissement: String,
    @Json(name = "region") val region: String? = null,
    @Json(name = "idDirecteur") val idDirecteur: String? = null,
    @Json(name = "directeur") val directeur: UserDto? = null,
    @Json(name = "gestionnaires") val gestionnaires: List<UserDto> = emptyList(),
)
@JsonClass(generateAdapter = true)
data class AssignEstablishmentUserRequest(@Json(name = "idUtilisateur") val idUtilisateur: String)
@JsonClass(generateAdapter = true)
data class CreateEstablishmentRequest(@Json(name = "nomEtablissement") val nomEtablissement: String, @Json(name = "region") val region: String)
