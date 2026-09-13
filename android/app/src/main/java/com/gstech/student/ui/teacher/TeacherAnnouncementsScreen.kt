package com.gstech.student.ui.teacher

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
import com.gstech.student.data.repository.LocalAcademicRepository
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TeacherAnnouncementsScreen(container: AppContainer) {
    val repo = container.adminManagementRepository
    val local = container.localAcademicRepository
    val scope = rememberCoroutineScope()

    var classes by remember { mutableStateOf<List<ClassDto>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<ClassDto?>(null) }
    var historyClass by remember { mutableStateOf<ClassDto?>(null) }
    var announcements by remember { mutableStateOf<List<LocalAcademicRepository.LocalAnnouncement>>(emptyList()) }

    val userId by produceState<String?>(initialValue = null) {
        value = container.tokenManager.userIdNow()
    }

    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        runCatching {
            val a = repo.assignments().filter { it.idFormateur == id }
            classes = repo.classes().filter { c -> a.any { it.idClasse == c.idClasse } }
        }
    }

    LaunchedEffect(userId, historyClass) {
        val id = userId ?: return@LaunchedEffect
        announcements = local.announcements(authorId = id, classId = historyClass?.idClasse)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .padding(20.dp)
    ) {
        Text("Announcements", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Body") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selected?.nomClasse ?: "To class")
            }
            DropdownMenu(expanded, { expanded = false }) {
                classes.forEach { c ->
                    DropdownMenuItem({ Text(c.nomClasse) }, { selected = c; expanded = false })
                }
            }
        }

        Button(
            onClick = {
                val id = userId ?: return@Button
                val target = selected ?: return@Button
                if (title.isNotBlank() && body.isNotBlank()) {
                    scope.launch {
                        local.addAnnouncement(id, target.idClasse, title, body)
                        title = ""
                        body = ""
                        announcements = local.announcements(authorId = id, classId = historyClass?.idClasse)
                    }
                }
            },
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Post announcement")
        }

        Spacer(Modifier.height(16.dp))
        Text("Announcement history", style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)

        var hx by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { hx = true }) {
                Text(historyClass?.nomClasse ?: "All classes")
            }
            DropdownMenu(hx, { hx = false }) {
                DropdownMenuItem({ Text("All classes") }, { historyClass = null; hx = false })
                classes.forEach { cl ->
                    DropdownMenuItem({ Text(cl.nomClasse) }, { historyClass = cl; hx = false })
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(announcements) { a ->
                GSCard {
                    Text(a.title, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                    Text(a.body, color = GSTextSecondary)
                    Text(
                        classes.firstOrNull { it.idClasse == a.classId }?.nomClasse ?: "Class",
                        color = GSBluePrimary
                    )
                }
            }
        }
    }
}