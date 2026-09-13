package com.gstech.student.model

data class Student(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val studentNumber: String?,
    val promotion: String?,
    val cin: String?,
    val telephone: String?,
    val adresse: String?,
) {
    val fullName: String get() = "$firstName $lastName"
}

data class ClassSession(
    val id: String,
    val courseId: String,
    val courseName: String,
    val room: String,
    val teacherName: String?,
    val dayOfWeek: String, // lundi..dimanche, as returned by the API
    val startTime: String, // HH:mm
    val endTime: String,   // HH:mm
)

enum class AttendanceStatus { PRESENT, ABSENT, LATE, UNKNOWN }

data class AttendanceRecord(
    val id: String,
    val status: AttendanceStatus,
    val courseName: String,
    val dateTimeIso: String?,
)

data class AttendanceSummary(
    val percentage: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val minRequiredPercent: Int = 75,
)

enum class NotificationCategory { ACADEMIC, ADMIN, URGENT, OTHER }

data class AppNotification(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val body: String,
    val read: Boolean,
    val dateTimeIso: String,
)

data class DocumentRequest(
    val id: String,
    val type: String,
    val status: String,
    val dateIso: String,
)
