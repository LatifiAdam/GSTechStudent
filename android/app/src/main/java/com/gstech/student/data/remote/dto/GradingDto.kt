package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StudentGradeDto(
    @Json(name = "idNote") val idNote: String,
    @Json(name = "idAffectation") val idAffectation: String?,
    @Json(name = "idCours") val idCours: String?,
    @Json(name = "nomCours") val nomCours: String?,
    @Json(name = "idClasse") val idClasse: String?,
    @Json(name = "nomClasse") val nomClasse: String?,
    @Json(name = "note1") val note1: Double?,
    @Json(name = "note2") val note2: Double?,
    @Json(name = "note3") val note3: Double?,
)
