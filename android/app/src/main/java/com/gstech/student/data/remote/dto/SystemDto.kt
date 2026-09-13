package com.gstech.student.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServerStatusDto(
    val status: String,
    val api: String,
    val uptimeSeconds: Long,
    val timestamp: String,
)

@JsonClass(generateAdapter = true)
data class DatabaseStatusDto(
    val status: String,
    val database: String,
    val host: String,
    val port: Int,
    val responseMs: Long? = null,
    val error: String? = null,
)

@JsonClass(generateAdapter = true)
data class AccountCountsDto(
    val total: Int,
    val df: Int,
    val srio: Int,
    val scq: Int,
    val directeur: Int,
    val gestionnaire: Int,
    val formateur: Int,
    val stagiaire: Int,
    val superadmin: Int,
)

@JsonClass(generateAdapter = true)
data class TechnicalLogDto(
    val id: String,
    val report: String,
    val date: String,
)

@JsonClass(generateAdapter = true)
data class TechnicalLogCreateDto(
    val report: String,
)
