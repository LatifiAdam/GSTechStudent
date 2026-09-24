package com.gstech.student.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.gstech.student.data.AppContainer
import com.gstech.student.model.Role
import com.gstech.student.ui.admin.*
import com.gstech.student.ui.components.DirectorBottomBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccountCircle
import com.gstech.student.ui.shared.*
import kotlinx.coroutines.launch

@Composable fun DirectorRoot(container:AppContainer,onSignedOut:()->Unit){
 val nav=rememberNavController(); val scope=rememberCoroutineScope(); val entry by nav.currentBackStackEntryAsState(); val route=entry?.destination?.route; val bar=route in DirectorScreen.bottomBarRoutes
 Scaffold(bottomBar={if(bar)DirectorBottomBar(route){s->nav.navigate(s.route){popUpTo(nav.graph.findStartDestination().id){saveState=true};launchSingleTop=true;restoreState=true}}}){pad->NavHost(nav,DirectorScreen.Home.route,Modifier.padding(pad)){
  composable(DirectorScreen.Home.route){
      AdminHomeScreen(container, {}, "GSTech Directeur", true, listOf(
          AdminQuickAction("Formateurs", Icons.Default.Person) { nav.navigate("director/formateurs") },
          AdminQuickAction("Groupes", Icons.Default.Groups) { nav.navigate(DirectorScreen.Classes.route) },
          AdminQuickAction("Créneaux", Icons.Default.Schedule) { nav.navigate(DirectorScreen.Schedule.route) },
          AdminQuickAction("Profil", Icons.Default.AccountCircle) { nav.navigate(DirectorScreen.Profile.route) },
      ))
  }
  composable("director/formateurs"){UsersListScreen(
      container, { id -> nav.navigate("director/user/$id") }, { nav.navigate(DirectorScreen.NewUser.route) },
      showAdminUsers = false,
      initialRole = "formateur",
      allowedRoleFilters = listOf(Role.FORMATEUR),
      filterLabels = mapOf(Role.FORMATEUR to "Formateur")
  )}
  composable(DirectorScreen.Users.route){UsersListScreen(
      container, { id -> nav.navigate("director/user/$id") }, { nav.navigate(DirectorScreen.NewUser.route) },
      showAdminUsers = false,
      allowedRoleFilters = listOf(Role.GESTIONNAIRE, Role.FORMATEUR, Role.ETUDIANT),
      filterLabels = mapOf(
          Role.GESTIONNAIRE to "Gestionnaire",
          Role.FORMATEUR to "Formateur",
          Role.ETUDIANT to "Stagiaire"
      )
  )}
  composable(DirectorScreen.NewUser.route){AddUserScreen(container,{nav.popBackStack()},setOf("formateur"))}
  composable("director/user/{id}"){ backStack -> val id = backStack.arguments?.getString("id").orEmpty(); UserDetailScreen(container, id){nav.popBackStack()} }
  composable(DirectorScreen.Courses.route){AdminCoursesScreen(container)}
  composable(DirectorScreen.Validation.route){com.gstech.student.ui.director.ValidationScreen(container)}
  composable(DirectorScreen.Classes.route){AdminClassesScreen(container, readOnly = true, allowCreate = false)}
  composable(DirectorScreen.Assign.route){AdminAssignmentsScreen(container)}
  composable(DirectorScreen.Schedule.route){AdminSchedulerScreen(container, onOpenClasses = { nav.navigate(DirectorScreen.Classes.route) })}
  composable(DirectorScreen.Profile.route){SettingsScreen(onBack={nav.popBackStack()},onEditProfile={nav.navigate("director/edit-profile")},onLanguage={nav.navigate("director/language")},onSignOut={scope.launch{container.authRepository.logout();onSignedOut()}})}
  composable("director/edit-profile"){EditProfileScreen(container){nav.popBackStack()}}
  composable("director/language"){LanguageScreen{nav.popBackStack()}}
 }}
}
