package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// APPEL entity, nested inside a PRESENCE row.
@JsonClass(generateAdapter = true)
data class AppelDto(
    @Json(name = "idAppel") val idAppel: String,
    @Json(name = "dateHeure") val dateHeure: String,
    @Json(name = "valide") val valide: Boolean? = null,
    @Json(name = "cours") val cours: CourseDto? = null,
)

// PRESENCE entity — GET /attendance/students/:id/history — Phase 2 section 4.6.
// statut: 'present' | 'absent' | 'retard' (RG1/RG2, presence.entity.ts).
@JsonClass(generateAdapter = true)
data class PresenceDto(
    @Json(name = "idPresence") val idPresence: String,
    @Json(name = "statut") val statut: String,
    @Json(name = "appel") val appel: AppelDto? = null,
)

// GET /reports/students/:id — Phase 2 section 4.11.
@JsonClass(generateAdapter = true)
data class AttendanceDetailDto(
    @Json(name = "present") val present: Int = 0,
    @Json(name = "absent") val absent: Int = 0,
    @Json(name = "retard") val retard: Int = 0,
)

@JsonClass(generateAdapter = true)
data class StudentReportDto(
    @Json(name = "titre") val titre: String? = null,
    @Json(name = "nbAppels") val nbAppels: Int = 0,
    @Json(name = "nbPresences") val nbPresences: Int = 0,
    @Json(name = "tauxPresence") val tauxPresence: Double = 0.0,
    @Json(name = "detail") val detail: AttendanceDetailDto = AttendanceDetailDto(),
)
