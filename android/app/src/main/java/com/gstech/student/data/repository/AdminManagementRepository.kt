package com.gstech.student.data.repository

import com.gstech.student.data.remote.AffectationsApi
import com.gstech.student.data.remote.ClassesApi
import com.gstech.student.data.remote.CoursesApi
import com.gstech.student.data.remote.ScheduleApi
import com.gstech.student.data.remote.UsersApi
import com.gstech.student.data.remote.dto.AffectationDto
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.CourseDto
import com.gstech.student.data.remote.dto.CreneauDto
import com.gstech.student.data.remote.dto.CreateAffectationRequest
import com.gstech.student.data.remote.dto.CreateClassRequest
import com.gstech.student.data.remote.dto.CreateCreneauRequest
import com.gstech.student.data.remote.dto.StudentUserDto
import com.gstech.student.data.remote.dto.UpdateCreneauRequest
import com.gstech.student.data.remote.dto.UserDto
import retrofit2.Response

class AdminManagementRepository(
    private val classesApi: ClassesApi,
    private val affectationsApi: AffectationsApi,
    private val scheduleApi: ScheduleApi,
    private val usersApi: UsersApi,
    private val coursesApi: CoursesApi,
) {
    suspend fun classes(): List<ClassDto> = classesApi.getAll()

    suspend fun createClass(name: String, description: String? = null): ClassDto =
        classesApi.create(CreateClassRequest(name, description))

    suspend fun updateClass(id: String, name: String, description: String? = null): ClassDto =
        classesApi.update(id, CreateClassRequest(name, description))

    suspend fun deleteClass(id: String): Response<Unit> = classesApi.delete(id)

    suspend fun students(classId: String): List<StudentUserDto> = classesApi.students(classId)

    suspend fun assignStudent(classId: String, studentId: String): StudentUserDto =
        classesApi.assignStudent(classId, studentId)

    suspend fun removeStudent(classId: String, studentId: String): Response<Unit> =
        classesApi.removeStudent(classId, studentId)

    suspend fun assignments(): List<AffectationDto> = affectationsApi.getAll()

    suspend fun assignmentsForClass(classId: String): List<AffectationDto> =
        assignments().filter { it.idClasse == classId }

    suspend fun createAssignment(classId: String, courseId: String, teacherId: String): AffectationDto =
        affectationsApi.create(CreateAffectationRequest(classId, courseId, teacherId))

    suspend fun deleteAssignment(id: String): Response<Unit> = affectationsApi.delete(id)

    suspend fun schedule(): List<CreneauDto> = assignments().flatMap { it.creneaux.orEmpty() }

    suspend fun scheduleForClass(classId: String): List<CreneauDto> =
        scheduleApi.getSchedule(classeId = classId)
            .sortedWith(compareBy({ dayOrder(it.jourSemaine) }, { it.heureDebut }))

    suspend fun scheduleForAssignment(assignmentId: String): List<CreneauDto> =
        assignments().firstOrNull { it.idAffectation == assignmentId }?.creneaux.orEmpty()

    suspend fun scheduleForTeacher(teacherId: String): List<CreneauDto> =
        assignments().filter { it.idFormateur == teacherId }
            .flatMap { it.creneaux.orEmpty() }
            .sortedWith(compareBy({ dayOrder(it.jourSemaine) }, { it.heureDebut }))

    suspend fun scheduleForCourse(courseId: String): List<CreneauDto> =
        assignments().filter { it.idCours == courseId }.flatMap { it.creneaux.orEmpty() }

    suspend fun createSlot(body: CreateCreneauRequest): CreneauDto = scheduleApi.create(body)

    suspend fun updateSlot(id: String, body: UpdateCreneauRequest): CreneauDto =
        scheduleApi.update(id, body)

    suspend fun deleteSlot(id: String): Response<Unit> = scheduleApi.delete(id)

    suspend fun studentsUnassigned(): List<UserDto> = usersApi.getAll("stagiaire")
    suspend fun teachers(): List<UserDto> = usersApi.getAll("formateur")
    suspend fun courses(): List<CourseDto> = coursesApi.getAll()

    suspend fun findClass(id: String): ClassDto? = classes().firstOrNull { it.idClasse == id }
    suspend fun findAssignment(id: String): AffectationDto? = assignments().firstOrNull { it.idAffectation == id }

    suspend fun hasScheduleConflict(
        classId: String,
        day: String,
        start: String,
        end: String,
        ignoreSlotId: String? = null,
    ): Boolean = scheduleForClass(classId).any { slot ->
        slot.idCreneau != ignoreSlotId &&
            slot.jourSemaine.equals(day, ignoreCase = true) &&
            timesOverlap(slot.heureDebut, slot.heureFin, start, end)
    }

    private fun timesOverlap(aStart: String, aEnd: String, bStart: String, bEnd: String): Boolean =
        toMinutes(aStart) < toMinutes(bEnd) && toMinutes(bStart) < toMinutes(aEnd)

    private fun toMinutes(value: String): Int {
        val parts = value.take(5).split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
            (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    private fun dayOrder(day: String): Int = when (day.lowercase()) {
        "lundi" -> 1
        "mardi" -> 2
        "mercredi" -> 3
        "jeudi" -> 4
        "vendredi" -> 5
        "samedi" -> 6
        "dimanche" -> 7
        else -> 99
    }
}
