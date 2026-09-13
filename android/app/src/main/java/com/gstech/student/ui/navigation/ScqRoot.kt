package com.gstech.student.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.admin.AddUserScreen
import com.gstech.student.ui.admin.AdminEstablishmentsScreen
import com.gstech.student.ui.admin.AdminHomeScreen
import com.gstech.student.ui.admin.UsersListScreen
import com.gstech.student.ui.shared.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun ScqRoot(container: AppContainer, onSignedOut: () -> Unit) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val cur by nav.currentBackStackEntryAsState()
    val r = cur?.destination?.route
    val tabs = listOf("scq/home", "scq/efp", "scq/directeurs", "scq/profile")

    Scaffold(
        bottomBar = {
            if (r in tabs) {
                NavigationBar {
                    tabs.forEach { x ->
                        NavigationBarItem(
                            selected = r == x,
                            onClick = {
                                nav.navigate(x) {
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                val iconVector = when (x) {
                                    "scq/home" -> Icons.Default.Home
                                    "scq/efp" -> Icons.Default.Business
                                    "scq/directeurs" -> Icons.Default.Person
                                    else -> Icons.Default.Person
                                }
                                Icon(imageVector = iconVector, contentDescription = null)
                            },
                            label = {
                                Text(x.substringAfter('/').replaceFirstChar { it.uppercase() })
                            }
                        )
                    }
                }
            }
        }
    ) { p ->
        NavHost(
            navController = nav,
            startDestination = "scq/home",
            modifier = Modifier.padding(p)
        ) {
            composable("scq/home") {
                AdminHomeScreen(container, {}, "GSTech SCQ", false)
            }
            composable("scq/efp") {
                AdminEstablishmentsScreen(container)
            }
            composable("scq/directeurs") {
                UsersListScreen(
                    container, {}, { nav.navigate("scq/add") },
                    showAdminUsers = false,
                    initialRole = "directeur",
                    allowedRoleFilters = listOf(com.gstech.student.model.Role.DIRECTEUR),
                    filterLabels = mapOf(com.gstech.student.model.Role.DIRECTEUR to "Directeur")
                )
            }
            composable("scq/add") {
                AddUserScreen(container, { nav.popBackStack() }, setOf("directeur"))
            }
            composable("scq/profile") {
                SettingsScreen(
                    { nav.popBackStack() },
                    {},
                    {},
                    {
                        scope.launch {
                            container.authRepository.logout()
                            onSignedOut()
                        }
                    }
                )
            }
        }
    }
}