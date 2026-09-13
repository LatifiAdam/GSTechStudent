package com.gstech.student.ui.navigation

sealed class RootScreen(val route: String) {
    object Splash : RootScreen("splash")
    object Login : RootScreen("login")
    object ForgotPassword : RootScreen("forgot-password")
    object StudentApp : RootScreen("app/student")
    object TeacherApp : RootScreen("app/teacher")
    object GestionnaireApp : RootScreen("app/gestionnaire")
    object DirecteurApp : RootScreen("app/directeur")
    object AdminApp : RootScreen("app/admin")
    object SuperAdminApp : RootScreen("app/superadmin")
    object DfApp : RootScreen("app/df")
    object SrioApp : RootScreen("app/srio")
    object ScqApp : RootScreen("app/scq")
}
