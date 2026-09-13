package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.AffectationDto
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.CourseDto
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSDanger
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import kotlinx.coroutines.launch

@Composable
fun AdminAssignmentsScreen(container: AppContainer) {
    val repository = container.adminManagementRepository
    val scope = rememberCoroutineScope()

    var classes by remember { mutableStateOf<List<ClassDto>>(emptyList()) }
    var teachers by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var courses by remember { mutableStateOf<List<CourseDto>>(emptyList()) }
    var assignments by remember { mutableStateOf<List<AffectationDto>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var classIndex by remember { mutableIntStateOf(-1) }
    var courseIndex by remember { mutableIntStateOf(-1) }
    var teacherIndex by remember { mutableIntStateOf(-1) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            try {
                classes = repository.classes()
                teachers = repository.teachers()
                courses = repository.courses()
                assignments = repository.assignments()
                error = null
            } catch (e: Exception) {
                error = e.message ?: "Unable to load assignments."
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Assignments", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            Button(onClick = { showDialog = true }) { Text("Assign") }
        }

        error?.let {
            Text(it, color = GSDanger, modifier = Modifier.padding(horizontal = 20.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(assignments, key = { it.idAffectation }) { assignment ->
                GSCard {
                    Text(
                        "${assignment.classe?.nomClasse ?: assignment.idClasse} • " +
                            "${assignment.cours?.nomCours ?: assignment.idCours}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary
                    )
                    Text(
                        teacherName(assignment),
                        color = GSTextSecondary,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    repository.deleteAssignment(assignment.idAffectation)
                                    assignments = repository.assignments()
                                } catch (e: Exception) {
                                    error = e.message ?: "Unable to remove assignment."
                                }
                            }
                        }
                    ) {
                        Text("Remove", color = GSDanger)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New assignment") },
            text = {
                Column {
                    SelectionField("Class", classes.map { it.nomClasse }, classIndex) { classIndex = it }
                    Spacer(Modifier.height(8.dp))
                    SelectionField("Course", courses.map { it.nomCours }, courseIndex) { courseIndex = it }
                    Spacer(Modifier.height(8.dp))
                    SelectionField("Formateur", teachers.map { "${it.prenom} ${it.nom}" }, teacherIndex) { teacherIndex = it }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = classIndex >= 0 && courseIndex >= 0 && teacherIndex >= 0,
                    onClick = {
                        scope.launch {
                            try {
                                repository.createAssignment(
                                    classes[classIndex].idClasse,
                                    courses[courseIndex].idCours,
                                    teachers[teacherIndex].idUtilisateur
                                )
                                assignments = repository.assignments()
                                showDialog = false
                                classIndex = -1
                                courseIndex = -1
                                teacherIndex = -1
                            } catch (e: Exception) {
                                error = e.message ?: "Unable to create assignment."
                            }
                        }
                    }
                ) { Text("Assign") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun teacherName(assignment: AffectationDto): String {
    val user = assignment.formateur?.utilisateur
    return "${user?.prenom.orEmpty()} ${user?.nom.orEmpty()}".trim()
        .ifBlank { assignment.idFormateur }
}

@Composable
private fun SelectionField(
    label: String,
    values: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(values.getOrNull(selectedIndex) ?: label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            values.forEachIndexed { index, value ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
