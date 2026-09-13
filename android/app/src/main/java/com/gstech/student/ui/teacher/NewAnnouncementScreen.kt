package com.gstech.student.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun NewAnnouncementScreen(container: AppContainer, courseId: String, courseName: String, onPosted: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("examen") } // examen | controle
    var posting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPosted) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
            Text("New Announcement", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }

        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text("For: $courseName", color = GSTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(16.dp))

            Text("Title", color = GSTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, placeholder = { Text("Enter announcement title...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
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

            Spacer(Modifier.height(14.dp))
            Text("Body Message", color = GSTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = body, onValueChange = { body = it },
                placeholder = { Text("Write full announcement details here...") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
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

            Spacer(Modifier.height(14.dp))
            Text("Announcement Type", color = GSTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip("Exam", type == "examen") { type = "examen" }
                TypeChip("Test / Quiz", type == "controle") { type = "controle" }
            }

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = GSDanger, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        error = "Title and body are required."
                        return@Button
                    }
                    posting = true
                    scope.launch {
                        runCatching { container.teacherRepository.postAnnouncement(courseId, title, body, type, null) }
                            .onSuccess { posting = false; onPosted() }
                            .onFailure { posting = false; error = userFriendlyErrorMessage(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                enabled = !posting,
            ) {
                if (posting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Publish Announcement", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) GSBluePrimary else GSSurface,
        modifier = Modifier.selectable(selected = selected, onClick = onClick),
    ) {
        Text(label, color = if (selected) Color.White else GSTextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}
