package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AdminUser
import com.gstech.student.model.Role
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.components.StatusPill
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun UserDetailScreen(container: AppContainer, userId: String, onBack: () -> Unit) {
    var state by remember { mutableStateOf<UiState<AdminUser>>(UiState.Loading) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showModify by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { currentUserId = container.tokenManager.userIdNow() }
    LaunchedEffect(userId) { state = safeCall { container.adminRepository.getUser(userId) } }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
        }

        when (val s = state) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(s.message) {}
            is UiState.Success -> Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
                GSCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(80.dp).clip(CircleShape).background(GSDivider), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = GSTextSecondary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(s.data.name, style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
                        Text("ID: ${displayNumericId(s.data.id)}", color = GSTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            StatusPill(s.data.role.name, GSBluePrimary)
                            Spacer(Modifier.width(6.dp))
                            StatusPill("ACTIVE", GSSuccess)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                GSCard {
                    Text("Personal Information", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary)
                    Spacer(Modifier.height(10.dp))
                    DetailRow("Email Address", s.data.email)

                    Spacer(Modifier.height(6.dp))

                    DetailRow("CIN", s.data.cin ?: "—")

                    Spacer(Modifier.height(6.dp))

                    DetailRow("Telephone", s.data.telephone ?: "—")

                    Spacer(Modifier.height(6.dp))

                    DetailRow("Address", s.data.adresse ?: "—")

                    Spacer(Modifier.height(6.dp))

                    DetailRow("Promotion", s.data.promotion ?: "—")
                    Spacer(Modifier.height(6.dp))
                    DetailRow("User ID", displayNumericId(s.data.id))
                    if (s.data.role == Role.FORMATEUR) {
                        Spacer(Modifier.height(6.dp))
                        DetailRow("Module", s.data.department ?: "—")
                    }
                }

                // An administrator must never be able to delete the account
                // that is currently signed in.
                if (s.data.id != currentUserId) {
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { showModify = true }, modifier = Modifier.weight(1f)) { Text("Modify Account") }
                        OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = GSDanger)) { Text("Delete Account") }
                    }
                } else {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "This is your currently signed-in administrator account. It cannot be deleted from the Users section.",
                        color = GSTextSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }

    if (showModify && state is UiState.Success) {
        val user = (state as UiState.Success<AdminUser>).data
        var firstName by remember(user.id) { mutableStateOf(user.name.substringBeforeLast(" ")) }
        var lastName by remember(user.id) { mutableStateOf(user.name.substringAfterLast(" ", "")) }
        var email by remember(user.id) { mutableStateOf(user.email) }
        var cin by remember(user.id) { mutableStateOf(user.cin.orEmpty()) }
        var telephone by remember(user.id) { mutableStateOf(user.telephone.orEmpty()) }
        var adresse by remember(user.id) { mutableStateOf(user.adresse.orEmpty()) }
        var module by remember(user.id) { mutableStateOf(user.department.orEmpty()) }
        var numeroEtudiant by remember(user.id) { mutableStateOf(user.studentNumber.orEmpty()) }
        var promotion by remember(user.id) { mutableStateOf(user.promotion.orEmpty()) }
        AlertDialog(
            onDismissRequest = { showModify = false },
            title = { Text("Modify account") },
            text = { Column {
                OutlinedTextField(firstName, { firstName = it }, label = { Text("First name") }, singleLine = true)
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Last name") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(cin, { cin = it }, label = { Text("CIN") }, singleLine = true)
                OutlinedTextField(telephone, { telephone = it }, label = { Text("Telephone") }, singleLine = true)
                OutlinedTextField(adresse, { adresse = it }, label = { Text("Address") }, singleLine = false)
                if (user.role == Role.FORMATEUR) {
                    OutlinedTextField(module, { module = it }, label = { Text("Subject (module)") }, singleLine = true)
                }
                if (user.role == Role.ETUDIANT) {
                    OutlinedTextField(numeroEtudiant, { numeroEtudiant = it }, label = { Text("Student number") }, singleLine = true)
                    OutlinedTextField(promotion, { promotion = it }, label = { Text("Promotion") }, singleLine = true)
                }
            } },
            confirmButton = { TextButton(onClick = {
                scope.launch {
                    runCatching { container.adminRepository.updateUser(user.id, com.gstech.student.data.remote.dto.UpdateUserRequest(nom = lastName, prenom = firstName, email = email, cin = cin.ifBlank { null }, telephone = telephone.ifBlank { null }, adresse = adresse.ifBlank { null }, module = if (user.role == Role.FORMATEUR) module.ifBlank { null } else null, numeroEtudiant = if (user.role == Role.ETUDIANT) numeroEtudiant.ifBlank { null } else null, promotion = if (user.role == Role.ETUDIANT) promotion.ifBlank { null } else null)) }
                    state = safeCall { container.adminRepository.getUser(user.id) }
                    showModify = false
                }
            }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showModify = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this account?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch { runCatching { container.adminRepository.deleteUser(userId) }; onBack() }
                }) { Text("Delete", color = GSDanger) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = GSTextSecondary)
        Text(value, color = GSTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}


private fun displayNumericId(uuid: String): String {
    val n = (uuid.lowercase().hashCode().absoluteValue % 999999) + 1
    return n.toString()
}
