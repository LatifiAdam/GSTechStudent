package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.AttendanceApi
import com.gstech.student.data.remote.JustificationsApi
import com.gstech.student.data.remote.ReportsApi
import com.gstech.student.data.remote.dto.CreateJustificationRequest
import com.gstech.student.data.remote.dto.PresenceDto
import com.gstech.student.model.AttendanceRecord
import com.gstech.student.model.AttendanceStatus
import com.gstech.student.model.AttendanceSummary
import kotlin.math.roundToInt

class AttendanceRepository(
    private val attendanceApi: AttendanceApi,
    private val reportsApi: ReportsApi,
    private val justificationsApi: JustificationsApi,
    private val tokenManager: TokenManager,
) {
    suspend fun getHistory(): List<AttendanceRecord> {
        val studentId = tokenManager.userIdNow() ?: error("No signed-in user.")
        return attendanceApi.getHistory(studentId).map { it.toRecord() }
    }

    suspend fun getSummary(): AttendanceSummary {
        val studentId = tokenManager.userIdNow() ?: error("No signed-in user.")
        val report = reportsApi.getStudentReport(studentId)
        val pct = (report.tauxPresence).roundToInt()
        return AttendanceSummary(
            percentage = pct,
            present = report.detail.present,
            absent = report.detail.absent,
            late = report.detail.retard,
        )
    }

    suspend fun getCourseAttendance(@Suppress("UNUSED_PARAMETER") courseId: String, courseName: String): Pair<Int, Pair<Int, Int>> {
        val history = getHistory()
        val forCourse = history.filter { it.courseName == courseName }
        val total = forCourse.size
        val attended = forCourse.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
        val pct = if (total == 0) 0 else ((attended.toDouble() / total) * 100).roundToInt()
        return pct to (attended to total)
    }

    suspend fun submitJustification(presenceId: String, motif: String, attachmentUrl: String?) {
        justificationsApi.submit(CreateJustificationRequest(presenceId, motif, attachmentUrl))
    }

    private fun PresenceDto.toRecord() = AttendanceRecord(
        id = idPresence,
        status = when (statut) {
            "present" -> AttendanceStatus.PRESENT
            "absent" -> AttendanceStatus.ABSENT
            "retard" -> AttendanceStatus.LATE
            else -> AttendanceStatus.UNKNOWN
        },
        courseName = appel?.cours?.nomCours ?: "Course",
        dateTimeIso = appel?.dateHeure,
    )
}
