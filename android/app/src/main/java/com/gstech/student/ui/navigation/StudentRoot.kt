package com.gstech.student.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.attendance.AttendanceScreen
import com.gstech.student.ui.attendance.JustifyAbsenceScreen
import com.gstech.student.ui.components.GSBottomBar
import com.gstech.student.ui.course.CourseDetailScreen
import com.gstech.student.ui.documents.DocumentsScreen
import com.gstech.student.ui.home.HomeScreen
import com.gstech.student.ui.profile.ProfileScreen
import com.gstech.student.ui.schedule.ScheduleScreen
import com.gstech.student.ui.grades.GradesScreen
import com.gstech.student.ui.announcements.AnnouncementsScreen
import com.gstech.student.ui.shared.ChangePasswordScreen
import com.gstech.student.ui.shared.LanguageScreen
import com.gstech.student.ui.shared.SettingsScreen
import com.gstech.student.ui.shared.TwoFactorScreen


@Composable
fun StudentRoot(
    container: AppContainer,
    onSignedOut: () -> Unit
) {
    val navController: NavHostController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route


    fun navigateToMainScreen(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }

            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in Screen.bottomBarRoutes) {
                GSBottomBar(
                    currentRoute = currentRoute
                ) { screen ->
                    navigateToMainScreen(screen.route)
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = if (currentRoute in Screen.bottomBarRoutes) {
                Modifier.padding(padding)
            } else {
                Modifier
            }
        ) {

            // =========================================================
            // HOME
            // =========================================================

            composable(Screen.Home.route) {
                HomeScreen(
                    container = container,

                    onOpenAttendance = {
                        navigateToMainScreen(Screen.Attendance.route)
                    },

                    onOpenSchedule = {
                        navigateToMainScreen(Screen.Schedule.route)
                    },

                    onOpenDocuments = { navigateToMainScreen(Screen.Documents.route) },
                    onOpenGrades = { navigateToMainScreen(Screen.Grades.route) },

                    onOpenCourse = { id, name ->
                        navController.navigate(
                            Screen.CourseDetail.createRoute(id, name)
                        )
                    }
                )
            }

            composable(Screen.Grades.route) { GradesScreen(container) }
            composable(Screen.Announcements.route) { AnnouncementsScreen(container) }

            // =========================================================
            // ATTENDANCE
            // =========================================================

            composable(Screen.Attendance.route) {
                AttendanceScreen(container)
            }

            // =========================================================
            // SCHEDULE
            // =========================================================

            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    container = container
                ) { id, name ->

                    navController.navigate(
                        Screen.CourseDetail.createRoute(id, name)
                    )
                }
            }

            // =========================================================
            // PROFILE
            // =========================================================

            composable(Screen.Profile.route) {
                ProfileScreen(
                    container = container,
                    onSignedOut = onSignedOut
                )
            }

            // =========================================================
            // DOCUMENTS
            // =========================================================

            composable(Screen.Documents.route) {
                DocumentsScreen(
                    container = container
                ) {
                    navController.popBackStack()
                }
            }

            // =========================================================
            // JUSTIFY ABSENCE
            // =========================================================

            composable(Screen.JustifyAbsence.route) {
                JustifyAbsenceScreen(
                    container = container
                ) {
                    navController.popBackStack()
                }
            }

            // =========================================================
            // SETTINGS
            // =========================================================

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },

                    onLanguage = {
                        navController.navigate(
                            Screen.Language.route
                        )
                    },

                    onSignOut = onSignedOut
                )
            }

            // =========================================================
            // CHANGE PASSWORD
            // =========================================================

            composable("student/change-password") {
                ChangePasswordScreen(container) {
                    navController.popBackStack()
                }
            }

            // =========================================================
            // TWO FACTOR
            // =========================================================

            composable(Screen.TwoFactor.route) {
                TwoFactorScreen {
                    navController.popBackStack()
                }
            }

            // =========================================================
            // LANGUAGE
            // =========================================================

            composable(Screen.Language.route) {
                LanguageScreen {
                    navController.popBackStack()
                }
            }

            // =========================================================
            // COURSE DETAIL
            // =========================================================

            composable(
                route = Screen.CourseDetail.route,
                arguments = listOf(
                    navArgument("courseId") {
                        type = NavType.StringType
                    },
                    navArgument("courseName") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->

                val courseId =
                    entry.arguments
                        ?.getString("courseId")
                        .orEmpty()

                val courseName =
                    android.net.Uri.decode(
                        entry.arguments
                            ?.getString("courseName")
                            .orEmpty()
                    )

                CourseDetailScreen(
                    container = container,
                    courseId = courseId,
                    courseName = courseName,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}