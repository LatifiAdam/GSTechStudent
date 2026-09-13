package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.DocumentRequest
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.components.StatusPill
import com.gstech.student.ui.documents.documentTypes
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

@Composable
fun AdminDocumentsScreen(container: AppContainer) {
    var state by remember { mutableStateOf<UiState<List<DocumentRequest>>>(UiState.Loading) }
    var refuseTarget by remember { mutableStateOf<DocumentRequest?>(null) }
    var refuseMotif by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun reload() { scope.launch { state = UiState.Loading; state = safeCall { container.adminRepository.getDocumentRequests() } } }
    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Text("Document Requests", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp))
        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { reload() }
            is UiState.Success -> {
                val pending = s.data.count { it.status == "en_attente" }
                val processing = s.data.count { it.status == "en_cours" }
                val ready = s.data.count { it.status == "delivree" }
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("PENDING", "$pending", GSWarning, Modifier.weight(1f))
                    MiniStat("PROCESSING", "$processing", GSBluePrimary, Modifier.weight(1f))
                    MiniStat("READY", "$ready", GSSuccess, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                if (s.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No document requests.", color = GSTextSecondary) }
                } else {
                    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.data, key = { it.id }) { req ->
                            GSCard {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(
                                            documentTypes.firstOrNull { it.first == req.type }?.second ?: req.type,
                                            fontWeight = FontWeight.SemiBold, color = GSTextPrimary,
                                        )
                                        Text("Requested: ${req.dateIso.take(10)}", color = GSTextSecondary, fontSize = 12.sp)
                                    }
                                    val (label, color) = when (req.status) {
                                        "delivree" -> "Ready" to GSSuccess
                                        "refusee" -> "Refused" to GSDanger
                                        "en_cours" -> "Processing" to GSBluePrimary
                                        else -> "Pending" to GSWarning
                                    }
                                    StatusPill(label, color)
                                }
                                if (req.status == "en_attente" || req.status == "en_cours") {
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedButton(onClick = { refuseTarget = req; refuseMotif = "" }, colors = ButtonDefaults.outlinedButtonColors(contentColor = GSDanger)) {
                                            Text("Refuse")
                                        }
                                        Button(
                                            onClick = { scope.launch { runCatching { container.adminRepository.generateDocument(req.id) }; reload() } },
                                            colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                                        ) { Text("Generate") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    refuseTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { refuseTarget = null },
            title = { Text("Refuse request") },
            text = {
                OutlinedTextField(value = refuseMotif, onValueChange = { refuseMotif = it }, label = { Text("Reason (RG10)") }, modifier = Modifier.fillMaxWidth(),
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
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { container.adminRepository.refuseDocument(target.id, refuseMotif.ifBlank { "Not eligible." }) }
                        refuseTarget = null
                        reload()
                    }
                }) { Text("Refuse", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { refuseTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    GSCard(modifier = modifier) {
        Text(label, color = GSTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}
