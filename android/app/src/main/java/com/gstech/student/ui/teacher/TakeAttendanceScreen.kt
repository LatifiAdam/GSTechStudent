package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.gstech.student.model.AttendanceStatus
import com.gstech.student.model.RosterStudent
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun TakeAttendanceScreen(container: AppContainer, creneauId: String, courseName: String, onDone: () -> Unit) {
    val viewModel = remember(creneauId) { TakeAttendanceViewModel(container, creneauId) }
    val state by viewModel.state.collectAsState()
    val submitted by viewModel.submitted.collectAsState()

    LaunchedEffect(submitted) { if (submitted) onDone() }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
            Text("Take Attendance", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.openCall() }
            is UiState.Success -> {
                val (present, absent, late) = viewModel.counts(s.data.roster)
                Column(Modifier.padding(20.dp, 12.dp)) {
                    Text(courseName, style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
                    Text("Session opened just now", color = GSTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Mark attendance below", color = GSTextSecondary, fontSize = 13.sp)
                        TextButton(onClick = { viewModel.markAllPresent() }) { Text("Mark All Present", color = GSBluePrimary, fontWeight = FontWeight.SemiBold) }
                    }
                }

                if (s.data.roster.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucun Stagiere inscrit dans ce module pour le moment.", color = GSTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(s.data.roster, key = { it.id }) { student ->
                            RosterRow(student) { status -> viewModel.setStatus(student.id, status) }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }

                Column(Modifier.padding(20.dp)) {
                    Button(
                        onClick = { viewModel.submit() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                        enabled = s.data.roster.isNotEmpty() && !s.data.validated,
                    ) {
                        Text(
                            if (s.data.validated) "Attendance Already Submitted" else "Submit Attendance",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(50), color = GSTextPrimary, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("$present Present", color = GSSuccess, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(10.dp))
                            Text("$absent Absent", color = GSDanger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(10.dp))
                            Text("$late Late", color = GSWarning, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RosterRow(student: RosterStudent, onSetStatus: (AttendanceStatus) -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = GSSurface, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(GSDivider))
            Spacer(Modifier.width(12.dp))
            Text(student.name, fontWeight = FontWeight.SemiBold, color = GSTextPrimary, modifier = Modifier.weight(1f))
            StatusToggle("P", GSSuccess, student.status == AttendanceStatus.PRESENT) { onSetStatus(AttendanceStatus.PRESENT) }
            Spacer(Modifier.width(6.dp))
            StatusToggle("A", GSDanger, student.status == AttendanceStatus.ABSENT) { onSetStatus(AttendanceStatus.ABSENT) }
            Spacer(Modifier.width(6.dp))
            StatusToggle("L", GSWarning, student.status == AttendanceStatus.LATE) { onSetStatus(AttendanceStatus.LATE) }
        }
    }
}

@Composable
private fun StatusToggle(letter: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color else GSBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(letter, color = if (selected) Color.White else GSTextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
