package com.gstech.student.ui.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import com.gstech.student.data.AppContainer
import com.gstech.student.model.DocumentRequest
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.components.StatusPill
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun DocumentsScreen(container: AppContainer, onBack: () -> Unit) {
    val viewModel = remember { DocumentsViewModel(container) }
    val state by viewModel.state.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val approved by viewModel.approved.collectAsState()
    var showRequestDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = GSBackground,
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary)
                }
                Text("Documents", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showRequestDialog = true }, containerColor = GSBluePrimary) {
                Icon(Icons.Filled.Add, contentDescription = "Request a document", tint = androidx.compose.ui.graphics.Color.White)
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (val s = state) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> ErrorState(s.message) { viewModel.load() }
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No document requests yet. Tap + to request one.", color = GSTextSecondary)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(s.data, key = { it.id }) { req -> DocumentRow(req) {
                                if (req.status == "delivree") scope.launch {
                                    runCatching {
                                        val body = container.documentsRepository.getRequestFile(req.id)
                                        val f = File(container.context.cacheDir, "document-${req.id}.pdf")
                                        body.byteStream().use { input -> f.outputStream().use { input.copyTo(it) } }
                                        val uri = FileProvider.getUriForFile(container.context, "${container.context.packageName}.fileprovider", f)
                                        container.context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, uri).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK) }, "Ouvrir le document").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                                    }
                                }
                            } }
                        }
                    }
                }
            }
        }
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { if (!submitting) showRequestDialog = false },
            title = { Text("Request a document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Documents disponibles", color = GSTextSecondary)
                    documentTypes.forEach { (code, label) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GSSurface,
                            modifier = Modifier.fillMaxWidth().clickable(enabled = !submitting) {
                                viewModel.requestDocument(code)
                                showRequestDialog = false
                            }
                        ) {
                            Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(label, color = GSTextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("Générée avec vos informations personnelles", color = GSTextSecondary, fontSize = 12.sp)
                                }
                                Text("Demander", color = GSBluePrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (approved.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Documents approuvés par le Directeur", color = GSTextPrimary, fontWeight = FontWeight.SemiBold)
                        approved.forEach { doc ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = GSSurface,
                                modifier = Modifier.fillMaxWidth().clickable(enabled = !submitting) {
                                    viewModel.requestApprovedDocument(doc.idDocument)
                                    showRequestDialog = false
                                }
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(doc.nomDocument, color = GSTextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("Document validé par la direction", color = GSTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Text("Aucun autre document approuvé n'est disponible.", color = GSTextSecondary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showRequestDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DocumentRow(req: DocumentRequest, onOpen: () -> Unit) {
    Surface(color = Color(0xFFD9EEFF), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
    GSCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    documentTypes.firstOrNull { it.first == req.type }?.second ?: req.type,
                    fontWeight = FontWeight.SemiBold,
                    color = GSTextPrimary,
                )
                Text(req.dateIso.take(10), color = GSTextSecondary, fontSize = 12.sp)
            }
            val (label, color) = when (req.status) {
                "delivree" -> "Delivered" to GSSuccess
                "refusee" -> "Refused" to GSDanger
                "en_cours" -> "In Progress" to GSWarning
                else -> "Pending" to GSTextSecondary
            }
            if (req.status == "delivree") TextButton(onClick = onOpen) { Text("Open", color = GSBluePrimary) }
            StatusPill(label, color)
        }
    }
    }
}
