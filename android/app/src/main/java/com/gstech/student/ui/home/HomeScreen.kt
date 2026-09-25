package com.gstech.student.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 18.dp),
    ) {
        GSHeader(
            roleLabel = "SIS PLATFORM",
            greeting = "Bonjour, ${data.student.firstName}",
            avatarText = data.student.firstName.take(2),
        )

        Spacer(Modifier.height(7.dp))
        Text(
            "ID ${data.student.studentNumber ?: "—"} • ${data.student.promotion ?: ""}",
            color = GSTextSecondary,
            fontSize = 12.sp,
        )
        Text(
            "Groupe : ${data.student.groupName ?: "—"}",
            color = GSTextSecondary,
            fontSize = 12.sp,
        )

        Spacer(Modifier.height(16.dp))
        GSCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularStat(percent = data.summary.percentage, size = 78.dp)
                Spacer(Modifier.width(15.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (data.summary.percentage >= data.summary.minRequiredPercent) "Assiduité excellente" else "Assiduité à surveiller",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${data.summary.absent} absence(s) • ${data.summary.late} retard(s) sur ${data.summary.present + data.summary.absent + data.summary.late} séance(s).",
                        color = GSTextSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GSCard {
            SectionTitle("Cours du jour") {
                TextButton(onClick = onOpenSchedule, contentPadding = PaddingValues(0.dp)) {
                    Text("Voir tout", color = GSBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            if (data.todaysClasses.isEmpty()) {
                Text("Aucun cours prévu aujourd'hui.", color = GSTextSecondary, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                data.todaysClasses.forEach { session ->
                    ClassRow(session) { onOpenCourse(session.courseId, session.courseName) }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        data.latestNotification?.let { notif ->
            Spacer(Modifier.height(12.dp))
            GSCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(notif.category.name, GSDanger)
                    Spacer(Modifier.width(8.dp))
                    Text(notif.title, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                }
                Spacer(Modifier.height(6.dp))
                Text(notif.body, color = GSTextSecondary, fontSize = 12.sp, maxLines = 3)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Accès rapide")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickAction("Présence", Icons.Filled.EventAvailable, onOpenAttendance)
            QuickAction("Documents", Icons.Filled.Description, onOpenDocuments)
            QuickAction("Notes", Icons.Filled.Grade, onOpenGrades)
            QuickAction("Planning", Icons.Filled.CalendarMonth, onOpenSchedule)
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun ClassRow(session: ClassSession, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GSSurfaceTint)
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = GSSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GSDivider),
        ) {
            Text(
                "${session.startTime}\n${session.endTime}",
                color = GSBluePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(session.courseName, fontWeight = FontWeight.SemiBold, color = GSTextPrimary, fontSize = 13.sp)
            Spacer(Modifier.height(2.dp))
            Text(session.room, color = GSTextSecondary, fontSize = 11.sp)
        }
        Text("Cours", color = GSBluePrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RowScope.QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(GSSurface)
            .border(1.dp, GSDivider, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(GSBluePrimary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = GSBluePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, fontSize = 10.sp, color = GSTextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
