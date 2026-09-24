package com.gstech.student.model

data class TeacherCourse(
    val id: String,
    val name: String,
    val code: String, // GSTech has no course-code field server-side; derived from the name.
    val room: String,
    val schedule: String,
    val groupName: String? = null,
)

data class RosterStudent(
    val id: String,
    val name: String,
    val presenceId: String? = null,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
)

data class AttendanceCall(
    val id: String,
    val courseId: String,
    val validated: Boolean,
    val roster: List<RosterStudent>,
)

enum class JustificationStatus { PENDING, APPROVED, REJECTED }

data class JustificationItem(
    val id: String,
    val studentName: String,
    val presenceId: String,
    val motif: String,
    val attachment: String?,
    val status: JustificationStatus,
    val dateIso: String,
    val courseName: String?,
)
