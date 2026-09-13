package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.StudentUserDto
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSDanger
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.util.UiState
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextFieldDefaults

@Composable
fun AdminClassesScreen(container: AppContainer, readOnly: Boolean = false, allowCreate: Boolean = false) {
    val repository = container.adminManagementRepository
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<UiState<List<ClassDto>>>(UiState.Loading) }
    var students by remember { mutableStateOf<List<StudentUserDto>>(emptyList()) }
    var allStudents by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var selectedClass by remember { mutableStateOf<ClassDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var classToDelete by remember { mutableStateOf<ClassDto?>(null) }
    var editingClass by remember { mutableStateOf<ClassDto?>(null) }
    var className by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            state = try {
                if (!readOnly) allStudents = repository.studentsUnassigned()
                UiState.Success(repository.classes())
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Unable to load classes.")
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

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
            Text(if (readOnly) "Groupes" else "Classes", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            if (!readOnly || allowCreate) {
                Button(onClick = { showCreateDialog = true }) { Text("Nouveau groupe") }
            }
        }

        when (val current = state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(current.message) { reload() }
            is UiState.Success -> LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(current.data, key = { it.idClasse }) { clazz ->
                    GSCard(
                        Modifier
                            .fillMaxWidth()
                            .then(
                                if (readOnly) Modifier.clickable {
                                    scope.launch {
                                        students = repository.students(clazz.idClasse)
                                        selectedClass = clazz
                                    }
                                } else Modifier
                            )
                    ) {
                        Text(clazz.nomClasse, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Text(clazz.description ?: "No description", color = GSTextSecondary)
                        Text(
                            "${clazz.etudiants?.size ?: 0} students • ${clazz.affectations?.size ?: 0} courses",
                            color = GSTextSecondary,
                            fontSize = 12.sp
                        )
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            if (!readOnly) {
                                TextButton(onClick = { editingClass = clazz }) { Text("Edit") }
                            }
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        students = repository.students(clazz.idClasse)
                                        selectedClass = clazz
                                    }
                                }
                            ) { Text(if (readOnly) "Voir les Stagiaires" else "Gérer les Stagieres") }
                            if (!readOnly) {
                                TextButton(onClick = { classToDelete = clazz }) { Text("Delete", color = GSDanger) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (classToDelete != null) {
        val clazz = classToDelete!!
        AlertDialog(
            onDismissRequest = { classToDelete = null },
            title = { Text("Delete class?") },
            text = { Text("Are you sure you want to delete \"${clazz.nomClasse}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    classToDelete = null
                    scope.launch { runCatching { repository.deleteClass(clazz.idClasse) }.onSuccess { reload() } }
                }) { Text("Delete", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { classToDelete = null }) { Text("Cancel") } },
        )
    }

    if ((!readOnly || allowCreate) && showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create class") },
            text = {
                Column {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class name") },
                        singleLine = true,
                    
                        colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        focusedLabelColor = GSBluePrimary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        focusedBorderColor = GSBluePrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        errorBorderColor = GSDanger,
                                        errorLabelColor = GSDanger,
                                        cursorColor = GSBluePrimary,
                                        errorCursorColor = GSDanger,
                                    ))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                    
                        colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        focusedLabelColor = GSBluePrimary,
                                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        focusedBorderColor = GSBluePrimary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        errorBorderColor = GSDanger,
                                        errorLabelColor = GSDanger,
                                        cursorColor = GSBluePrimary,
                                        errorCursorColor = GSDanger,
                                    ))
                }
            },
            confirmButton = {
                TextButton(
                    enabled = className.isNotBlank(),
                    onClick = {
                        scope.launch {
                            repository.createClass(className.trim(), description.trim().ifBlank { null })
                            className = ""
                            description = ""
                            showCreateDialog = false
                            reload()
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }

    if (!readOnly) editingClass?.let { clazz ->
        EditClassDialog(
            clazz = clazz,
            onDismiss = { editingClass = null },
            onSave = { name, description ->
                scope.launch {
                    repository.updateClass(clazz.idClasse, name, description)
                    editingClass = null
                    reload()
                }
            },
        )
    }

    selectedClass?.let { clazz ->
        ClassStudentsDialog(
            clazz = clazz,
            students = students,
            allStudents = allStudents,
            onDismiss = { selectedClass = null },
            onAdd = { studentId ->
                scope.launch {
                    repository.assignStudent(clazz.idClasse, studentId)
                    students = repository.students(clazz.idClasse)
                }
            },
            onRemove = { studentId ->
                scope.launch {
                    repository.removeStudent(clazz.idClasse, studentId)
                    students = repository.students(clazz.idClasse)
                }
            }
        )
    }
}

@Composable
private fun EditClassDialog(
    clazz: ClassDto,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
) {
    var name by remember(clazz) { mutableStateOf(clazz.nomClasse) }
    var description by remember(clazz) { mutableStateOf(clazz.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit class") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Class name") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    focusedLabelColor = GSBluePrimary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    focusedBorderColor = GSBluePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    errorBorderColor = GSDanger,
                                    errorLabelColor = GSDanger,
                                    cursorColor = GSBluePrimary,
                                    errorCursorColor = GSDanger,
                                ))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    focusedLabelColor = GSBluePrimary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    focusedBorderColor = GSBluePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    errorBorderColor = GSDanger,
                                    errorLabelColor = GSDanger,
                                    cursorColor = GSBluePrimary,
                                    errorCursorColor = GSDanger,
                                ))
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name.trim(), description.trim().ifBlank { null }) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ClassStudentsDialog(
    clazz: ClassDto,
    students: List<StudentUserDto>,
    allStudents: List<UserDto>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val available = allStudents.filter { user -> user.idClasse == null && students.none { it.idUtilisateur == user.idUtilisateur } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${clazz.nomClasse} Stagieres") },
        text = {
            Column {
                Text("${students.size} Stagieres", color = GSTextSecondary)
                students.forEach { student ->
                    val name = "${student.utilisateur?.prenom.orEmpty()} ${student.utilisateur?.nom.orEmpty()}"
                        .trim()
                        .ifBlank { student.numeroEtudiant.orEmpty() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = GSTextPrimary)
                        TextButton(onClick = { onRemove(student.idUtilisateur) }) {
                            Text("Remove", color = GSDanger)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { expanded = true }, enabled = available.isNotEmpty()) {
                        Text(if (available.isEmpty()) "No available students" else "Add student")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        available.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.prenom} ${user.nom}") },
                                onClick = {
                                    onAdd(user.idUtilisateur)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
