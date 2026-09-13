package com.gstech.student.data.repository

import com.gstech.student.data.local.TokenManager
import com.gstech.student.data.remote.ScheduleApi
import com.gstech.student.data.remote.dto.CreneauDto
import com.gstech.student.model.ClassSession

class ScheduleRepository(
    private val api: ScheduleApi,
    private val tokenManager: TokenManager,
) {

    suspend fun getTeacherSchedule(teacherId: String): List<CreneauDto> =
        api.getSchedule(formateurId = teacherId)

    suspend fun getWeeklySchedule(): List<ClassSession> {
        val studentId = tokenManager.userIdNow()
            ?: error("No signed-in user.")

        return api.getSchedule(etudiantId = studentId).map {
            it.toClassSession()
        }
    }

    private fun CreneauDto.toClassSession() = ClassSession(
        id = idCreneau,
        courseId = (cours ?: affectation?.cours)
            ?.idCours
            .orEmpty(),
        courseName = (cours ?: affectation?.cours)
            ?.nomCours
            ?: "Course",
        room = salle ?: "—",
        teacherName = (cours?.formateur ?: affectation?.formateur)
            ?.utilisateur
            ?.let { "${it.prenom} ${it.nom}".trim() },
        dayOfWeek = jourSemaine,
        startTime = heureDebut,
        endTime = heureFin,
    )
}