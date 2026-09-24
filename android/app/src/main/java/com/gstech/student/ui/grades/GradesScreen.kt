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
import com.gstech.student.data.remote.dto.StudentGradeDto
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*

@Composable
fun GradesScreen(container: AppContainer) {
    var grades by remember { mutableStateOf<List<StudentGradeDto>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        runCatching { container.gradingRepository.getMyGrades() }
            .onSuccess { grades = it }
            .onFailure { error = it.message ?: "Impossible de charger les notes." }
    }

    Column(Modifier.fillMaxSize().background(GSBackground).padding(20.dp)) {
        Text("Grades", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Moyenne = (somme des notes) / nombre de notes", color = GSTextSecondary)
        Spacer(Modifier.height(12.dp))
        when {
            error != null -> ErrorState(error!!) {
                error = null
                grades = null
                reloadKey++
            }
            grades == null -> LoadingState()
            grades!!.isEmpty() -> Text("Aucune note enregistrée.", color = GSTextSecondary)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(grades!!, key = { it.idNote }) { g ->
                    val notes = listOfNotNull(g.note1, g.note2, g.note3)
                    val grade = notes.takeIf { it.isNotEmpty() }?.average()
                    GSCard {
                        Text(g.nomCours ?: g.idCours ?: "Cours", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Text("Groupe : ${g.nomClasse ?: "—"}", color = GSTextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text("Notes : ${if (notes.isEmpty()) "—" else notes.joinToString(" • ") { String.format("%.2f", it) }}", color = GSTextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(if (grade == null) "Note finale : —" else "Note finale : ${String.format("%.2f", grade)}", style = MaterialTheme.typography.titleSmall, color = GSBluePrimary)
                    }
                }
            }
        }
    }
}
