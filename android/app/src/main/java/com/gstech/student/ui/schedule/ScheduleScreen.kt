package com.gstech.student.ui.schedule

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
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
import com.gstech.student.model.ClassSession
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun ScheduleScreen(container: AppContainer, onOpenCourse: (id: String, name: String) -> Unit) {
    val viewModel = remember { ScheduleViewModel(container) }
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Schedule  •  ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM . yyyy"))}", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = GSTextPrimary)
        }

        Spacer(Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(viewModel.weekDays) { day ->
                val selected = day == viewModel.selectedDay
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) GSBluePrimary else GSSurface)
                        .clickable { viewModel.selectDay(day) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(day.shortLabel, color = if (selected) Color.White else GSTextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(day.dayNumber, color = if (selected) Color.White else GSTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> {
                val sessions = viewModel.sessionsFor(viewModel.selectedDay)
                if (sessions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No classes scheduled for this day.", color = GSTextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(sessions) { session ->
                            ScheduleCard(session) { onOpenCourse(session.courseId, session.courseName) }
                        }
                    }
                }
            }
        }
    }
}

private val stripeColors = listOf(GSBluePrimary, GSTeal, Color(0xFF7FE0DB))

@Composable
private fun ScheduleCard(session: ClassSession, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GSSurface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(stripeColors[session.id.hashCode().mod(stripeColors.size)]),
            )
            Column(Modifier.padding(16.dp).weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${session.startTime} - ${session.endTime}", color = GSBluePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Surface(shape = RoundedCornerShape(50), color = GSBackground) {
                        Text(session.room, fontSize = 11.sp, color = GSTextSecondary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(session.courseName, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                session.teacherName?.let {
                    Text(it, color = GSTextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}
