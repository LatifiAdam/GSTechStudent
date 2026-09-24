package com.gstech.student.data.remote

import com.gstech.student.BuildConfig
import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.dto.RefreshRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class NetworkModule(private val tokenManager: TokenManager) {

    private val plainRetrofit: Retrofit by lazy {
        buildRetrofit(OkHttpClient.Builder().build())
    }

    val authApi: AuthApi by lazy { plainRetrofit.create(AuthApi::class.java) }

    private val authenticatedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor())
            .authenticator(tokenAuthenticator())
            .build()
    }

    private val authenticatedRetrofit: Retrofit by lazy { buildRetrofit(authenticatedClient) }

    val usersApi: UsersApi by lazy { authenticatedRetrofit.create(UsersApi::class.java) }
    val classesApi: ClassesApi by lazy { authenticatedRetrofit.create(ClassesApi::class.java) }
    val affectationsApi: AffectationsApi by lazy { authenticatedRetrofit.create(AffectationsApi::class.java) }
    val coursesApi: CoursesApi by lazy { authenticatedRetrofit.create(CoursesApi::class.java) }
    val scheduleApi: ScheduleApi by lazy { authenticatedRetrofit.create(ScheduleApi::class.java) }
    val gradingApi: GradingApi by lazy { authenticatedRetrofit.create(GradingApi::class.java) }
    val attendanceApi: AttendanceApi by lazy { authenticatedRetrofit.create(AttendanceApi::class.java) }
    val notificationsApi: NotificationsApi by lazy { authenticatedRetrofit.create(NotificationsApi::class.java) }
    val reportsApi: ReportsApi by lazy { authenticatedRetrofit.create(ReportsApi::class.java) }
    val justificationsApi: JustificationsApi by lazy { authenticatedRetrofit.create(JustificationsApi::class.java) }
    val documentRequestsApi: DocumentRequestsApi by lazy { authenticatedRetrofit.create(DocumentRequestsApi::class.java) }
    val announcementsApi: AnnouncementsApi by lazy { authenticatedRetrofit.create(AnnouncementsApi::class.java) }
    val documentsApi: DocumentsApi by lazy { authenticatedRetrofit.create(DocumentsApi::class.java) }
    val establishmentsApi: EstablishmentsApi by lazy { authenticatedRetrofit.create(EstablishmentsApi::class.java) }
    val systemApi: SystemApi by lazy { authenticatedRetrofit.create(SystemApi::class.java) }

    private fun buildRetrofit(client: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    private fun authHeaderInterceptor() = Interceptor { chain ->
        val token = runBlocking { tokenManager.accessTokenNow() }
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }

    private fun tokenAuthenticator() = Authenticator { _: Route?, response ->
        // Avoid infinite retry loops.
        if (response.request.header("X-Retry") != null) return@Authenticator null

        val refreshToken = runBlocking { tokenManager.refreshTokenNow() } ?: return@Authenticator null
        val newAccessToken = runBlocking {
            try {
                authApi.refresh(RefreshRequest(refreshToken)).accessToken.also {
                    tokenManager.saveAccessToken(it)
                }
            } catch (e: Exception) {
                null
            }
        } ?: return@Authenticator null

        response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .header("X-Retry", "1")
            .build()
    }

}
