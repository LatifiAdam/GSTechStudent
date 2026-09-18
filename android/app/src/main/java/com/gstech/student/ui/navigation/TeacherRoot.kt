package com.gstech.student.ui.navigation

import android.net.Uri
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
import com.gstech.student.ui.components.TeacherBottomBar
import com.gstech.student.ui.shared.ChangePasswordScreen
import com.gstech.student.ui.shared.EditProfileScreen
import com.gstech.student.ui.teacher.*

@Composable
fun TeacherRoot(
    container: AppContainer,
    onSignedOut: () -> Unit,
) {
    val navController: NavHostController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigateTo(screen: TeacherScreen) {
        navController.navigate(screen.route) {
            popUpTo(
                navController.graph.findStartDestination().id
            ) {
                saveState = true
            }

            launchSingleTop = true
            restoreState = true
        }
    }

    val showBottomBar =
        currentRoute in TeacherScreen.bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TeacherBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = ::navigateTo,
                )
            }
        },
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = TeacherScreen.Home.route,
            modifier = if (showBottomBar) {
                Modifier.padding(paddingValues)
            } else {
                Modifier
            },
        ) {

            /*
             * ---------------------------------------------------------
             * HOME
             * ---------------------------------------------------------
             */
            composable(TeacherScreen.Classes.route) { TeacherClassesScreen(container) }
            composable(TeacherScreen.Grading.route) { GradingScreen(container) }
            composable(TeacherScreen.Announcements.route) { TeacherAnnouncementsScreen(container) }
            composable(TeacherScreen.Justifications.route) { JustificationsScreen(container) }

            composable(
                route = TeacherScreen.Home.route,
            ) {
                TeacherHomeScreen(
                    container = container,

                    onOpenAttendance = {
                        navigateTo(
                            TeacherScreen.Attendance
                        )
                    },

                    onOpenAnnounce = { _, _ ->
                        navigateTo(TeacherScreen.Announcements)
                    },

                    onOpenAlerts = {
                        navController.navigate(
                            TeacherScreen.Alerts.route
                        )
                    },

                    onOpenCourses = {
                        navController.navigate(
                            TeacherScreen.Courses.route
                        )
                    },
                )
            }

            composable(
                route = TeacherScreen.Schedule.route,
            ) {
                TeacherTimetableScreen(
                    container = container,
                ) { creneauId, courseName ->

                    navController.navigate(
                        TeacherScreen.TakeAttendance.createRoute(
                            creneauId,
                            courseName,
                        )
                    )
                }
            }

            /*
             * ---------------------------------------------------------
             * ATTENDANCE
             * ---------------------------------------------------------
             */
            composable(
                route = TeacherScreen.Attendance.route,
            ) {
                TeacherAttendanceScreen(
                    container = container,

                    onPickCourse = { creneauId, courseName ->

                        navController.navigate(
                            TeacherScreen.TakeAttendance.createRoute(
                                creneauId,
                                courseName,
                            )
                        )
                    },
                )
            }

            /*
             * ---------------------------------------------------------
             * PROFILE
             * ---------------------------------------------------------
             */
            composable(
                route = TeacherScreen.Profile.route,
            ) {
                TeacherProfileScreen(
                    container = container,

                    onSignedOut = onSignedOut,
                    onEditProfile = { navController.navigate("teacher/edit-profile") },
                )
            }

            composable("teacher/edit-profile") {
                EditProfileScreen(container) { navController.popBackStack() }
            }

            /*
             * ---------------------------------------------------------
             * COURSES
             * ---------------------------------------------------------
             */
            composable(
                route = TeacherScreen.Courses.route,
            ) {
                MyCoursesScreen(
                    container = container,
                ) { courseId, courseName ->

                    navController.navigate(
                        TeacherScreen.NewAnnouncement.createRoute(
                            courseId,
                            courseName,
                        )
                    )
                }
            }

            /*
             * ---------------------------------------------------------
             * ALERTS / JUSTIFICATIONS
             * ---------------------------------------------------------
             */
            composable(
                route = TeacherScreen.Alerts.route,
            ) {
                JustificationsScreen(
                    container = container,
                )
            }

            /*
             * ---------------------------------------------------------
             * CHANGE PASSWORD
             * ---------------------------------------------------------
             */
            composable(
                route = "teacher/settings/change-password",
            ) {
                ChangePasswordScreen(container) {
                    navController.popBackStack()
                }
            }

            /*
             * ---------------------------------------------------------
             * TAKE ATTENDANCE
             * ---------------------------------------------------------
             */
            composable(
                route = TeacherScreen.TakeAttendance.route,

                arguments = listOf(
                    navArgument("creneauId") {
                        type = NavType.StringType
                    },

                    navArgument("courseName") {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->

                val creneauId =
                    entry.arguments
                        ?.getString("creneauId")
                        .orEmpty()

                val courseName =
                    Uri.decode(
                        entry.arguments
                            ?.getString("courseName")
                            .orEmpty()
                    )

                TakeAttendanceScreen(
                    container = container,
                    creneauId = creneauId,
                    courseName = courseName,
                ) {
                    navController.popBackStack()
                }
            }

            /*
             * ---------------------------------------------------------
             * NEW ANNOUNCEMENT
             * ---------------------------------------------------------
             */
            composable(
                route = TeacherScreen.NewAnnouncement.route,

                arguments = listOf(
                    navArgument("courseId") {
                        type = NavType.StringType
                    },

                    navArgument("courseName") {
                        type = NavType.StringType
                    },
                ),
            ) { entry ->

                val courseId =
                    entry.arguments
                        ?.getString("courseId")
                        .orEmpty()

                val courseName =
                    Uri.decode(
                        entry.arguments
                            ?.getString("courseName")
                            .orEmpty()
                    )

                NewAnnouncementScreen(
                    container = container,
                    courseId = courseId,
                    courseName = courseName,
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}