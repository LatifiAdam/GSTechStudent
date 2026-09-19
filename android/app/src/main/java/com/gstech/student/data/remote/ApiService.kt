package com.gstech.student.data.remote

import okhttp3.ResponseBody
import okhttp3.MultipartBody

import com.gstech.student.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*


interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<Unit>
}

interface UsersApi {
    @Multipart
    @POST("users/me/profile-image")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): UserDto

    @Multipart
    @POST("users/{id}/profile-image")
    suspend fun uploadUserProfileImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
    ): UserDto

    @GET("users/me/profile-image")
    suspend fun getMyProfileImage(): Response<ResponseBody>

    @GET("users/{id}/profile-image")
    suspend fun getUserProfileImage(@Path("id") id: String): Response<ResponseBody>

    @GET("users")
    suspend fun getAll(@Query("role") role: String? = null): List<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto

    @POST("users")
    suspend fun create(@Body body: CreateUserRequest): UserDto

    @PATCH("users/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateUserRequest): UserDto

    @PATCH("users/{id}/password")
    suspend fun changePassword(@Path("id") id: String, @Body body: ChangePasswordRequest): Response<Unit>

    @DELETE("users/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}

interface CoursesApi {
    @GET("courses")
    suspend fun getAll(@Query("formateurId") formateurId: String? = null): List<CourseDto>

    @POST("courses")
    suspend fun create(@Body body: CreateCourseRequest): CourseDto

    @DELETE("courses/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}


interface ClassesApi {
    @GET("classes") suspend fun getAll(): List<ClassDto>
    @GET("classes/{id}") suspend fun get(@Path("id") id:String): ClassDto
    @POST("classes") suspend fun create(@Body body:CreateClassRequest): ClassDto
    @PATCH("classes/{id}") suspend fun update(@Path("id") id:String,@Body body:CreateClassRequest): ClassDto
    @DELETE("classes/{id}") suspend fun delete(@Path("id") id:String): Response<Unit>
    @GET("classes/{id}/stagieres") suspend fun students(@Path("id") id:String): List<StudentUserDto>
    @POST("classes/{id}/stagieres/{studentId}") suspend fun assignStudent(@Path("id") id:String,@Path("studentId") studentId:String): StudentUserDto
    @DELETE("classes/{id}/stagieres/{studentId}") suspend fun removeStudent(@Path("id") id:String,@Path("studentId") studentId:String): Response<Unit>
}

interface AffectationsApi {
    @GET("affectations") suspend fun getAll(): List<AffectationDto>
    @POST("affectations") suspend fun create(@Body body:CreateAffectationRequest): AffectationDto
    @DELETE("affectations/{id}") suspend fun delete(@Path("id") id:String): Response<Unit>
}
interface AttendanceApi {
    @GET("attendance/stagieres/{id}/history")
    suspend fun getHistory(@Path("id") studentId: String): List<PresenceDto>

    // Teacher "Take Attendance" flow — Phase 2 section 4.6.
    @POST("attendance/calls")
    suspend fun openCall(@Body body: OpenCallRequest): CallDto

    @GET("attendance/calls/{id}")
    suspend fun getCall(@Path("id") id: String): CallDto

    @PATCH("attendance/calls/{id}/records")
    suspend fun updateRecords(@Path("id") id: String, @Body body: UpdateRecordsRequest): CallDto

    @POST("attendance/calls/{id}/validate")
    suspend fun validateCall(@Path("id") id: String): CallDto
}

interface NotificationsApi {
    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @PATCH("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): NotificationDto
}
interface JustificationsApi {
    @POST("justifications")
    suspend fun submit(@Body body: CreateJustificationRequest): Response<Unit>

    @GET("justifications")
    suspend fun getAll(@Query("statut") statut: String? = null, @Query("coursId") coursId: String? = null): List<JustificationDto>

    @PATCH("justifications/{id}/accept")
    suspend fun accept(@Path("id") id: String): JustificationDto

    @PATCH("justifications/{id}/refuse")
    suspend fun refuse(@Path("id") id: String, @Body body: RefuseJustificationRequest): JustificationDto
}

interface DocumentRequestsApi {
    @GET("document-requests")
    suspend fun list(@Query("statut") statut: String? = null): List<DocumentRequestDto>

    @POST("document-requests")
    suspend fun create(@Body body: CreateDocumentRequest): DocumentRequestDto

    @PATCH("document-requests/{id}/generate")
    suspend fun generate(@Path("id") id: String): DocumentRequestDto

    @PATCH("document-requests/{id}/refuse")
    suspend fun refuse(@Path("id") id: String, @Body body: RefuseJustificationRequest): DocumentRequestDto

    @Streaming @GET("document-requests/{id}/file") suspend fun file(@Path("id") id: String): ResponseBody
}

interface AnnouncementsApi {
    // GET /announcements — Phase 2 section 4.9. Not filterable server-side by
    // course, so callers filter the returned list by `cours.idCours` client-side.
    @GET("announcements")
    suspend fun getAll(): List<AnnouncementDto>

    @POST("announcements")
    suspend fun create(@Body body: CreateAnnouncementDto): AnnouncementDto

    @DELETE("announcements/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>
}


interface EstablishmentsApi {
 @GET("etablissements") suspend fun getAll(): List<EstablishmentDto>
 @POST("etablissements") suspend fun create(@Body body: CreateEstablishmentRequest): EstablishmentDto
 @POST("etablissements/{id}/directeur") suspend fun assignDirector(@Path("id") id:String,@Body body:AssignEstablishmentUserRequest): EstablishmentDto
 @POST("etablissements/{id}/gestionnaires") suspend fun addGestionnaire(@Path("id") id:String,@Body body:AssignEstablishmentUserRequest): Any
 @DELETE("etablissements/{id}/gestionnaires/{gestionnaireId}") suspend fun removeGestionnaire(@Path("id") id:String,@Path("gestionnaireId") gid:String): Response<Unit>
 @GET("etablissements/mine") suspend fun mine(): EstablishmentDto?
}
