package com.gstech.student.ui.gestionnaire

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun GestionnaireClassesScreen(container: AppContainer) {
    val repo = container.adminManagementRepository
    val scope = rememberCoroutineScope()
    var classes by remember { mutableStateOf<List<ClassDto>>(emptyList()) }
    var students by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var selected by remember { mutableStateOf<ClassDto?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var showCreate by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            runCatching {
                val loadedClasses = repo.classes()
                classes = loadedClasses.map { c ->
                    runCatching { c.copy(etudiants = repo.students(c.idClasse)) }.getOrDefault(c)
                }
                students = repo.studentsUnassigned().filter { it.idClasse == null }
                selected?.let { current ->
                    classes.firstOrNull { it.idClasse == current.idClasse }?.let { selected = it }
                }
            }.onFailure { error = userFriendlyErrorMessage(it) }.also { loading = false }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().background(GSBackground).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Groupes", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            Button(onClick = { showCreate = true }) { Text("Nouveau groupe") }
        }
        Spacer(Modifier.height(12.dp))
        error?.let { Text(it, color = GSDanger) }
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (selected == null) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(classes, key = { it.idClasse }) { c ->
                    GSCard(Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            runCatching {
                                val freshStudents = repo.students(c.idClasse)
                                selected = c.copy(etudiants = freshStudents)
                                students = repo.studentsUnassigned().filter { it.idClasse == null }
                            }.onFailure { error = userFriendlyErrorMessage(it) }
                        }
                    }) {
                        Text(c.nomClasse, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Text("${c.etudiants?.size ?: 0} stagiaire(s)", color = GSTextSecondary)
                        c.description?.takeIf { it.isNotBlank() }?.let { Text(it, color = GSTextSecondary) }
                    }
                }
            }
        } else {
            TextButton(onClick = { selected = null }) { Text("← All classes") }
            Text(selected!!.nomClasse, style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
            Spacer(Modifier.height(10.dp))
            Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) { Text("Ajouter un stagiaire à ce groupe") }
            Spacer(Modifier.height(10.dp))
            val current = selected!!.etudiants.orEmpty()
            if (current.isEmpty()) { Text("Aucun Stagiere dans cette classe.", color = GSTextSecondary) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current, key = { it.idUtilisateur }) { st ->
                    GSCard {
                        Text("${st.utilisateur?.prenom.orEmpty()} ${st.utilisateur?.nom.orEmpty()}".trim(), style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Text(st.numeroEtudiant ?: st.idUtilisateur, color = GSTextSecondary)
                    }
                }
            }
        }
    }

    if (showAdd && selected != null) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Choisir un stagiaire") },
            text = {
                if (students.isEmpty()) {
                    Text("Aucun stagiaire sans groupe dans votre établissement.", color = GSTextSecondary)
                } else LazyColumn {
                    items(students, key = { it.idUtilisateur }) { st ->
                        DropdownMenuItem(
                            text = { Text("${st.prenom} ${st.nom}") },
                            onClick = {
                                scope.launch {
                                    val classId = selected!!.idClasse
                                    runCatching {
                                        repo.assignStudent(classId, st.idUtilisateur)
                                        val freshStudents = repo.students(classId)
                                        val freshClasses = repo.classes().map { c ->
                                            if (c.idClasse == classId) c.copy(etudiants = freshStudents) else c
                                        }
                                        classes = freshClasses
                                        selected = freshClasses.firstOrNull { it.idClasse == classId }
                                        students = repo.studentsUnassigned().filter { it.idClasse == null }
                                    }.onFailure { error = userFriendlyErrorMessage(it) }
                                }
                                showAdd = false
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAdd = false }) { Text("Close") } }
        )
    }
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Nouveau groupe") },
            text = {
                Column {
                    OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Nom du groupe") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = groupDescription, onValueChange = { groupDescription = it }, label = { Text("Description") }, minLines = 2)
                }
            },
            confirmButton = {
                TextButton(enabled = groupName.isNotBlank(), onClick = {
                    scope.launch {
                        runCatching { repo.createClass(groupName.trim(), groupDescription.trim().ifBlank { null }) }
                            .onFailure { error = userFriendlyErrorMessage(it) }
                            .onSuccess {
                                groupName = ""
                                groupDescription = ""
                                showCreate = false
                                load()
                            }
                    }
                }) { Text("Créer") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Annuler") } }
        )
    }

}
