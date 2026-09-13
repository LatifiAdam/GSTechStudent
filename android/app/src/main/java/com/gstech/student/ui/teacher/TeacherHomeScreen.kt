package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.TeacherCourse
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun TeacherHomeScreen(
    container: AppContainer,
    onOpenAttendance: () -> Unit,
    onOpenAnnounce: (courseId: String, courseName: String) -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenCourses: () -> Unit,
) {
    val viewModel = remember { TeacherHomeViewModel(container) }
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> TeacherHomeContent(
                data = s.data,
                onOpenAttendance = onOpenAttendance,
                onOpenAnnounce = { s.data.courses.firstOrNull()?.let { onOpenAnnounce(it.id, it.name) } },
                onOpenAlerts = onOpenAlerts,
                onOpenCourses = onOpenCourses,
            )
        }
    }
}

@Composable
private fun TeacherHomeContent(
    data: TeacherHomeData,
    onOpenAttendance: () -> Unit,
    onOpenAnnounce: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenCourses: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(GSBluePrimary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("GSTech", color = GSBluePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("FACULTY PLATFORM", color = GSTeal, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(Modifier.size(44.dp).clip(CircleShape).background(GSDivider), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = "Avatar", tint = GSTextSecondary)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Good Morning, Prof. ${data.name.substringAfterLast(' ')}", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        Text(data.department, color = GSTextSecondary)

        Spacer(Modifier.height(16.dp))
        GSCard {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Today's Lectures", color = GSTextSecondary, fontSize = 13.sp)
                    Text("${data.courses.size} Classes", color = GSBluePrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Column(Modifier.weight(1f)) {
                    Text("Total Stagieres", color = GSTextSecondary, fontSize = 13.sp)
                    Text("— Engaged", color = GSTeal, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickAction("Take Attendance", Icons.Filled.CalendarMonth, onOpenAttendance)
            QuickAction("Post Announcement", Icons.Filled.Campaign, onOpenAnnounce)
            QuickAction("View Stats", Icons.Filled.BarChart, onOpenCourses)
        }

        Spacer(Modifier.height(18.dp))
        Text("Today's Lectures", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
        Spacer(Modifier.height(10.dp))
        if (data.courses.isEmpty()) {
            Text("No courses assigned yet.", color = GSTextSecondary)
        } else {
            data.courses.forEach { course ->
                LectureRow(course)
                Spacer(Modifier.height(10.dp))
            }
        }

        if (data.pendingJustifications > 0) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GSWarning.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenAlerts),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(50), color = GSWarning) {
                        Text("${data.pendingJustifications} PENDING", color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Student absence justifications need your review.", color = GSTextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GSTextPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LectureRow(course: TeacherCourse) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GSSurface).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = GSBackground) {
            Text(course.schedule.ifBlank { "—" }, color = GSBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(course.name, fontWeight = FontWeight.SemiBold, color = GSTextPrimary)
            Text("${course.code} • ${course.room}", color = GSTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RowScope.QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.weight(1f).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(GSSurface), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = GSBluePrimary)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = GSTextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
