package com.gstech.student.ui.navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.admin.*
import com.gstech.student.ui.shared.*
import kotlinx.coroutines.launch

@Composable fun SrioRoot(container: AppContainer, onSignedOut:()->Unit){
 val nav= rememberNavController(); val scope=rememberCoroutineScope(); val current by nav.currentBackStackEntryAsState(); val r=current?.destination?.route
 val tabs=listOf("srio/home","srio/efp","srio/gs","srio/profile")
 Scaffold(bottomBar={if(r in tabs) NavigationBar {tabs.forEach{ x -> NavigationBarItem(selected=r==x,onClick={nav.navigate(x){launchSingleTop=true}},icon={},label={Text(x.substringAfter('/').replaceFirstChar{it.uppercase()})})}}}){pad->NavHost(nav,"srio/home",Modifier.padding(pad)){composable("srio/home"){AdminHomeScreen(container,{} ,"GSTech SRIO",false)};composable("srio/efp"){AdminEstablishmentsScreen(container)};composable("srio/gs"){UsersListScreen(
     container, {}, { nav.navigate("srio/add") },
     showAdminUsers = false,
     initialRole = "gestionnaire",
     allowedRoleFilters = listOf(com.gstech.student.model.Role.GESTIONNAIRE),
     filterLabels = mapOf(com.gstech.student.model.Role.GESTIONNAIRE to "Gestionnaire")
 )};composable("srio/add"){AddUserScreen(container,{nav.popBackStack()},setOf("gestionnaire"))};composable("srio/profile"){SettingsScreen({nav.popBackStack()},{nav.navigate("srio/edit-profile")},{},{scope.launch{container.authRepository.logout();onSignedOut()}})};composable("srio/edit-profile"){EditProfileScreen(container){nav.popBackStack()}}}}
}
