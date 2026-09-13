package com.gstech.student.data.remote

import com.gstech.student.data.remote.dto.DocumentDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface DocumentsApi {
    @GET("documents/pending") suspend fun pending(): List<DocumentDto>
    @GET("documents/mine") suspend fun mine(): List<DocumentDto>
    @GET("documents/approved") suspend fun approved(): List<DocumentDto>
    @Streaming @GET("documents/{id}/file") suspend fun file(@Path("id") id: String): ResponseBody
    @Multipart @POST("documents/upload") suspend fun upload(@Part file: MultipartBody.Part, @Part("nomDocument") name: RequestBody): DocumentDto
    @PATCH("documents/{id}/approve") suspend fun approve(@Path("id") id: String): DocumentDto
    @PATCH("documents/{id}/refuse") suspend fun refuse(@Path("id") id: String, @Body body: Map<String, String>): DocumentDto
}
