package com.gstech.student.ui.navigation

sealed class TeacherScreen(val route: String) {
    object Home : TeacherScreen("teacher/home")
    object Schedule : TeacherScreen("teacher/schedule")
    object Attendance : TeacherScreen("teacher/attendance")
    object Profile : TeacherScreen("teacher/profile")
    object Classes : TeacherScreen("teacher/classes")
    object Grading : TeacherScreen("teacher/grading")
    object Announcements : TeacherScreen("teacher/announcements")
    object Justifications : TeacherScreen("teacher/justifications")

    // Secondary teacher screens remain available without occupying the bottom bar.
    object Courses : TeacherScreen("teacher/courses")
    object Alerts : TeacherScreen("teacher/alerts")

    object TakeAttendance : TeacherScreen("teacher/attendance/{creneauId}/{courseName}") {
        fun createRoute(creneauId: String, courseName: String) =
            "teacher/attendance/${android.net.Uri.encode(creneauId)}/${android.net.Uri.encode(courseName)}"
    }

    object NewAnnouncement : TeacherScreen("teacher/announce/{courseId}/{courseName}") {
        fun createRoute(courseId: String, courseName: String) =
            "teacher/announce/${android.net.Uri.encode(courseId)}/${android.net.Uri.encode(courseName)}"
    }

    companion object {
        val bottomBarRoutes = setOf(
            Home.route,
            Schedule.route,
            Attendance.route,
            Profile.route,
            Classes.route,
            Grading.route,
            Announcements.route,
            Justifications.route
        )
    }
}
