package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AdminCourse
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.components.StatusPill
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun AdminCoursesScreen(container: AppContainer) {
    var state by remember {
        mutableStateOf<UiState<List<AdminCourse>>>(UiState.Loading)
    }

    var showCreate by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()
    var courseToDelete by remember { mutableStateOf<AdminCourse?>(null) }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            state = safeCall {
                container.adminRepository.getCourses()
            }
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    Scaffold(
        containerColor = GSBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showCreate = true
                },
                containerColor = GSBluePrimary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add course",
                    tint = Color.White
                )
            }
        },
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Text(
                "Courses",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary,
                modifier = Modifier.padding(
                    20.dp,
                    20.dp,
                    20.dp,
                    12.dp
                )
            )

            when (val s = state) {

                is UiState.Loading -> {
                    LoadingState()
                }

                is UiState.Error -> {
                    ErrorState(s.message) {
                        reload()
                    }
                }

                is UiState.Success -> {

                    LazyColumn(
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            s.data,
                            key = { it.id }
                        ) { course ->

                            GSCard {

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    Column {

                                        // Full course name
                                        Text(
                                            course.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = GSTextPrimary
                                        )

                                    }

                                    StatusPill(
                                        "ACTIVE",
                                        GSSuccess
                                    )
                                }

                                Spacer(
                                    Modifier.height(8.dp)
                                )

                                Text(
                                    course.teacherName,
                                    color = GSTextSecondary,
                                    fontSize = 13.sp
                                )

                                Spacer(Modifier.height(6.dp))
                                TextButton(onClick = { courseToDelete = course }) { Text("Delete", color = GSDanger) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (courseToDelete != null) {
        val course = courseToDelete!!
        AlertDialog(
            onDismissRequest = { courseToDelete = null },
            title = { Text("Delete course?") },
            text = { Text("Are you sure you want to delete \"${course.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    courseToDelete = null
                    scope.launch {
                        runCatching { container.adminRepository.deleteCourse(course.id) }.onSuccess { reload() }
                    }
                }) { Text("Delete", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { courseToDelete = null }) { Text("Cancel") } },
        )
    }

    if (showCreate) {

        CreateCourseDialog(
            container = container,
            onDismiss = {
                showCreate = false
            },
            onCreated = {
                showCreate = false
                reload()
            },
        )
    }
}

@Composable
private fun CreateCourseDialog(
    container: AppContainer,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    var error by remember { mutableStateOf<String?>(null) }
    var teachers by remember { mutableStateOf<List<com.gstech.student.data.remote.dto.UserDto>>(emptyList()) }
    var selectedTeacher by remember { mutableStateOf<com.gstech.student.data.remote.dto.UserDto?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { teachers = runCatching { container.adminManagementRepository.teachers() }.getOrDefault(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("New course")
        },

        text = {

            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Course name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                
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
                var expandedTeacher by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expandedTeacher = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedTeacher?.let { "${it.prenom} ${it.nom}" } ?: "Select teacher")
                    }
                    DropdownMenu(expanded = expandedTeacher, onDismissRequest = { expandedTeacher = false }) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(text = { Text("${teacher.prenom} ${teacher.nom} • ${teacher.idUtilisateur}") }, onClick = { selectedTeacher = teacher; expandedTeacher = false })
                        }
                    }
                }

                error?.let {
                    Text(
                        it,
                        color = GSDanger,
                        fontSize = 12.sp
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    scope.launch {

                        runCatching {

                            container.adminRepository.createCourse(name)

                        }.onSuccess {

                            onCreated()

                        }.onFailure {

                            error =
                                userFriendlyErrorMessage(it)
                        }
                    }
                }
            ) {
                Text("Create")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },
    )
}
