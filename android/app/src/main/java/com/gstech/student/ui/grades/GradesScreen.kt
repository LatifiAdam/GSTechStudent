package com.gstech.student.ui.grades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*

@Composable
fun GradesScreen(container: AppContainer) {
    val local = container.localAcademicRepository
    var course by remember { mutableStateOf<String?>(null) }

    val studentId by produceState<String?>(initialValue = null) {
        value = container.tokenManager.userIdNow()
    }

    val grades = remember(studentId) {
        studentId?.let { local.grades(studentId = it) } ?: emptyList()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .padding(20.dp)
    ) {
        Text(
            "Grades",
            style = MaterialTheme.typography.headlineMedium,
            color = GSTextPrimary
        )
        Spacer(Modifier.height(10.dp))

        val courses = grades.map { it.courseId }.distinct()
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(course ?: "All courses")
            }
            DropdownMenu(expanded, { expanded = false }) {
                DropdownMenuItem({ Text("All courses") }, { course = null; expanded = false })
                courses.forEach { c ->
                    DropdownMenuItem({ Text(c) }, { course = c; expanded = false })
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(grades.filter { course == null || it.courseId == course }) { g ->
                GSCard {
                    Text(
                        "Course: ${g.courseId}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary
                    )
                    Text("Grade 1: ${g.values.getOrNull(0) ?: 0}", color = GSTextSecondary)
                    Text("Grade 2: ${g.values.getOrNull(1) ?: 0}", color = GSTextSecondary)
                    Text("Grade 3: ${g.values.getOrNull(2) ?: 0}", color = GSTextSecondary)
                }
            }
        }
    }
}