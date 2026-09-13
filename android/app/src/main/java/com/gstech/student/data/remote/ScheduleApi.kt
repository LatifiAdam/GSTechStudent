package com.gstech.student.data.remote

import com.gstech.student.data.remote.dto.CreneauDto
import com.gstech.student.data.remote.dto.CreateCreneauRequest
import com.gstech.student.data.remote.dto.UpdateCreneauRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {

    @GET("schedule")
    suspend fun getSchedule(
        @Query("coursId") coursId: String? = null,
        @Query("etudiantId") etudiantId: String? = null,
        @Query("formateurId") formateurId: String? = null,
        @Query("classeId") classeId: String? = null,
    ): List<CreneauDto>

    @POST("schedule")
    suspend fun create(
        @Body body: CreateCreneauRequest,
    ): CreneauDto

    @PATCH("schedule/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdateCreneauRequest,
    ): CreneauDto

    @DELETE("schedule/{id}")
    suspend fun delete(
        @Path("id") id: String,
    ): Response<Unit>
}