package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.EstablishmentDto
import com.gstech.student.data.remote.dto.UserDto
import com.gstech.student.model.Role
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.RegionNames
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import com.gstech.student.util.userFriendlyErrorMessage
import kotlinx.coroutines.launch

@Composable
fun AdminEstablishmentsScreen(
    container: AppContainer,
    onOpenUser: (String) -> Unit = {},
) {
    val repo = container.establishmentsRepository
    val usersRepo = container.adminRepository
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<UiState<List<EstablishmentDto>>>(UiState.Loading) }
    var directors by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var gestionnaires by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var currentRole by remember { mutableStateOf<Role?>(null) }
    var selected by remember { mutableStateOf<EstablishmentDto?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var createName by remember { mutableStateOf("") }
    var createRegion by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var showAssignmentDialog by remember { mutableStateOf(false) }
    var assignmentMode by remember { mutableStateOf<AssignmentMode?>(null) }
    var editingName by remember { mutableStateOf("") }
    var showEditName by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRemoveDirectorConfirm by remember { mutableStateOf(false) }
    var directorToRemove by remember { mutableStateOf<EstablishmentDto?>(null) }
    var managerToRemove by remember { mutableStateOf<Pair<EstablishmentDto, String>?>(null) }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            state = safeCall {
                val role = container.authRepository.currentRole()
                currentRole = role
                val ds = if (role == Role.DF || role == Role.SCQ) usersRepo.getUsersDto(Role.DIRECTEUR) else emptyList()
                val gs = if (role == Role.DF || role == Role.SRIO) usersRepo.getUsersDto(Role.GESTIONNAIRE) else emptyList()
                Triple(repo.all(), ds, gs)
            }.let { result ->
                when (result) {
                    is UiState.Success -> {
                        directors = result.data.second
                        gestionnaires = result.data.third
                        UiState.Success(result.data.first)
                    }
                    is UiState.Error -> result
                    UiState.Loading -> UiState.Loading
                }
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    val regionalOnly = currentRole == Role.SRIO || currentRole == Role.SCQ
    val canManageEfp = currentRole == Role.DF || currentRole == Role.SUPER_ADMIN

    Column(
        Modifier.fillMaxSize().background(GSBackground).padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (regionalOnly) "EFP de votre région" else "Gestion des EFP",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    when (currentRole) {
                        Role.SRIO -> "Affectez et consultez les Gestionnaires de votre région."
                        Role.SCQ -> "Affectez et consultez les Directeurs de votre région."
                        else -> "Ouvrez un EFP pour modifier son nom et gérer ses responsables."
                    },
                    color = GSTextSecondary,
                    fontSize = 13.sp,
                )
            }
            if (canManageEfp) {
                FilledTonalButton(onClick = { showCreate = true }, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Business, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Ajouter")
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        when (val s = state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { reload() }
            is UiState.Success -> {
                val total = s.data.size
                val assigned = when (currentRole) {
                    Role.SRIO -> s.data.count { it.gestionnaires.isNotEmpty() }
                    Role.SCQ -> s.data.count { it.directeur != null }
                    else -> s.data.count { it.directeur != null || it.gestionnaires.isNotEmpty() }
                }
                val progress = if (total == 0) 0f else assigned.toFloat() / total
                GSCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = GSBluePrimary.copy(alpha = .10f)) {
                            Icon(Icons.Default.Public, null, tint = GSBluePrimary, modifier = Modifier.padding(10.dp).size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("$total établissements", fontWeight = FontWeight.Bold, color = GSTextPrimary)
                            Text("$assigned avec affectation", color = GSTextSecondary, fontSize = 12.sp)
                        }
                        Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = GSBluePrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp), trackColor = GSDivider)
                }
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                ) {
                    items(s.data, key = { it.idEtablissement }) { e ->
                        GSCard(
                            Modifier.fillMaxWidth().clickable {
                                selected = e
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = GSBluePrimary.copy(alpha = .09f)) {
                                    Icon(Icons.Default.Business, null, tint = GSBluePrimary, modifier = Modifier.padding(9.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(e.nomEtablissement, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary, fontWeight = FontWeight.Bold)
                                    Text("Région : ${RegionNames.display(e.region)}", color = GSTextSecondary, fontSize = 12.sp)
                                }
                                Text("Ouvrir", color = GSBluePrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(10.dp))
                            when (currentRole) {
                                Role.SRIO -> AssignmentInfo(Icons.Default.Groups, "Gestionnaire(s)", e.gestionnaires.joinToString(", ") { "${it.prenom} ${it.nom}" }.ifBlank { "Aucun gestionnaire" })
                                Role.SCQ -> AssignmentInfo(Icons.Default.Person, "Directeur", e.directeur?.let { "${it.prenom} ${it.nom}" } ?: "Aucun directeur")
                                else -> {
                                    AssignmentInfo(Icons.Default.Person, "Directeur", e.directeur?.let { "${it.prenom} ${it.nom}" } ?: "Non assigné")
                                    Spacer(Modifier.height(5.dp))
                                    AssignmentInfo(Icons.Default.Groups, "Gestionnaires", e.gestionnaires.joinToString(", ") { "${it.prenom} ${it.nom}" }.ifBlank { "Aucun" })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { e ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(e.nomEtablissement) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Région : ${RegionNames.display(e.region)}", color = GSTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Directeur", color = GSTextSecondary, fontSize = 12.sp)
                    if (e.directeur != null) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${e.directeur.prenom} ${e.directeur.nom}",
                                color = GSBluePrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f).clickable { onOpenUser(e.directeur.idUtilisateur) },
                            )
                            if (currentRole == Role.DF || currentRole == Role.SCQ) {
                                TextButton(onClick = { directorToRemove = e; showRemoveDirectorConfirm = true }) { Text("Retirer") }
                            }
                        }
                    } else {
                        Text("Aucun directeur", color = GSTextPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Gestionnaires", color = GSTextSecondary, fontSize = 12.sp)
                    if (e.gestionnaires.isEmpty()) {
                        Text("Aucun gestionnaire", color = GSTextPrimary)
                    } else {
                        e.gestionnaires.forEach { g ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${g.prenom} ${g.nom}",
                                    color = GSBluePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f).clickable { onOpenUser(g.idUtilisateur) },
                                )
                                if (currentRole == Role.DF || currentRole == Role.SRIO) {
                                    TextButton(onClick = { managerToRemove = e to g.idUtilisateur }) { Text("Retirer") }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (currentRole == Role.SRIO) {
                        TextButton(onClick = { assignmentMode = AssignmentMode.GESTIONNAIRE; showAssignmentDialog = true }) { Text("Affecter gestionnaire") }
                    }
                    if (currentRole == Role.SCQ) {
                        TextButton(onClick = { assignmentMode = AssignmentMode.DIRECTEUR; showAssignmentDialog = true }) { Text("Affecter directeur") }
                    }
                    if (canManageEfp) {
                        TextButton(onClick = { editingName = e.nomEtablissement; showEditName = true }) { Text("Modifier nom") }
                        TextButton(onClick = { showDeleteConfirm = true }) { Text("Supprimer", color = GSDanger) }
                    }
                    TextButton(onClick = { selected = null }) { Text("Fermer") }
                }
            }
        )
    }

    if (showAssignmentDialog && selected != null && assignmentMode != null) {
        val isDirector = assignmentMode == AssignmentMode.DIRECTEUR
        val choices = if (isDirector) directors else gestionnaires
        AlertDialog(
            onDismissRequest = { showAssignmentDialog = false },
            title = { Text(if (isDirector) "Affecter un Directeur" else "Affecter un Gestionnaire") },
            text = {
                if (choices.isEmpty()) {
                    Text("Aucun compte disponible dans votre périmètre.", color = GSTextSecondary)
                } else {
                    Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                        choices.forEach { u ->
                            TextButton(
                                onClick = {
                                    val selectedId = selected?.idEtablissement ?: return@TextButton
                                    scope.launch {
                                        runCatching {
                                            if (isDirector) repo.assignDirector(selectedId, u.idUtilisateur)
                                            else repo.addGestionnaire(selectedId, u.idUtilisateur)
                                        }.onSuccess {
                                            showAssignmentDialog = false
                                            reload()
                                        }.onFailure {
                                            actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Affectation impossible."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("${u.prenom} ${u.nom}", color = GSTextPrimary) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAssignmentDialog = false }) { Text("Fermer") } },
        )
    }

    if (showEditName && selected != null) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Modifier l’établissement") },
            text = { OutlinedTextField(editingName, { editingName = it }, label = { Text("Nom de l’établissement") }, singleLine = true) },
            confirmButton = {
                TextButton(
                    enabled = editingName.isNotBlank(),
                    onClick = {
                        scope.launch {
                            runCatching { repo.update(selected!!.idEtablissement, editingName.trim()) }
                                .onSuccess { showEditName = false; selected = null; reload() }
                                .onFailure { actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Modification impossible." }
                        }
                    },
                ) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { showEditName = false }) { Text("Annuler") } },
        )
    }

    if (showDeleteConfirm && selected != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cet établissement ?") },
            text = { Text("La suppression est définitive. Si des classes, cours ou stagiaires dépendent encore de cet EFP, la base de données peut refuser la suppression.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { repo.delete(selected!!.idEtablissement) }
                            .onSuccess { showDeleteConfirm = false; selected = null; reload() }
                            .onFailure { actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Suppression impossible." }
                    }
                }) { Text("Supprimer", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") } },
        )
    }

    if (showRemoveDirectorConfirm && directorToRemove != null) {
        AlertDialog(
            onDismissRequest = { showRemoveDirectorConfirm = false },
            title = { Text("Retirer le Directeur ?") },
            confirmButton = {
                TextButton(onClick = {
                    val id = directorToRemove!!.idEtablissement
                    scope.launch {
                        runCatching { repo.removeDirector(id) }
                            .onSuccess { showRemoveDirectorConfirm = false; directorToRemove = null; selected = null; reload() }
                            .onFailure { actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Action impossible." }
                    }
                }) { Text("Retirer", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { showRemoveDirectorConfirm = false }) { Text("Annuler") } },
        )
    }

    managerToRemove?.let { (e, gid) ->
        AlertDialog(
            onDismissRequest = { managerToRemove = null },
            title = { Text("Retirer le Gestionnaire ?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { repo.removeGestionnaire(e.idEtablissement, gid) }
                            .onSuccess { managerToRemove = null; selected = null; reload() }
                            .onFailure { actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Action impossible." }
                    }
                }) { Text("Retirer", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { managerToRemove = null }) { Text("Annuler") } },
        )
    }

    if (showCreate) {
        val regions = listOf(
            "RSK" to "Rabat-Salé-Kénitra", "CS" to "Casablanca-Settat", "TTA" to "Tanger-Tétouan-Al Hoceïma",
            "FM" to "Fès-Meknès", "M" to "Marrakech-Safi", "OR" to "Oriental", "BS" to "Béni Mellal-Khénifra",
            "D" to "Drâa-Tafilalet", "SMD" to "Souss-Massa", "GON" to "Guelmim-Oued Noun",
        )
        var expanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Créer un établissement") },
            text = {
                Column {
                    OutlinedTextField(createName, { createName = it }, label = { Text("Nom") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(regions.firstOrNull { it.first == createRegion }?.second ?: "Sélectionner une région")
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            regions.forEach { (code, name) -> DropdownMenuItem(text = { Text(name) }, onClick = { createRegion = code; expanded = false }) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = createName.isNotBlank() && createRegion.isNotBlank(), onClick = {
                    scope.launch {
                        runCatching { repo.create(createName.trim(), createRegion) }
                            .onSuccess { createName = ""; createRegion = ""; showCreate = false; reload() }
                            .onFailure { actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Création impossible." }
                    }
                }) { Text("Créer") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Annuler") } },
        )
    }

    actionError?.let { message ->
        AlertDialog(onDismissRequest = { actionError = null }, title = { Text("Erreur") }, text = { Text(message) }, confirmButton = { TextButton(onClick = { actionError = null }) { Text("OK") } })
    }
}

private enum class AssignmentMode { DIRECTEUR, GESTIONNAIRE }

@Composable
private fun AssignmentInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = GSTextSecondary)
            Text(value, fontSize = 14.sp, color = GSTextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
