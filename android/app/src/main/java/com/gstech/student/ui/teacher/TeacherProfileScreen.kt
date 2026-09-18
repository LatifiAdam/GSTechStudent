package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch

@Composable
fun TeacherProfileScreen(container: AppContainer, onSignedOut: () -> Unit, onEditProfile: () -> Unit) {
    var state by remember { mutableStateOf<UiState<Triple<String, String, Int>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        state = safeCall {
            val id = container.tokenManager.userIdNow() ?: error("No signed-in user.")
            val user = container.profileRepository.rawUser(id)
            val courseCount = container.teacherRepository.getMyCourses().size
            Triple("${user.prenom} ${user.nom}", user.email, courseCount)
        }
    }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Text("My Profile", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp))

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) {}
            is UiState.Success -> {
                val (name, email, courseCount) = s.data
                Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(GSDivider), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = GSTextSecondary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Prof. $name", style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
                            Text("Dept. Computer Science", color = GSTextSecondary, fontSize = 13.sp)
                            Text("Associate Professor", color = GSBluePrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onEditProfile,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                    ) {
                        Text("Modifier mon profil")
                    }

                    Spacer(Modifier.height(18.dp))
                    GSCard {
                        Text("Teaching Information", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Spacer(Modifier.height(10.dp))
                        InfoRow("Active Courses", "$courseCount Courses This Semester")
                    }

                    Spacer(Modifier.height(16.dp))
                    GSCard {
                        Text("Contact Information", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Spacer(Modifier.height(10.dp))
                        InfoRow("Email", email)
                    }

                    Spacer(Modifier.height(16.dp))
                    GSCard {
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = GSTextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Theme (Dark Mode)", color = GSTextPrimary)
                            }
                            Switch(checked = com.gstech.student.ui.theme.ThemeController.darkTheme, onCheckedChange = { enabled -> com.gstech.student.ui.theme.ThemeController.setDarkMode(container.context, enabled) }, colors = SwitchDefaults.colors(checkedTrackColor = GSBluePrimary))
                        }
                        HorizontalDivider(color = GSDivider, modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            Modifier.fillMaxWidth().clickable { showSignOutConfirm = true }.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = GSDanger, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Sign Out", color = GSDanger, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutConfirm = false
                    scope.launch { container.authRepository.logout(); onSignedOut() }
                }) { Text("Sign Out", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = GSTextSecondary)
        Text(value, color = GSTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

