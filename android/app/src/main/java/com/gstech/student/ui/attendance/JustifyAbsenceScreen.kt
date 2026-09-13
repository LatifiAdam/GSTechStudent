package com.gstech.student.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AttendanceRecord
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

private val reasons = listOf(
    "Medical Excuse / Illness",
    "Official Academic Event",
    "Family Emergency",
    "Transportation Issue",
    "Other",
)

@Composable
fun JustifyAbsenceScreen(container: AppContainer, onDone: () -> Unit) {
    val viewModel = remember { JustifyAbsenceViewModel(container) }
    val state by viewModel.state.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val submitted by viewModel.submitted.collectAsState()

    var selectedRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var reason by remember { mutableStateOf(reasons.first()) }
    var notes by remember { mutableStateOf("") }
    var showReasonPicker by remember { mutableStateOf(false) }
    var showRecordPicker by remember { mutableStateOf(false) }

    LaunchedEffect(submitted) { if (submitted) onDone() }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
            Text("Justify Absence", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> {
                if (selectedRecord == null) selectedRecord = s.data.firstOrNull()

                if (s.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No unjustified absences or late marks to report right now.", color = GSTextSecondary, modifier = Modifier.padding(32.dp))
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(20.dp)) {
                        Text("Absence", color = GSTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GSSurface,
                            modifier = Modifier.fillMaxWidth().selectable(selected = true, onClick = { showRecordPicker = true }),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(selectedRecord?.courseName ?: "—", color = GSTextPrimary, fontWeight = FontWeight.Medium)
                                    Text(selectedRecord?.dateTimeIso?.take(10) ?: "—", color = GSTextSecondary, fontSize = 12.sp)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = GSTextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Justification Reason", color = GSTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GSSurface,
                            modifier = Modifier.fillMaxWidth().selectable(selected = true, onClick = { showReasonPicker = true }),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(reason, color = GSTextPrimary)
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = GSTextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Additional Notes", color = GSTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = notes, onValueChange = { notes = it },
                            placeholder = { Text("Add any extra context...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(14.dp),
                        
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

                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                val presenceId = selectedRecord?.id ?: return@Button
                                val motif = if (notes.isNotBlank()) "$reason — $notes" else reason
                                viewModel.submit(presenceId, motif, onDone = {})
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                            enabled = !submitting && selectedRecord != null,
                        ) {
                            if (submitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Submit Justification", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (showRecordPicker) {
                    AlertDialog(
                        onDismissRequest = { showRecordPicker = false },
                        title = { Text("Select absence") },
                        text = {
                            Column {
                                s.data.forEach { record ->
                                    TextButton(onClick = { selectedRecord = record; showRecordPicker = false }) {
                                        Text("${record.courseName} — ${record.dateTimeIso?.take(10) ?: "—"}", color = GSTextPrimary)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = { TextButton(onClick = { showRecordPicker = false }) { Text("Close") } },
                    )
                }

                if (showReasonPicker) {
                    AlertDialog(
                        onDismissRequest = { showReasonPicker = false },
                        title = { Text("Justification reason") },
                        text = {
                            Column {
                                reasons.forEach { r ->
                                    TextButton(onClick = { reason = r; showReasonPicker = false }) { Text(r, color = GSTextPrimary) }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = { TextButton(onClick = { showReasonPicker = false }) { Text("Close") } },
                    )
                }
            }
        }
    }
}
