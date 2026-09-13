package com.gstech.student.ui.announcements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.AnnouncementDto
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*

@Composable
fun AnnouncementsScreen(container: AppContainer) {
    var filter by remember { mutableStateOf("All") }
    var items by remember { mutableStateOf<List<AnnouncementDto>>(emptyList()) }
    LaunchedEffect(Unit) { runCatching { container.courseRepository.getAnnouncementsRaw() }.onSuccess { items = it } }
    Column(Modifier.fillMaxSize().background(GSBackground).padding(20.dp)) {
        Text("Announcements", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Formateur", "Admin").forEach { f ->
                FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) })
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items.filter { a ->
                filter == "All" || (filter == "Admin" && a.typeAnnonce == "generale") || (filter == "Formateur" && a.typeAnnonce != "generale")
            }) { a ->
                GSCard {
                    Text(a.titre, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                    Text(a.contenu, color = GSTextSecondary)
                    Text(if (a.typeAnnonce == "generale") "Admin" else "Formateur", color = GSBluePrimary)
                }
            }
        }
    }
}
