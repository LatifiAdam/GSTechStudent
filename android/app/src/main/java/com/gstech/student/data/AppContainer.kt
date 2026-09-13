package com.gstech.student.data

import android.content.Context
import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.NetworkModule
import com.gstech.student.data.repository.AttendanceRepository
import com.gstech.student.data.repository.AdminManagementRepository
import com.gstech.student.data.repository.AdminRepository
import com.gstech.student.data.repository.AuthRepository
import com.gstech.student.data.repository.CourseRepository
import com.gstech.student.data.repository.DocumentsRepository
import com.gstech.student.data.repository.NotificationsRepository
import com.gstech.student.data.repository.LocalAcademicRepository
import com.gstech.student.data.repository.ProfileRepository
import com.gstech.student.data.repository.ScheduleRepository
import com.gstech.student.data.repository.TeacherRepository
import com.gstech.student.data.repository.EstablishmentsRepository
import com.gstech.student.util.SessionManager

class AppContainer(context: Context) {

    val context: Context = context.applicationContext

    val tokenManager = TokenManager(context.applicationContext)

    val localAcademicRepository = LocalAcademicRepository(context.applicationContext)

    init {
        SessionManager.initialize(tokenManager)
    }

    private val network = NetworkModule(tokenManager)

    val authRepository = AuthRepository(
        network.authApi,
        tokenManager
    )

    val profileRepository = ProfileRepository(
        network.usersApi,
        tokenManager
    )

    val scheduleRepository = ScheduleRepository(
        network.scheduleApi,
        tokenManager
    )

    val attendanceRepository = AttendanceRepository(
        network.attendanceApi,
        network.reportsApi,
        network.justificationsApi,
        tokenManager
    )

    val notificationsRepository = NotificationsRepository(
        network.notificationsApi
    )

    val documentsRepository = DocumentsRepository(
        network.documentRequestsApi,
        network.documentsApi
    )

    val courseRepository = CourseRepository(
        network.announcementsApi
    )

    val teacherRepository = TeacherRepository(
        network.coursesApi,
        network.attendanceApi,
        network.justificationsApi,
        network.announcementsApi,
        tokenManager
    )

    val adminManagementRepository = AdminManagementRepository(
        network.classesApi, network.affectationsApi, network.scheduleApi, network.usersApi, network.coursesApi
    )

    val documentsApi = network.documentsApi
    val documentRequestsApi = network.documentRequestsApi
    val systemApi = network.systemApi

    val establishmentsRepository = EstablishmentsRepository(network.establishmentsApi)

    val adminRepository = AdminRepository(
        network.usersApi,
        network.coursesApi,
        network.documentRequestsApi,
        network.announcementsApi,
        network.reportsApi
    )
}