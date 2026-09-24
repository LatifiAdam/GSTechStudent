package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import com.gstech.student.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.TeacherCourse
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSHeader
import com.gstech.student.ui.components.SectionTitle
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 18.dp)) {
        GSHeader(
            roleLabel = "FACULTY PLATFORM",
            greeting = "Bonjour, Prof. ${data.name.substringAfterLast(' ')}",
            avatarText = data.name.trim().split(" ").lastOrNull()?.take(2),
        )
        Spacer(Modifier.height(4.dp))
        Text(data.department, color = GSTextSecondary, fontSize = 12.sp)

        Spacer(Modifier.height(15.dp))
        GSCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Cours du jour", color = GSTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text("${data.courses.size}", color = GSBluePrimary, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                    Text("séances", color = GSTextSecondary, fontSize = 11.sp)
                }
                VerticalDivider(modifier = Modifier.height(60.dp), color = GSDivider)
                Column(Modifier.weight(1f)) {
                    Text("Stagiaires", color = GSTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text("—", color = GSTeal, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                    Text("suivi actif", color = GSTextSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(17.dp))
        SectionTitle("Accès rapide")
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickAction("Présence", Icons.Filled.EventAvailable, onOpenAttendance)
            QuickAction("Annonce", Icons.Filled.Campaign, onOpenAnnounce)
            QuickAction("Mes cours", Icons.Filled.BarChart, onOpenCourses)
        }

        Spacer(Modifier.height(18.dp))
        SectionTitle("Cours du jour")
        Spacer(Modifier.height(9.dp))
        if (data.courses.isEmpty()) {
            Text("Aucun cours assigné pour le moment.", color = GSTextSecondary)
        } else {
            data.courses.forEach { course ->
                LectureRow(course)
                Spacer(Modifier.height(8.dp))
            }
        }

        if (data.pendingJustifications > 0) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GSWarning.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GSWarning.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenAlerts),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(50), color = GSWarning.copy(alpha = 0.15f)) {
                        Text("${data.pendingJustifications}", color = GSWarning, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.width(9.dp))
                    Text("Justification(s) à examiner.", color = GSTextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LectureRow(course: TeacherCourse) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(com.gstech.student.ui.theme.GSSurfaceTint)
            .border(1.dp, GSDivider, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(10.dp), color = GSSurface, border = androidx.compose.foundation.BorderStroke(1.dp, GSDivider)) {
            Text(course.schedule.ifBlank { "—" }, color = GSBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(course.name, fontWeight = FontWeight.SemiBold, color = GSTextPrimary, fontSize = 13.sp)
            Text("${course.groupName ?: "Groupe —"} • ${course.code} • ${course.room}", color = GSTextSecondary, fontSize = 11.sp)
        }
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
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(GSBluePrimary.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = GSBluePrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, fontSize = 10.sp, color = GSTextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
