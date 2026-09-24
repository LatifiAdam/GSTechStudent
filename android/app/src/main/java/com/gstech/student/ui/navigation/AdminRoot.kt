package com.gstech.student.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.admin.*
import com.gstech.student.ui.components.AdminBottomBar
import com.gstech.student.ui.shared.*
import kotlinx.coroutines.launch

@Composable
fun AdminRoot(container: AppContainer, onSignedOut: () -> Unit) {
    val navController: NavHostController = rememberNavController()
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route
    val showBar = route in AdminScreen.bottomBarRoutes
    Scaffold(bottomBar = { if (showBar) AdminBottomBar(route) { screen -> navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState=true }; launchSingleTop=true; restoreState=true } } }) { padding ->
        NavHost(navController, AdminScreen.Home.route, modifier = if(showBar) Modifier.padding(padding) else Modifier) {
            composable(AdminScreen.Home.route) { AdminHomeScreen(container, onOpenSettings = { navController.navigate(AdminScreen.Profile.route) }, headerTitle = "GSTech DF") }
            composable(AdminScreen.Users.route) { UsersListScreen(container, onOpenUser={id->navController.navigate(AdminScreen.UserDetail.createRoute(id))}, onAddUser={navController.navigate(AdminScreen.NewUser.route)}) }
            composable(AdminScreen.Establishments.route) { AdminEstablishmentsScreen(container) { id -> navController.navigate(AdminScreen.UserDetail.createRoute(id)) } }
            composable(AdminScreen.NewUser.route) { AddUserScreen(container, { navController.popBackStack() }, setOf("srio", "scq")) }
            composable(AdminScreen.UserDetail.route, arguments=listOf(navArgument("userId"){type=NavType.StringType})) { e -> UserDetailScreen(container, e.arguments?.getString("userId").orEmpty()){navController.popBackStack()} }
            composable(AdminScreen.Profile.route) { SettingsScreen(onBack={navController.popBackStack()}, onEditProfile={navController.navigate("admin/edit-profile")}, onLanguage={navController.navigate("admin/language")}, onSignOut={scope.launch{container.authRepository.logout();onSignedOut()}}) }
            composable("admin/edit-profile") { EditProfileScreen(container){navController.popBackStack()} }
            composable("admin/language") { LanguageScreen{navController.popBackStack()} }
            composable("admin/settings/2fa") { TwoFactorScreen{navController.popBackStack()} }
        }
    }
}
