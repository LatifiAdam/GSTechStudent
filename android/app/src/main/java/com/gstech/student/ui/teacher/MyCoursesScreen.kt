package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun MyCoursesScreen(container: AppContainer, onOpenCourse: (id: String, name: String) -> Unit) {
    val viewModel = remember { MyCoursesViewModel(container) }
    val state by viewModel.state.collectAsState()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("My Courses", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No courses assigned yet.", color = GSTextSecondary)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(s.data, key = { it.id }) { course ->
                            CourseCard(course) { onOpenCourse(course.id, course.name) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(course: TeacherCourse, onClick: () -> Unit) {
    GSCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(course.name, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                Text(course.code, color = GSTeal, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(50), color = GSBackground) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(course.schedule.ifBlank { "Schedule not set" }, color = GSTextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Text("Room ${course.room}", color = GSTextSecondary, fontSize = 12.sp)
    }
}
