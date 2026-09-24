package com.gstech.student.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {

    object Home : Screen("student/home")

    object Attendance : Screen("student/attendance")

    object Schedule : Screen("student/schedule")

    object Notifications : Screen("student/notifications")

    object Profile : Screen("student/profile")

    object Documents : Screen("student/documents")
    object Grades : Screen("student/grades")
    object Announcements : Screen("student/announcements")

    object JustifyAbsence : Screen("student/justify-absence")

    object Settings : Screen("student/settings")

    object TwoFactor : Screen("student/two-factor")

    object Language : Screen("student/language")

    object CourseDetail : Screen("student/course/{courseId}/{courseName}") {
        fun createRoute(courseId: String, courseName: String) =
            "student/course/${Uri.encode(courseId)}/${Uri.encode(courseName)}"
    }

    companion object {
        val bottomBarRoutes = setOf(
            Home.route,
            Attendance.route,
            Schedule.route,
            Grades.route,
            Documents.route,
            Announcements.route,
            Profile.route
        )
    }
}