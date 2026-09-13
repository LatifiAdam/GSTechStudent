package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.CreneauDto
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import com.gstech.student.util.UiState

private val teacherDays = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")
private val teacherTimes = listOf("08:00", "10:30", "13:00", "15:30")
private val teacherEnds = listOf("10:30", "13:00", "15:30", "18:00")

@Composable
fun TeacherTimetableScreen(
    container: AppContainer,
    onOpen: (String, String) -> Unit,
) {
    var state by remember { mutableStateOf<UiState<List<CreneauDto>>>(UiState.Loading) }
    var retry by remember { mutableIntStateOf(0) }

    LaunchedEffect(retry) {
        state = try {
            val teacherId = container.tokenManager.userIdNow() ?: error("No signed-in teacher.")
            UiState.Success(container.scheduleRepository.getTeacherSchedule(teacherId))
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load timetable.")
        }
    }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Text(
            "My timetable",
            style = MaterialTheme.typography.headlineMedium,
            color = GSTextPrimary,
            modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 8.dp),
        )

        when (val current = state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(current.message) {
                state = UiState.Loading
                retry++
            }
            is UiState.Success -> TimetableGrid(current.data, onOpen)
        }
    }
}

@Composable
private fun TimetableGrid(
    slots: List<CreneauDto>,
    onOpen: (String, String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val slotHeight = 112.dp
    val dayWidth = 190.dp

    if (slots.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scheduled classes.", color = GSTextSecondary)
        }
        return
    }

    Box(Modifier.fillMaxWidth().horizontalScroll(scrollState)) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(74.dp))
                teacherDays.forEach { day ->
                    Box(Modifier.width(dayWidth).height(44.dp), contentAlignment = Alignment.Center) {
                        Text(day.replaceFirstChar { it.uppercase() }, fontSize = 13.sp, color = GSTextSecondary)
                    }
                }
            }

            teacherTimes.forEachIndexed { index, time ->
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.width(74.dp).height(slotHeight), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(time, fontSize = 12.sp, color = GSTextSecondary, modifier = Modifier.padding(top = 14.dp))
                        Text(teacherEnds[index], fontSize = 11.sp, color = GSTextSecondary)
                    }

                    teacherDays.forEach { day ->
                        val slot = slots.firstOrNull {
                            it.jourSemaine.equals(day, ignoreCase = true) && it.heureDebut.take(5) == time
                        }

                        if (slot == null) {
                            Spacer(Modifier.width(dayWidth).height(slotHeight))
                        } else {
                            val courseName = slot.cours?.nomCours
                                ?: slot.affectation?.cours?.nomCours
                                ?: "Course"
                            val className = slot.affectation?.classe?.nomClasse ?: "Class"
                            val room = slot.salle?.takeIf { it.isNotBlank() } ?: "—"

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFD7E3EF),
                                modifier = Modifier.width(dayWidth).height(slotHeight).padding(5.dp),
                                onClick = { onOpen(slot.idCreneau, courseName) },
                            ) {
                                Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) {
                                    Text(
                                        courseName,
                                        fontSize = 14.sp,
                                        lineHeight = 17.sp,
                                        color = Color(0xFF07263B),
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(className, fontSize = 12.sp, color = Color(0xFF5E6F7E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(room, fontSize = 12.sp, color = Color(0xFF5E6F7E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
