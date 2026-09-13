package com.gstech.student.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gstech.student.data.AppContainer
import com.gstech.student.model.Role
import com.gstech.student.ui.auth.LoginScreen
import com.gstech.student.ui.auth.TwoFactorVerificationScreen
import com.gstech.student.ui.shared.TwoFactorPreference
import com.gstech.student.ui.shared.ForgotPasswordScreen
import com.gstech.student.ui.splash.SplashScreen
import com.gstech.student.util.SessionManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun GSTechNavGraph(
    container: AppContainer
) {
    val navController: NavHostController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {

        // Register the global session-expiration handler.
        SessionManager.onSessionExpired = {
            coroutineScope.launch {

                navController.navigate(RootScreen.Login.route) {
                    // Remove Splash and all protected screens.
                    popUpTo(0) {
                        inclusive = true
                    }

                    launchSingleTop = true
                }
            }
        }

        onDispose {
            SessionManager.onSessionExpired = null
        }
    }

    NavHost(
        navController = navController,
        startDestination = RootScreen.Splash.route
    ) {

        // -------------------------
        // SPLASH
        // -------------------------
        composable(RootScreen.Splash.route) {

            SplashScreen(container) { role ->

                val destination = when (role) {

                    Role.ETUDIANT ->
                        RootScreen.StudentApp.route

                    Role.FORMATEUR ->
                        RootScreen.TeacherApp.route

                    Role.SUPER_ADMIN ->
                        RootScreen.SuperAdminApp.route

                    Role.DF ->
                        RootScreen.DfApp.route

                    Role.SRIO ->
                        RootScreen.SrioApp.route

                    Role.SCQ ->
                        RootScreen.ScqApp.route

                    Role.DIRECTEUR ->
                        RootScreen.DirecteurApp.route

                    Role.GESTIONNAIRE ->
                        RootScreen.GestionnaireApp.route

                    null ->
                        RootScreen.Login.route
                }

                navController.navigate(destination) {
                    popUpTo(RootScreen.Splash.route) {
                        inclusive = true
                    }
                }
            }
        }

        // -------------------------
        // LOGIN
        // -------------------------
        composable(RootScreen.Login.route) {

            LoginScreen(
                container = container,

                onLoggedIn = { role ->

                    val destination = when (role) {

                        Role.ETUDIANT ->
                            RootScreen.StudentApp.route

                        Role.FORMATEUR ->
                            RootScreen.TeacherApp.route

                        Role.SUPER_ADMIN ->
                            RootScreen.SuperAdminApp.route

                        Role.DF -> RootScreen.DfApp.route
                        Role.SRIO -> RootScreen.SrioApp.route
                        Role.SCQ -> RootScreen.ScqApp.route

                        Role.DIRECTEUR ->
                            RootScreen.DirecteurApp.route

                        Role.GESTIONNAIRE ->
                            RootScreen.GestionnaireApp.route
                    }

                    val target = if (TwoFactorPreference.enabled) "auth/two-factor-verification/${role.name}" else destination
                    navController.navigate(target) {
                        popUpTo(RootScreen.Login.route) { inclusive = true }
                    }
                },

                onForgotPassword = {
                    navController.navigate(
                        RootScreen.ForgotPassword.route
                    )
                }
            )
        }

        composable("auth/two-factor-verification/{role}") { entry ->
            val pendingRole = entry.arguments?.getString("role") ?: Role.ETUDIANT.name
            TwoFactorVerificationScreen(email = "your login email") {
                // Backend email-code validation is connected in the backend phase.
                val role = Role.valueOf(pendingRole)
                val destination = when(role){ Role.ETUDIANT -> RootScreen.StudentApp.route; Role.FORMATEUR -> RootScreen.TeacherApp.route; Role.GESTIONNAIRE -> RootScreen.GestionnaireApp.route; Role.DIRECTEUR -> RootScreen.DirecteurApp.route; Role.SUPER_ADMIN -> RootScreen.SuperAdminApp.route; Role.DF -> RootScreen.DfApp.route; Role.SRIO -> RootScreen.SrioApp.route; Role.SCQ -> RootScreen.ScqApp.route }
                navController.navigate(destination){ popUpTo(0){inclusive=true} }
            }
        }

        // -------------------------
        // FORGOT PASSWORD
        // -------------------------
        composable(RootScreen.ForgotPassword.route) {

            ForgotPasswordScreen {
                navController.popBackStack()
            }
        }

        // -------------------------
        // STUDENT APP
        // -------------------------
        composable(RootScreen.StudentApp.route) {

            StudentRoot(
                container = container,
                onSignedOut = {

                    navController.navigate(
                        RootScreen.Login.route
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // -------------------------
        // TEACHER APP
        // -------------------------
        composable(RootScreen.TeacherApp.route) {

            TeacherRoot(
                container = container,
                onSignedOut = {

                    navController.navigate(
                        RootScreen.Login.route
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(RootScreen.GestionnaireApp.route) { GestionnaireRoot(container) { navController.navigate(RootScreen.Login.route) { popUpTo(0) { inclusive = true } } } }

        composable(RootScreen.DirecteurApp.route) { DirectorRoot(container) { navController.navigate(RootScreen.Login.route) { popUpTo(0) { inclusive = true } } } }

        // -------------------------
        composable(RootScreen.SuperAdminApp.route) { SuperAdminRoot(container) { navController.navigate(RootScreen.Login.route) { popUpTo(0) { inclusive = true } } } }
        composable(RootScreen.DfApp.route) { AdminRoot(container) { navController.navigate(RootScreen.Login.route) { popUpTo(0) { inclusive = true } } } }
        composable(RootScreen.SrioApp.route) { SrioRoot(container) { navController.navigate(RootScreen.Login.route) { popUpTo(0) { inclusive = true } } } }
        composable(RootScreen.ScqApp.route) { ScqRoot(container) { navController.navigate(RootScreen.Login.route) { popUpTo(0) { inclusive = true } } } }

        // ADMIN APP
        // -------------------------
        composable(RootScreen.AdminApp.route) {

            AdminRoot(
                container = container,
                onSignedOut = {

                    navController.navigate(
                        RootScreen.Login.route
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}