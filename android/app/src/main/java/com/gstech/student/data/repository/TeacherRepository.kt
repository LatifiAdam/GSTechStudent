package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.AnnouncementsApi
import com.gstech.student.data.remote.AttendanceApi
import com.gstech.student.data.remote.CoursesApi
import com.gstech.student.data.remote.JustificationsApi
import com.gstech.student.data.remote.dto.*
import com.gstech.student.model.*
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.LocalDate

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

    suspend fun getTodaysCourses(): List<TeacherCourse> {
        val id = teacherId()
        val today = LocalDate.now().dayOfWeek
        return coursesApi.getAll(formateurId = id)
            .flatMap { course ->
                course.affectations.orEmpty().flatMap { affectation ->
                    affectation.creneaux.orEmpty()
                        .filter { dayMatches(it.jourSemaine, today) }
                        .map { slot ->
                            TeacherCourse(
                                id = course.idCours,
                                name = course.nomCours,
                                code = course.nomCours.filter { ch -> ch.isLetterOrDigit() }.take(6).uppercase(),
                                room = slot.salle ?: course.salle ?: "—",
                                schedule = "${formatLectureTime(slot.heureDebut)} - ${formatLectureTime(slot.heureFin)}",
                                groupName = affectation.nomClasse ?: affectation.classe?.nomClasse,
                            )
                        }
                }
            }
            .distinctBy { "${it.id}|${it.groupName}|${it.schedule}|${it.room}" }
            .sortedBy { it.schedule }
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
        schedule = formatLectureTime(horaire),
        groupName = affectations?.firstOrNull()?.nomClasse
            ?: affectations?.firstOrNull()?.classe?.nomClasse,
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

private fun dayMatches(value: String?, day: DayOfWeek): Boolean {
    if (value.isNullOrBlank()) return false
    val normalized = value.trim().lowercase()
    val expected = when (day) {
        DayOfWeek.MONDAY -> setOf("1", "lundi", "monday")
        DayOfWeek.TUESDAY -> setOf("2", "mardi", "tuesday")
        DayOfWeek.WEDNESDAY -> setOf("3", "mercredi", "wednesday")
        DayOfWeek.THURSDAY -> setOf("4", "jeudi", "thursday")
        DayOfWeek.FRIDAY -> setOf("5", "vendredi", "friday")
        DayOfWeek.SATURDAY -> setOf("6", "samedi", "saturday")
        DayOfWeek.SUNDAY -> setOf("7", "dimanche", "sunday")
    }
    return normalized in expected
}

private fun formatLectureTime(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    val raw = value.trim().replace('T', ' ')
    val match = Regex("(\\d{1,2}):(\\d{2})(?:[:.]\\d{2})?").find(raw)
    return if (match != null) {
        val h = match.groupValues[1].toIntOrNull() ?: return raw
        val m = match.groupValues[2]
        "%02d:%s".format(h, m)
    } else raw.take(16)
}
