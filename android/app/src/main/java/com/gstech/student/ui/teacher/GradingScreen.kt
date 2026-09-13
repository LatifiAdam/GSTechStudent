package com.gstech.student.ui.teacher

import android.R.attr.label
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.StudentUserDto
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GradingScreen(container: AppContainer) {
    val repo = container.adminManagementRepository;
    val local = container.localAcademicRepository;
    val scope = rememberCoroutineScope()
    var classes by remember { mutableStateOf<List<ClassDto>>(emptyList()) };
    var selected by remember { mutableStateOf<ClassDto?>(null) };
    var students by remember { mutableStateOf<List<StudentUserDto>>(emptyList()) };
    var courseId by remember { mutableStateOf("") };
    var courseName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        runCatching {
            val id = container.tokenManager.userIdNow()!!;
            val a = repo.assignments().filter { it.idFormateur == id };
            classes = repo.classes().filter { c -> a.any { it.idClasse == c.idClasse } };
            if (a.isNotEmpty()) {
                courseId = a.first().idCours; courseName = a.first().cours?.nomCours ?: "Course"
            }
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .padding(20.dp)
    ) {
        Text("Grading", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary);
        Spacer(Modifier.height(10.dp));

        if (selected == null) LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(classes) { c ->
                GSCard {
                    Text(c.nomClasse, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary);
                    Text("Select class", color = GSTextSecondary);
                    TextButton(onClick = { selected = c; scope.launch { students = repo.students(c.idClasse) } }) {
                        Text("Open")
                    }
                }
            }
        }
        else {
            TextButton(onClick = { selected = null }) { Text("← Classes") };
            Text(selected!!.nomClasse, style = MaterialTheme.typography.titleLarge, color = GSTextPrimary);
            Text("Course: $courseName", color = GSTextSecondary);
            Spacer(Modifier.height(8.dp));
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(students) { s ->
                    GradeEditor(s, courseId, local)
                }
            }
        }
    }
}

@Composable
private fun GradeEditor(
    student: StudentUserDto,
    courseId: String,
    local: com.gstech.student.data.repository.LocalAcademicRepository
) {
    val existing = remember(student.idUtilisateur, courseId) {
        local.grades(student.idUtilisateur, courseId).firstOrNull()?.values ?: listOf(0.0, 0.0, 0.0)
    }
    var a by remember { mutableStateOf(existing.getOrNull(0)?.toString().orEmpty()) }
    var b by remember { mutableStateOf(existing.getOrNull(1)?.toString().orEmpty()) }
    var c by remember { mutableStateOf(existing.getOrNull(2)?.toString().orEmpty()) }
    val u = student.utilisateur

    GSCard {
        Text(
            "${u?.prenom.orEmpty()} ${u?.nom.orEmpty()}",
            style = MaterialTheme.typography.titleMedium,
            color = GSTextPrimary
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(a, b, c).forEachIndexed { i, v ->
                OutlinedTextField(
                    value = v,
                    onValueChange = { nv ->
                        when (i) {
                            0 -> a = nv
                            1 -> b = nv
                            2 -> c = nv
                        }
                    },
                    label = { Text("Grade ${i + 1}") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Button(onClick = {
            local.saveGrades(
                student.idUtilisateur,
                courseId,
                listOf(
                    a.toDoubleOrNull() ?: 0.0,
                    b.toDoubleOrNull() ?: 0.0,
                    c.toDoubleOrNull() ?: 0.0
                )
            )
        }) {
            Text("Save")
        }
    }
}