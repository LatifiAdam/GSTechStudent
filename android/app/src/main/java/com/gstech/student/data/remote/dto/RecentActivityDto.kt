package com.gstech.student.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecentActivityDto(

    @Json(name = "type")
    val type: String,

    @Json(name = "message")
    val message: String,

    @Json(name = "date")
    val date: String,
)