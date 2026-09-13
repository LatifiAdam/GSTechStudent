package com.gstech.student.ui.navigation

sealed class DirectorScreen(val route: String) {
    object Home : DirectorScreen("director/home")
    object Users : DirectorScreen("director/users")
    object Validation : DirectorScreen("director/validation")
    object Gestion : DirectorScreen("director/gestion")
    object Courses : DirectorScreen("director/courses")
    object Classes : DirectorScreen("director/classes")
    object Assign : DirectorScreen("director/assign")
    object Schedule : DirectorScreen("director/schedule")
    object Alerts : DirectorScreen("director/alerts")
    object Profile : DirectorScreen("director/profile")
    object UserDetail : DirectorScreen("director/users/{userId}") {
        fun createRoute(userId: String) = "director/users/${android.net.Uri.encode(userId)}"
    }
    object NewUser : DirectorScreen("director/users/add")

    companion object {
        val bottomBarRoutes: Set<String> = setOf(
            Home.route, Users.route, Validation.route, Gestion.route, Courses.route, Classes.route,
            Assign.route, Schedule.route, Alerts.route, Profile.route
        )
    }
}
