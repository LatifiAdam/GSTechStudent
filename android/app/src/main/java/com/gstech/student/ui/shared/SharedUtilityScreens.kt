package com.gstech.student.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.ui.theme.*
import com.gstech.student.data.AppContainer
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.gstech.student.util.userFriendlyErrorMessage


@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(GSBackground).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
        }
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.LockOpen, contentDescription = null, tint = GSBluePrimary, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text("Having trouble signing in?", style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter your institutional email address below and we will send you a secure link to reset your password.",
                color = GSTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 13.sp,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Institutional Email", color = GSTextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("username@gstech.edu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
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

        if (sent) {
            Spacer(Modifier.height(10.dp))
            Text("If that address exists, a reset link has been sent. (Backend endpoint not implemented yet — this is a UI placeholder.)", color = GSTeal, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { sent = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
            enabled = email.isNotBlank(),
        ) { Text("Send Reset Link", fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("← Back to Login Page", color = GSBluePrimary)
        }
    }
}

@Composable
fun ChangePasswordScreen(container: AppContainer, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(20.dp, 20.dp, 20.dp, 0.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GSTextPrimary) }
            Text("Change Password", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        }
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            labeled("Current Password", current) { current = it }
            labeled("New Password", newPassword) { newPassword = it }
            labeled("Confirm New Password", confirm) { confirm = it }
            message?.let { Text(it, color = if (success) GSTeal else GSDanger, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }
            Button(
                onClick = {
                    message = null
                    when {
                        newPassword.length < 8 -> message = "New password must contain at least 8 characters."
                        newPassword != confirm -> message = "New passwords do not match."
                        else -> scope.launch {
                            saving = true
                            runCatching {
                                container.profileRepository.changePassword(current, newPassword)
                            }.onSuccess {
                                success = true
                                message = "Password updated successfully."
                                current = ""; newPassword = ""; confirm = ""
                            }.onFailure {
                                success = false
                                message = userFriendlyErrorMessage(it)
                            }
                            saving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                enabled = !saving && current.isNotBlank() && newPassword.isNotBlank() && confirm.isNotBlank(),
            ) { Text(if (saving) "Updating…" else "Update Password", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun labeled(label: String, value: String, onChange: (String) -> Unit) {
    Text(label, color = GSTextSecondary, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(value = value, onValueChange = onChange, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
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
}
