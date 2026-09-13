package com.gstech.student.data.remote

import com.gstech.student.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SystemApi {
    @GET("health")
    suspend fun health(): ServerStatusDto

    @GET("system/database")
    suspend fun database(): DatabaseStatusDto

    @GET("system/account-counts")
    suspend fun accountCounts(): AccountCountsDto

    @GET("system/logs")
    suspend fun logs(): List<TechnicalLogDto>

    @POST("system/logs")
    suspend fun addLog(@Body body: TechnicalLogCreateDto): TechnicalLogDto
}
