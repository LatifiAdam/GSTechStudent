package com.gstech.student.model

data class AdminUser(
    val id: String,
    val name: String,
    val role: Role,
    val department: String?,
    val email: String,
    val studentNumber: String?,
    val promotion: String?,
    val cin: String?,
    val telephone: String?,
    val adresse: String?,
)

data class AdminCourse(
    val id: String,
    val name: String,
    val code: String,
    val teacherName: String,
    val room: String,
)

data class AdminDashboard(
    val totalUsers: Int,
    val totalAdmins: Int,
    val totalDirectors: Int,
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val totalGestionnaires: Int = 0,
    val totalEfp: Int = 0,
)

data class AdminActivity(
    val type: String,
    val message: String,
    val date: String,
)