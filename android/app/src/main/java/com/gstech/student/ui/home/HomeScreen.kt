package com.gstech.student.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.gstech.student.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.ClassSession
import com.gstech.student.ui.components.*
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenAttendance: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenCourse: (id: String, name: String) -> Unit,
    onOpenGrades: () -> Unit = {},
) {
    val viewModel = remember { HomeViewModel(container) }
    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.message) { viewModel.load() }
        is UiState.Success -> HomeContent(
            data = s.data,
            onOpenAttendance = onOpenAttendance,
            onOpenSchedule = onOpenSchedule,
            onOpenDocuments = onOpenDocuments,
            onOpenCourse = onOpenCourse,
            onOpenGrades = onOpenGrades,
        )
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    onOpenAttendance: () -> Unit,
    onOpenSchedule: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenCourse: (id: String, name: String) -> Unit,
    onOpenGrades: () -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GSSurface,
                    modifier = Modifier.size(42.dp),
                    tonalElevation = 2.dp,
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_v5),
                        contentDescription = "GSTech",
                        modifier = Modifier.padding(3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("GSTech", color = GSBluePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("SIS PLATFORM", color = GSTeal, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(Modifier.size(44.dp).clip(CircleShape).background(GSDivider), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = "Avatar", tint = GSTextSecondary)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Good Morning, ${data.student.firstName}", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        Text(
            "ID: ${data.student.studentNumber ?: "—"} • ${data.student.promotion ?: ""}",
            color = GSTextSecondary,
        )
        Text(
            "Groupe : ${data.student.groupName ?: "—"}",
            color = GSTextSecondary,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(16.dp))
        GSCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularStat(percent = data.summary.percentage, size = 76.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        if (data.summary.percentage >= data.summary.minRequiredPercent) "Excellent Attendance" else "Attendance Needs Attention",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary,
                    )
                    Text(
                        "You have ${data.summary.absent} absence(s) this semester. Keep it up!",
                        color = GSTextSecondary,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GSCard {
            SectionTitle("Today's Classes") {
                TextButton(onClick = onOpenSchedule) { Text("View All", color = GSTeal, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(8.dp))
            if (data.todaysClasses.isEmpty()) {
                Text("No classes scheduled for today.", color = GSTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                data.todaysClasses.forEach { session ->
                    ClassRow(session) { onOpenCourse(session.courseId, session.courseName) }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        data.latestNotification?.let { notif ->
            Spacer(Modifier.height(16.dp))
            GSCard {
                StatusPill(notif.category.name, GSDanger)
                Spacer(Modifier.height(6.dp))
                Text(notif.title, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                Text(notif.body, color = GSTextSecondary, fontSize = 13.sp, maxLines = 2)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickAction("Absence", Icons.Filled.WarningAmber, onOpenAttendance)
            QuickAction("Document", Icons.Filled.Description, onOpenDocuments)
            QuickAction("Grades", Icons.Filled.StarBorder, onOpenGrades)
            QuickAction("Schedule", Icons.Filled.CalendarMonth, onOpenSchedule)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ClassRow(session: ClassSession, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GSBackground)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(GSSurface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text("${session.startTime} - ${session.endTime}", color = GSBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(session.courseName, fontWeight = FontWeight.SemiBold, color = GSTextPrimary)
            Text(session.room, color = GSTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RowScope.QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GSSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = GSBluePrimary)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = GSTextPrimary)
    }
}
