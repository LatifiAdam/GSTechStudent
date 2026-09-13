package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.AnnouncementsApi
import com.gstech.student.data.remote.AttendanceApi
import com.gstech.student.data.remote.CoursesApi
import com.gstech.student.data.remote.JustificationsApi
import com.gstech.student.data.remote.dto.*
import com.gstech.student.model.*
import java.time.OffsetDateTime

class TeacherRepository(
    private val coursesApi: CoursesApi,
    private val attendanceApi: AttendanceApi,
    private val justificationsApi: JustificationsApi,
    private val announcementsApi: AnnouncementsApi,
    private val tokenManager: TokenManager,
) {
    private suspend fun teacherId(): String = tokenManager.userIdNow() ?: error("No signed-in user.")

    suspend fun getMyCourses(): List<TeacherCourse> {
        val id = teacherId()
        return coursesApi.getAll(formateurId = id).map { it.toTeacherCourse() }
    }

    suspend fun openAttendanceCall(creneauId: String): AttendanceCall {
        val call = attendanceApi.openCall(OpenCallRequest(creneauId, OffsetDateTime.now().toString()))
        val full = attendanceApi.getCall(call.idAppel)
        return full.toAttendanceCall(creneauId)
    }

    suspend fun submitAttendance(callId: String, roster: List<RosterStudent>) {
        val exceptions = roster.filter { it.status != AttendanceStatus.PRESENT }
            .map { s ->
                AttendanceRecordEntry(
                    idEtudiant = s.id,
                    statut = when (s.status) {
                        AttendanceStatus.ABSENT -> "absent"
                        AttendanceStatus.LATE -> "retard"
                        else -> "present"
                    },
                )
            }
        if (exceptions.isNotEmpty()) {
            attendanceApi.updateRecords(callId, UpdateRecordsRequest(exceptions))
        }
        attendanceApi.validateCall(callId) // RG3: locks the call once submitted.
    }

    suspend fun getJustifications(status: JustificationStatus?, courseId: String? = null): List<JustificationItem> {
        val statutParam = when (status) {
            JustificationStatus.PENDING -> "en_attente"
            JustificationStatus.APPROVED -> "acceptee"
            JustificationStatus.REJECTED -> "refusee"
            null -> null
        }
        return justificationsApi.getAll(statutParam, courseId).map { it.toJustificationItem() }
    }

    suspend fun approve(justificationId: String) {
        justificationsApi.accept(justificationId)
    }

    suspend fun reject(justificationId: String, motif: String) {
        justificationsApi.refuse(justificationId, RefuseJustificationRequest(motif))
    }

    suspend fun postAnnouncement(courseId: String, title: String, body: String, typeAnnonce: String, eventDateIso: String?) {
        announcementsApi.create(
            CreateAnnouncementDto(
                titre = title,
                contenu = body,
                typeAnnonce = typeAnnonce,
                dateEvenement = eventDateIso,
                idCours = courseId,
            ),
        )
    }

    private fun CourseDto.toTeacherCourse() = TeacherCourse(
        id = idCours,
        name = nomCours,
        code = nomCours.filter { it.isLetterOrDigit() }.take(6).uppercase(),
        room = salle ?: "—",
        schedule = horaire?.take(16)?.replace('T', ' ') ?: "",
    )

    private fun PresenceWithStudentDto.toRosterStudent() = RosterStudent(
        id = etudiant?.idUtilisateur.orEmpty(),
        name = etudiant?.let { "${it.prenom.orEmpty()} ${it.nom.orEmpty()}".trim() } ?: "Student",
        presenceId = idPresence,
        status = when (statut) {
            "absent" -> AttendanceStatus.ABSENT
            "retard" -> AttendanceStatus.LATE
            else -> AttendanceStatus.PRESENT
        },
    )

    private fun CallDto.toAttendanceCall(courseId: String) = AttendanceCall(
        id = idAppel,
        courseId = courseId,
        validated = valide,
        roster = presences.orEmpty().map { it.toRosterStudent() },
    )

    private fun JustificationDto.toJustificationItem() = JustificationItem(
        id = idJustification,
        studentName = presence?.etudiant?.let { "${it.prenom.orEmpty()} ${it.nom.orEmpty()}".trim() } ?: "Student",
        presenceId = presence?.idPresence.orEmpty(),
        motif = motif,
        attachment = pieceJointe,
        status = when (statutJustification) {
            "acceptee" -> JustificationStatus.APPROVED
            "refusee" -> JustificationStatus.REJECTED
            else -> JustificationStatus.PENDING
        },
        dateIso = dateEnvoi,
        courseName = presence?.appel?.cours?.nomCours,
    )
}
