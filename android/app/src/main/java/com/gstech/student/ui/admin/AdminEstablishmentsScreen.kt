package com.gstech.student.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
fun AdminEstablishmentsScreen(container: AppContainer) {
    val repo = container.establishmentsRepository
    val usersRepo = container.adminRepository
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<UiState<List<EstablishmentDto>>>(UiState.Loading) }
    var directors by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var gestionnaires by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var createName by remember { mutableStateOf("") }
    var createRegion by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<EstablishmentDto?>(null) }
    var mode by remember { mutableStateOf("") }
    var currentRole by remember { mutableStateOf<Role?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var showAllAssignments by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentRole = container.authRepository.currentRole()
    }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            val result = safeCall {
                val role = container.authRepository.currentRole()
                val ds = if (role == Role.DF || role == Role.SCQ) usersRepo.getUsersDto(Role.DIRECTEUR) else emptyList()
                val gs = if (role == Role.DF || role == Role.SRIO) usersRepo.getUsersDto(Role.GESTIONNAIRE) else emptyList()
                Triple(repo.all(), ds, gs)
            }
            when (result) {
                is UiState.Success -> {
                    state = UiState.Success(result.data.first)
                    directors = result.data.second
                    gestionnaires = result.data.third
                }
                is UiState.Error -> state = result
                UiState.Loading -> Unit
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    val regionalOnly = currentRole == Role.SRIO || currentRole == Role.SCQ
    val canCreate = currentRole == Role.DF

    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(18.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (regionalOnly) "EFP de votre région" else "Gestion des EFP",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    when (currentRole) {
                        Role.SRIO -> "Affectez les Gestionnaires aux établissements."
                        Role.SCQ -> "Affectez les Directeurs aux établissements."
                        else -> "Visualisez les établissements et gérez les affectations."
                    },
                    color = GSTextSecondary,
                    fontSize = 13.sp
                )
            }
            if (canCreate) {
                FilledTonalButton(
                    onClick = { showCreate = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
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
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(600, easing = FastOutSlowInEasing),
                    label = "efpProgress"
                )

                GSCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GSBluePrimary.copy(alpha = .10f)
                        ) {
                            Icon(
                                Icons.Default.Public,
                                null,
                                tint = GSBluePrimary,
                                modifier = Modifier.padding(10.dp).size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("$total établissements", fontWeight = FontWeight.Bold, color = GSTextPrimary)
                            Text(
                                "$assigned avec affectation",
                                color = GSTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = GSBluePrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(7.dp),
                        trackColor = GSDivider
                    )
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    itemsIndexed(s.data, key = { _, e -> e.idEtablissement }) { index, e ->
                        var visible by remember(e.idEtablissement) { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300, delayMillis = index.coerceAtMost(8) * 45)) + expandVertically(tween(320, delayMillis = index.coerceAtMost(8) * 45))
                        ) {
                            val scale by animateFloatAsState(
                                targetValue = if (selected?.idEtablissement == e.idEtablissement) 1.01f else 1f,
                                animationSpec = tween(220),
                                label = "efpScale"
                            )
                            GSCard(
                                modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = GSBluePrimary.copy(alpha = .09f)
                                    ) {
                                        Icon(Icons.Default.Business, null, tint = GSBluePrimary, modifier = Modifier.padding(9.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(e.nomEtablissement, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Région : ${RegionNames.display(e.region)}", color = GSTextSecondary, fontSize = 12.sp)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = GSDivider)
                                Spacer(Modifier.height(10.dp))

                                when (currentRole) {
                                    Role.SRIO -> {
                                        AssignmentInfo(
                                            icon = Icons.Default.Groups,
                                            label = "Gestionnaire assigné",
                                            value = e.gestionnaires.joinToString(", ") { "${it.prenom} ${it.nom}" }.ifBlank { "Aucun gestionnaire" }
                                        )
                                    }
                                    Role.SCQ -> {
                                        AssignmentInfo(
                                            icon = Icons.Default.Person,
                                            label = "Directeur assigné",
                                            value = e.directeur?.let { "${it.prenom} ${it.nom}" } ?: "Aucun directeur"
                                        )
                                    }
                                    else -> {
                                        AssignmentInfo(Icons.Default.Person, "Directeur", e.directeur?.let { "${it.prenom} ${it.nom}" } ?: "Non assigné")
                                        Spacer(Modifier.height(5.dp))
                                        AssignmentInfo(Icons.Default.Groups, "Gestionnaires", e.gestionnaires.joinToString(", ") { "${it.prenom} ${it.nom}" }.ifBlank { "Aucun" })
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    if (currentRole == Role.SRIO) {
                                        TextButton(onClick = { selected = e; mode = "gestionnaire"; actionError = null }) { Text("Affecter gestionnaire") }
                                    }
                                    if (currentRole == Role.SCQ) {
                                        TextButton(onClick = { selected = e; mode = "director"; actionError = null }) { Text("Affecter directeur") }
                                    }
                                    if (currentRole == Role.DF) {
                                        TextButton(onClick = { showAllAssignments = e.idEtablissement }) { Text("Affectations") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    actionError?.let { message ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            title = { Text("Erreur") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { actionError = null }) { Text("OK") } }
        )
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Créer un établissement") },
            text = {
                Column {
                    OutlinedTextField(value = createName, onValueChange = { createName = it }, label = { Text("Nom") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = createRegion, onValueChange = { createRegion = it }, label = { Text("Région") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = createName.isNotBlank() && createRegion.isNotBlank(),
                    onClick = {
                        scope.launch {
                            runCatching { repo.create(createName.trim(), createRegion.trim()) }
                                .onSuccess {
                                    createName = ""
                                    createRegion = ""
                                    showCreate = false
                                    reload()
                                }
                                .onFailure { actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Création impossible." }
                        }
                    }
                ) { Text("Créer") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Annuler") } }
        )
    }

    selected?.let { e ->
        val options = if (mode == "director") directors else gestionnaires
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(if (mode == "director") "Affecter un Directeur" else "Affecter un Gestionnaire") },
            text = {
                Column {
                    if (options.isEmpty()) Text("Aucun utilisateur disponible.", color = GSTextSecondary)
                    options.forEach { u ->
                        TextButton(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        if (mode == "director") repo.assignDirector(e.idEtablissement, u.idUtilisateur)
                                        else repo.addGestionnaire(e.idEtablissement, u.idUtilisateur)
                                    }.onSuccess {
                                        selected = null
                                        reload()
                                    }.onFailure {
                                        actionError = userFriendlyErrorMessage(it) ?: it.message ?: "Affectation impossible."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("${u.prenom} ${u.nom}") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Annuler") } }
        )
    }

    showAllAssignments?.let { id ->
        val establishment = (state as? UiState.Success)?.data?.firstOrNull { it.idEtablissement == id }
        if (establishment != null) {
            AlertDialog(
                onDismissRequest = { showAllAssignments = null },
                title = { Text(establishment.nomEtablissement) },
                text = {
                    Column {
                        AssignmentInfo(Icons.Default.Person, "Directeur", establishment.directeur?.let { "${it.prenom} ${it.nom}" } ?: "Non assigné")
                        Spacer(Modifier.height(8.dp))
                        AssignmentInfo(Icons.Default.Groups, "Gestionnaires", establishment.gestionnaires.joinToString(", ") { "${it.prenom} ${it.nom}" }.ifBlank { "Aucun" })
                        Spacer(Modifier.height(8.dp))
                        Text("Région : ${RegionNames.display(establishment.region)}", color = GSTextSecondary)
                    }
                },
                confirmButton = { TextButton(onClick = { showAllAssignments = null }) { Text("Fermer") } }
            )
        }
    }
}

@Composable
private fun AssignmentInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = GSTextSecondary)
            Text(value, fontSize = 14.sp, color = GSTextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}
