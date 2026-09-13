package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun TeacherAttendanceScreen(container: AppContainer, onPickCourse: (id: String, name: String) -> Unit) {
    val viewModel = remember { MyCoursesViewModel(container) }
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Text(
            "Take Attendance",
            style = MaterialTheme.typography.headlineMedium,
            color = GSTextPrimary,
            modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp),
        )
        Text(
            "Choose a course to open today's session",
            color = GSTextSecondary,
            modifier = Modifier.padding(20.dp, 4.dp, 20.dp, 12.dp),
        )

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(s.data, key = { it.id }) { course ->
                    GSCard(modifier = Modifier.clickable { onPickCourse(course.id, course.name) }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(course.name, fontWeight = FontWeight.SemiBold, color = GSTextPrimary)
                                Text("${course.code} • ${course.room}", color = GSTextSecondary, fontSize = 13.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = GSTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
