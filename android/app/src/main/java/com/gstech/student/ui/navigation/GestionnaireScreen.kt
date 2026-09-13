package com.gstech.student.ui.navigation

sealed class GestionnaireScreen(val route: String) {
    object Home : GestionnaireScreen("gestionnaire/home")
    object Documents : GestionnaireScreen("gestionnaire/documents")
    object Demandes : GestionnaireScreen("gestionnaire/demandes")
    object Classes : GestionnaireScreen("gestionnaire/classes")
    object Stagiaires : GestionnaireScreen("gestionnaire/stagiaires")
    object Justifications : GestionnaireScreen("gestionnaire/justifications")
    object Profile : GestionnaireScreen("gestionnaire/profile")
    object ChangePassword : GestionnaireScreen("gestionnaire/settings/change-password")
    object EditProfile : GestionnaireScreen("gestionnaire/settings/edit-profile")
    object Language : GestionnaireScreen("gestionnaire/settings/language")
    object TwoFactor : GestionnaireScreen("gestionnaire/settings/2fa")
}
