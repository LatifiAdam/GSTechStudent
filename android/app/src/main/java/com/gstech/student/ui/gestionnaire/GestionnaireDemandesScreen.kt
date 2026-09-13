package com.gstech.student.ui.gestionnaire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.DocumentRequestDto
import com.gstech.student.data.remote.dto.RefuseJustificationRequest
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun GestionnaireDemandesScreen(container: AppContainer) {
    var requests by remember { mutableStateOf<List<DocumentRequestDto>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var processingId by remember { mutableStateOf<String?>(null) }
    var refuseTarget by remember { mutableStateOf<DocumentRequestDto?>(null) }
    var refuseReason by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            runCatching { container.documentRequestsApi.list("en_attente") }
                .onSuccess { requests = it; message = null }
                .onFailure { message = userFriendlyErrorMessage(it) }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
            .padding(20.dp)
    ) {
        Text(
            "Traiter une demande",
            style = MaterialTheme.typography.headlineMedium,
            color = GSTextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Demandes de documents envoyées par les Stagiaires de votre EFP.",
            color = GSTextSecondary
        )
        Spacer(Modifier.height(16.dp))

        message?.let {
            Text(it, color = GSDanger)
            Spacer(Modifier.height(8.dp))
        }

        if (requests.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GSBluePrimary.copy(alpha = 0.08f)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Description, null, tint = GSBluePrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Aucune demande en attente.", color = GSTextPrimary)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(requests, key = { it.idDemande }) { req ->
                val studentName = listOfNotNull(
                    req.etudiant?.utilisateur?.prenom,
                    req.etudiant?.utilisateur?.nom
                ).joinToString(" ").ifBlank { "Stagiaire" }
                val documentName = req.document?.nomDocument
                    ?: req.typeDocument
                    ?: "Document"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GSSurface)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(studentName, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(documentName, color = GSTextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Demandée le ${req.dateDemande.take(10)}", color = GSTextSecondary)
                        Spacer(Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                enabled = processingId != req.idDemande,
                                onClick = { refuseTarget = req },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Close, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Refuser")
                            }
                            Button(
                                enabled = processingId != req.idDemande,
                                onClick = {
                                    processingId = req.idDemande
                                    scope.launch {
                                        runCatching { container.documentRequestsApi.generate(req.idDemande) }
                                            .onSuccess {
                                                message = "Le document a été généré et envoyé au Stagiaire."
                                                reload()
                                            }
                                            .onFailure {
                                                message = userFriendlyErrorMessage(it)
                                            }
                                        processingId = null
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null)
                                Spacer(Modifier.width(6.dp))
                                Text(if (processingId == req.idDemande) "Traitement…" else "Accepter")
                            }
                        }
                    }
                }
            }
        }
    }

    refuseTarget?.let { req ->
        AlertDialog(
            onDismissRequest = { refuseTarget = null },
            title = { Text("Refuser la demande") },
            text = {
                Column {
                    Text("Indiquez le motif du refus. Le Stagiaire le verra dans le statut de sa demande.", color = GSTextSecondary)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = refuseReason,
                        onValueChange = { refuseReason = it },
                        label = { Text("Motif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = refuseReason.isNotBlank(),
                    onClick = {
                        scope.launch {
                            processingId = req.idDemande
                            runCatching {
                                container.documentRequestsApi.refuse(
                                    req.idDemande,
                                    RefuseJustificationRequest(refuseReason.trim())
                                )
                            }.onSuccess {
                                message = "La demande a été refusée."
                                refuseReason = ""
                                refuseTarget = null
                                reload()
                            }.onFailure {
                                message = userFriendlyErrorMessage(it)
                            }
                            processingId = null
                        }
                    }
                ) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { refuseTarget = null }) { Text("Annuler") }
            }
        )
    }
}
