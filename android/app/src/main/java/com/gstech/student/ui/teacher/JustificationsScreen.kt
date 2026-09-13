package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.JustificationItem
import com.gstech.student.model.JustificationStatus
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.components.StatusPill
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun JustificationsScreen(container: AppContainer) {
    val viewModel = remember { JustificationsViewModel(container) }
    val state by viewModel.state.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var rejectTarget by remember { mutableStateOf<JustificationItem?>(null) }
    var rejectMotif by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Text("Justifications", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 0.dp))

        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill("Pending", filter == JustificationStatus.PENDING) { viewModel.setFilter(JustificationStatus.PENDING) }
            FilterPill("Approved", filter == JustificationStatus.APPROVED) { viewModel.setFilter(JustificationStatus.APPROVED) }
            FilterPill("Rejected", filter == JustificationStatus.REJECTED) { viewModel.setFilter(JustificationStatus.REJECTED) }
        }

        Spacer(Modifier.height(14.dp))
        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) { viewModel.load() }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nothing here.", color = GSTextSecondary) }
                } else {
                    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.data, key = { it.id }) { item ->
                            JustificationCard(
                                item = item,
                                onApprove = { viewModel.approve(item) },
                                onReject = { rejectTarget = item; rejectMotif = "" },
                            )
                        }
                    }
                }
            }
        }
    }

    rejectTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("Reject justification") },
            text = {
                Column {
                    Text("Give ${target.studentName} a reason.", color = GSTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = rejectMotif, onValueChange = { rejectMotif = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth(),
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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reject(target, rejectMotif.ifBlank { "Not justified." })
                    rejectTarget = null
                }) { Text("Reject", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { rejectTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) GSBluePrimary else GSSurface,
        modifier = Modifier.selectable(selected = selected, onClick = onClick),
    ) {
        Text(label, color = if (selected) Color.White else GSTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

@Composable
private fun JustificationCard(item: JustificationItem, onApprove: () -> Unit, onReject: () -> Unit) {
    GSCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(item.studentName, fontWeight = FontWeight.SemiBold, color = GSTextPrimary)
                Text("Absence: ${item.dateIso.take(10)}", color = GSTextSecondary, fontSize = 12.sp)
            }
            val (label, color) = when (item.status) {
                JustificationStatus.PENDING -> "Pending" to GSWarning
                JustificationStatus.APPROVED -> "Approved" to GSSuccess
                JustificationStatus.REJECTED -> "Rejected" to GSDanger
            }
            StatusPill(label, color)
        }
        Spacer(Modifier.height(8.dp))
        Text("Reason: \"${item.motif}\"", color = GSTextSecondary, fontSize = 13.sp)
        item.attachment?.let {
            Spacer(Modifier.height(6.dp))
            Text("📄 Attachment provided", color = GSBluePrimary, fontSize = 12.sp)
        }
        if (item.status == JustificationStatus.PENDING) {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = GSDanger)) {
                    Text("Reject")
                }
                Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary)) {
                    Text("Approve")
                }
            }
        }
    }
}
