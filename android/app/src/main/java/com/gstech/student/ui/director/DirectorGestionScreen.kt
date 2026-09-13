package com.gstech.student.ui.director

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.EstablishmentDto
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.model.Role
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch

@Composable
fun DirectorGestionScreen(container: AppContainer) {
    val repo = container.establishmentsRepository
    val users = container.adminRepository
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UiState<EstablishmentDto?>>(UiState.Loading) }
    var allGestionnaires by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            state = safeCall { repo.mine() }
            when (val result = safeCall { users.getUsersDto(Role.GESTIONNAIRE) }) {
                is UiState.Success -> allGestionnaires = result.data
                is UiState.Error -> allGestionnaires = emptyList()
                UiState.Loading -> Unit
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        modifier = Modifier.fillMaxSize().background(GSBackground).padding(20.dp)
    ) {
        when (val s = state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { reload() }
            is UiState.Success -> {
                val establishment = s.data
                if (establishment == null) {
                    Text("Aucun établissement n'est affecté à ce Directeur", color = GSTextSecondary)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gestion", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
                            Text(establishment.nomEtablissement, color = GSTextSecondary)
                        }
                        Button(onClick = { showAdd = true }) { Text("Ajouter") }
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(establishment.gestionnaires, key = { it.idUtilisateur }) { gestionnaire ->
                            GSCard {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            listOf(gestionnaire.prenom, gestionnaire.nom).filterNotNull().joinToString(" "),
                                            color = GSTextPrimary
                                        )
                                        Text(gestionnaire.email ?: "", color = GSTextSecondary)
                                    }
                                    TextButton(onClick = {
                                        scope.launch {
                                            repo.removeGestionnaire(establishment.idEtablissement, gestionnaire.idUtilisateur)
                                            reload()
                                        }
                                    }) { Text("Retirer", color = GSDanger) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        val existingIds = (state as? UiState.Success)?.data?.gestionnaires
            ?.map { it.idUtilisateur }
            .orEmpty()
            .toSet()
        val options = allGestionnaires.filter { it.idUtilisateur !in existingIds }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Affecter un Gestionnaire") },
            text = {
                Column {
                    if (options.isEmpty()) {
                        Text("Aucun Gestionnaire disponible", color = GSTextSecondary)
                    } else {
                        options.forEach { gestionnaire ->
                            TextButton(
                                onClick = {
                                    val establishment = (state as? UiState.Success)?.data
                                    if (establishment != null) {
                                        scope.launch {
                                            repo.addGestionnaire(establishment.idEtablissement, gestionnaire.idUtilisateur)
                                            showAdd = false
                                            reload()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    listOf(gestionnaire.prenom, gestionnaire.nom).filterNotNull().joinToString(" ")
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdd = false }) { Text("Fermer") }
            }
        )
    }
}
