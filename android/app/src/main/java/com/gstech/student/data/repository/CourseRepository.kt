package com.gstech.student.data.repository

import com.gstech.student.data.remote.AnnouncementsApi

data class CourseAnnouncement(val title: String, val body: String, val dateIso: String)

class CourseRepository(private val api: AnnouncementsApi) {
    suspend fun getAnnouncementsRaw() = api.getAll()

    suspend fun getAnnouncementsForCourse(courseId: String): List<CourseAnnouncement> {
        return api.getAll()
            .filter { it.cours?.idCours == courseId || it.typeAnnonce == "generale" }
            .sortedByDescending { it.datePublication }
            .map { CourseAnnouncement(it.titre, it.contenu, it.datePublication) }
    }
}
