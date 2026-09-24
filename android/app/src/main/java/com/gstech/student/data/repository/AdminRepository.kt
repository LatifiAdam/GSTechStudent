package com.gstech.student.data.repository

import com.gstech.student.data.remote.AnnouncementsApi
import com.gstech.student.data.remote.CoursesApi
import com.gstech.student.data.remote.DocumentRequestsApi
import com.gstech.student.data.remote.ReportsApi
import com.gstech.student.data.remote.UsersApi
import com.gstech.student.data.remote.EstablishmentsApi
import com.gstech.student.data.remote.dto.*
import com.gstech.student.model.*
import kotlin.math.roundToInt

class AdminRepository(
    private val usersApi: UsersApi,
    private val coursesApi: CoursesApi,
    private val documentRequestsApi: DocumentRequestsApi,
    private val announcementsApi: AnnouncementsApi,
    private val reportsApi: ReportsApi,
    private val establishmentsApi: EstablishmentsApi,
) {

    suspend fun getDirectorDashboard(): AdminDashboard {
        val all = usersApi.getAll(null)
        val students = usersApi.getAll("stagiaire")
        val teachers = usersApi.getAll("formateur")
        val gestionnaires = usersApi.getAll("gestionnaire")
        return AdminDashboard(
            totalUsers = all.size,
            totalAdmins = 0,
            totalDirectors = 0,
            totalStudents = students.size,
            totalTeachers = teachers.size,
            totalGestionnaires = gestionnaires.size,
        )
    }

    suspend fun getDashboard(): AdminDashboard {
        val all = usersApi.getAll(null)
        val admins = usersApi.getAll("superadmin")
        val directors = usersApi.getAll("directeur")
        return AdminDashboard(totalUsers = all.size, totalAdmins = admins.size, totalDirectors = directors.size)
    }

    suspend fun getDashboardForRole(role: String?): AdminDashboard = when (role?.lowercase()) {
        "srio" -> {
            val gs = usersApi.getAll("gestionnaire")
            val ds = usersApi.getAll("directeur")
            val efp = establishmentsApi.getAll()
            AdminDashboard(totalUsers=gs.size, totalAdmins=0, totalDirectors=ds.size, totalGestionnaires=gs.size, totalEfp=efp.size)
        }
        "scq" -> { val ds = usersApi.getAll("directeur"); AdminDashboard(totalUsers=ds.size, totalAdmins=0, totalDirectors=ds.size) }
        else -> getDashboard()
    }

    suspend fun getRecentActivity(limit: Int = 10): List<AdminActivity> {
        return reportsApi
            .getRecentActivity(limit)
            .map { item ->
                AdminActivity(
                    type = item.type,
                    message = item.message,
                    date = item.date,
                )
            }
    }
    suspend fun getUsers(role: Role?): List<AdminUser> {
        val roleParam = role?.let {
            when (it) {
                Role.ETUDIANT -> "stagiaire"
                Role.DF -> "df"
                Role.SRIO -> "srio"
                Role.SCQ -> "scq"
                Role.FORMATEUR -> "formateur"
                Role.SUPER_ADMIN -> "superadmin"
                Role.DIRECTEUR -> "directeur"
                Role.GESTIONNAIRE -> "gestionnaire"
            }
        }
        return usersApi.getAll(roleParam).map { it.toAdminUser() }
    }

    suspend fun getUser(id: String): AdminUser = usersApi.getUser(id).toAdminUser()

    suspend fun getUsersDto(role: Role?): List<UserDto> = usersApi.getAll(role?.let { when(it){ Role.SUPER_ADMIN->"superadmin"; Role.DF->"df"; Role.SRIO->"srio"; Role.SCQ->"scq"; Role.DIRECTEUR->"directeur"; Role.GESTIONNAIRE->"gestionnaire"; Role.FORMATEUR->"formateur"; Role.ETUDIANT->"stagiaire" } })

    suspend fun getUserDto(id: String): UserDto = usersApi.getUser(id)

    suspend fun updateUser(id: String, request: UpdateUserRequest): UserDto = usersApi.update(id, request)

    suspend fun createUser(request: CreateUserRequest) {
        usersApi.create(request)
    }

    suspend fun deleteUser(id: String) {
        usersApi.delete(id)
    }

    suspend fun getCourses(): List<AdminCourse> = coursesApi.getAll().map { it.toAdminCourse() }

    suspend fun createCourse(name: String) {
        coursesApi.create(CreateCourseRequest(name))
    }

    suspend fun deleteCourse(id: String) {
        coursesApi.delete(id).let { check(it.isSuccessful) { "Unable to delete course (${it.code()})" } }
    }

    suspend fun getDocumentRequests(): List<DocumentRequest> =
        documentRequestsApi.list().map { DocumentRequest(it.idDemande, it.typeDocument.orEmpty(), it.statut, it.dateDemande) }

    suspend fun generateDocument(id: String) {
        documentRequestsApi.generate(id)
    }

    suspend fun refuseDocument(id: String, motif: String) {
        documentRequestsApi.refuse(id, RefuseJustificationRequest(motif))
    }

    suspend fun getAnnouncements(): List<CourseAnnouncement> =
        announcementsApi.getAll().sortedByDescending { it.datePublication }
            .map { CourseAnnouncement(it.titre, it.contenu, it.datePublication) }

    suspend fun postGeneralAnnouncement(title: String, body: String, typeAnnonce: String) {
        announcementsApi.create(CreateAnnouncementDto(title, body, typeAnnonce, null, null))
    }

    private fun UserDto.toAdminUser() = AdminUser(
        id = idUtilisateur,
        name = "$prenom $nom",
        role = when (role) {
            "stagiaire" -> Role.ETUDIANT
            "formateur" -> Role.FORMATEUR
            "superadmin", "admin", "administrateur" -> Role.SUPER_ADMIN
            "df" -> Role.DF
            "srio" -> Role.SRIO
            "scq" -> Role.SCQ
            "directeur" -> Role.DIRECTEUR
            "gestionnaire" -> Role.GESTIONNAIRE
            else -> when {
                numeroEtudiant != null -> Role.ETUDIANT
                niveauAcces != null -> Role.SUPER_ADMIN
                module != null -> Role.FORMATEUR
                else -> Role.ETUDIANT
            }
        },
        department = module,
        email = email,
        studentNumber = numeroEtudiant,
        promotion = promotion,
        cin = cin,
        telephone = telephone,
        adresse = adresse,
    )

    private fun AffectationDto.formateurNom(): String? = formateur?.utilisateur?.let { "${it.prenom} ${it.nom}" }

    private fun CourseDto.toAdminCourse() = AdminCourse(
        id = idCours,
        name = nomCours,
        code = nomCours.filter { it.isLetterOrDigit() }.take(6).uppercase(),
        teacherName = formateur?.utilisateur?.let { "${it.prenom} ${it.nom}" } ?: affectations?.firstOrNull()?.formateurNom() ?: "—",
        room = salle ?: "—",
    )
}
