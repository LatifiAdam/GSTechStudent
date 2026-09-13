package com.gstech.student.ui.navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.gstech.student.data.AppContainer
import com.gstech.student.model.Role
import com.gstech.student.ui.components.GestionnaireBottomBar
import com.gstech.student.ui.gestionnaire.GestionnaireHomeScreen
import com.gstech.student.ui.gestionnaire.GestionnaireClassesScreen
import com.gstech.student.ui.gestionnaire.GestionnaireDocumentsScreen
import com.gstech.student.ui.gestionnaire.GestionnaireDemandesScreen
import com.gstech.student.ui.admin.UsersListScreen
import com.gstech.student.ui.admin.AddUserScreen
import com.gstech.student.ui.admin.UserDetailScreen
import com.gstech.student.ui.shared.*
import kotlinx.coroutines.launch
@Composable fun GestionnaireRoot(container:AppContainer,onSignedOut:()->Unit){ val nav=rememberNavController(); val scope=rememberCoroutineScope(); val entry by nav.currentBackStackEntryAsState(); val route=entry?.destination?.route; val bar=route in setOf(GestionnaireScreen.Home.route,GestionnaireScreen.Classes.route,GestionnaireScreen.Demandes.route,GestionnaireScreen.Stagiaires.route,GestionnaireScreen.Profile.route); Scaffold(bottomBar={if(bar)GestionnaireBottomBar(route){nav.navigate(it){popUpTo(nav.graph.findStartDestination().id){saveState=true};launchSingleTop=true;restoreState=true}}}){pad->NavHost(nav,GestionnaireScreen.Home.route,Modifier.padding(pad)){ composable(GestionnaireScreen.Home.route){GestionnaireHomeScreen(container){nav.navigate(GestionnaireScreen.Documents.route)}} ; composable(GestionnaireScreen.Classes.route){GestionnaireClassesScreen(container)} ; composable(GestionnaireScreen.Documents.route){GestionnaireDocumentsScreen(container){nav.popBackStack()}} ; composable(GestionnaireScreen.Demandes.route){GestionnaireDemandesScreen(container)} ; composable(GestionnaireScreen.Stagiaires.route){UsersListScreen(
                container, { id -> nav.navigate("gestionnaire/user/$id") }, { nav.navigate("gestionnaire/add-stagiaire") },
                showAdminUsers = false,
                initialRole = "stagiaire",
                allowedRoleFilters = listOf(Role.ETUDIANT),
                filterLabels = mapOf(
                    Role.ETUDIANT to "Stagiaire"
                )
            )} ; composable("gestionnaire/add-stagiaire"){ AddUserScreen(container,{nav.popBackStack()},setOf("stagiaire")) } ; composable("gestionnaire/user/{id}"){ backStack -> val id = backStack.arguments?.getString("id").orEmpty(); UserDetailScreen(container, id){nav.popBackStack()} } ; composable(GestionnaireScreen.Profile.route){SettingsScreen(onBack={nav.popBackStack()},onEditProfile={nav.navigate(GestionnaireScreen.EditProfile.route)},onLanguage={nav.navigate(GestionnaireScreen.Language.route)},onSignOut={scope.launch{container.authRepository.logout();onSignedOut()}})} ; composable(GestionnaireScreen.EditProfile.route){EditProfileScreen(container){nav.popBackStack()}} ; composable(GestionnaireScreen.Language.route){LanguageScreen{nav.popBackStack()}} }} }
