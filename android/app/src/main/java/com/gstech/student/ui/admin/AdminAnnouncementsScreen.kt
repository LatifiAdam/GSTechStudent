package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.repository.CourseAnnouncement
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun AdminAnnouncementsScreen(container: AppContainer, onNew: () -> Unit) {
    var state by remember { mutableStateOf<UiState<List<CourseAnnouncement>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()
    fun reload() { scope.launch { state = UiState.Loading; state = safeCall { container.adminRepository.getAnnouncements() } } }
    LaunchedEffect(Unit) { reload() }

    Scaffold(
        containerColor = GSBackground,
        floatingActionButton = {
            FloatingActionButton(onClick = onNew, containerColor = GSBluePrimary) {
                Icon(Icons.Filled.Add, contentDescription = "New announcement", tint = Color.White)
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text("Announcements", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary, modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp))
            when (val s = state) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> ErrorState(s.message) { reload() }
                is UiState.Success -> LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.data) { a ->
                        GSCard {
                            Text(a.title, style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(a.body, color = GSTextSecondary, fontSize = 13.sp, maxLines = 3)
                            Spacer(Modifier.height(6.dp))
                            Text(a.dateIso.take(10), color = GSTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNewAnnouncementScreen(container: AppContainer, onPosted: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var posting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPosted) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
            Text("New Announcement", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                "Posted to ALL users (RG12: general announcements are administrator-only).",
                color = GSTextSecondary, fontSize = 12.sp,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Announcement Title") }, modifier = Modifier.fillMaxWidth(),
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
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Details / Body") }, modifier = Modifier.fillMaxWidth().height(140.dp),
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

            error?.let { Text(it, color = GSDanger, fontSize = 13.sp) }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) { error = "Title and body are required."; return@Button }
                    posting = true
                    scope.launch {
                        runCatching { container.adminRepository.postGeneralAnnouncement(title, body, "generale") }
                            .onSuccess { posting = false; onPosted() }
                            .onFailure { posting = false; error = userFriendlyErrorMessage(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                enabled = !posting,
            ) {
                if (posting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Publish Announcement", fontWeight = FontWeight.Bold)
            }
        }
    }
}
