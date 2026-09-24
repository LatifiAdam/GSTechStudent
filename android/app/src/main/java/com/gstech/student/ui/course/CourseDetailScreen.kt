package com.gstech.student.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.repository.CourseAnnouncement
import com.gstech.student.ui.components.CircularStat
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun CourseDetailScreen(
    container: AppContainer,
    courseId: String,
    courseName: String,
    onBack: () -> Unit,
) {
    val viewModel = remember(courseId, courseName) { CourseDetailViewModel(container, courseId, courseName) }
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary)
            }
            Spacer(Modifier.width(4.dp))
            Text(courseName, style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> Column(
                Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(4.dp))
                GSCard {
                    Text("Course Details", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                    Spacer(Modifier.height(10.dp))
                    DetailRow("Formateur", s.data.session?.teacherName ?: "—")
                    Spacer(Modifier.height(6.dp))
                    DetailRow("Credits", "3.0 Credits")
                    Spacer(Modifier.height(6.dp))
                    DetailRow("Location", s.data.session?.room ?: "—")
                }

                Spacer(Modifier.height(16.dp))
                GSCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularStat(percent = s.data.attendancePercent, size = 76.dp, color = GSTeal)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("$courseName Attendance", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                            Text("${s.data.attended} of ${s.data.total} classes attended", color = GSTextSecondary, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                GSCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grade Breakdown", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Text("Pending", color = GSTextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Les notes détaillées sont disponibles dans l'onglet Notes. La note finale est calculée par : " +
                            "(somme des notes) / nombre de notes.",
                        color = GSTextSecondary,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))
                GSCard {
                    Text("Course Announcements", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                    Spacer(Modifier.height(10.dp))
                    if (s.data.announcements.isEmpty()) {
                        Text("No announcements yet.", color = GSTextSecondary)
                    } else {
                        s.data.announcements.take(5).forEachIndexed { index, a ->
                            AnnouncementRow(a)
                            if (index != s.data.announcements.lastIndex) {
                                HorizontalDivider(color = GSDivider, modifier = Modifier.padding(vertical = 10.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = GSTextSecondary)
        Text(value, color = GSTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AnnouncementRow(announcement: CourseAnnouncement) {
    Column {
        Text(announcement.title, fontWeight = FontWeight.SemiBold, color = GSTextPrimary)
        Text(announcement.body, color = GSTextSecondary, fontSize = 13.sp, maxLines = 3)
    }
}
