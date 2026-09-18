package com.gstech.student.ui.shared

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.UpdateUserRequest
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun EditProfileScreen(container: AppContainer, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UiState<com.gstech.student.data.remote.dto.UserDto>>(UiState.Loading) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var changingPassword by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var uploadingProfileImage by remember { mutableStateOf(false) }
    var profileImageMessage by remember { mutableStateOf<String?>(null) }

    val profilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch {
            uploadingProfileImage = true
            profileImageMessage = null
            runCatching {
                val resolver = container.context.contentResolver
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("Impossible de lire l'image.")
                if (bytes.size > 2 * 1024 * 1024) error("L'image ne doit pas dépasser 2 Mo.")
                val mime = resolver.getType(uri) ?: error("Format d'image non reconnu.")
                if (mime !in setOf("image/jpeg", "image/png", "image/webp")) {
                    error("Format accepté : JPG, PNG ou WEBP.")
                }
                container.profileRepository.uploadProfileImage(bytes, "profile_image", mime)
                container.profileRepository.getProfileImageBitmap()
            }.onSuccess {
                profileBitmap = it
                profileImageMessage = "Photo de profil mise à jour."
            }.onFailure {
                profileImageMessage = userFriendlyErrorMessage(it)
            }
            uploadingProfileImage = false
        }
    }

    LaunchedEffect(Unit) {
        state = runCatching {
            val id = container.tokenManager.userIdNow() ?: error("No signed-in user.")
            container.adminRepository.getUserDto(id)
        }.fold(
            onSuccess = {
                firstName = it.prenom
                lastName = it.nom
                email = it.email
                UiState.Success(it)
            },
            onFailure = { UiState.Error(userFriendlyErrorMessage(it)) },
        )
    }

    LaunchedEffect(Unit) {
        runCatching { container.profileRepository.getProfileImageBitmap() }
            .onSuccess { profileBitmap = it }
    }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GSTextPrimary)
            }
            Text(
                "Edit Profile & Password",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        when (state) {
            UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState((state as UiState.Error).message) { onBack() }
            is UiState.Success -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            ) {
                profileBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Photo de profil",
                        modifier = Modifier.size(104.dp).padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = { profilePicker.launch(arrayOf("image/jpeg", "image/png", "image/webp")) },
                    enabled = !uploadingProfileImage,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uploadingProfileImage) "Upload en cours…" else "Modifier la photo de profil")
                }
                profileImageMessage?.let {
                    Text(it, color = if (it.contains("mise à jour", ignoreCase = true)) GSTeal else GSDanger, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("Profile information", style = MaterialTheme.typography.titleLarge, color = GSTextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                ProfileField("First name", firstName) { firstName = it }
                Spacer(Modifier.height(10.dp))
                ProfileField("Last name", lastName) { lastName = it }
                Spacer(Modifier.height(10.dp))
                ProfileField("Email", email) { email = it }
                message?.let { Text(it, color = GSTeal, modifier = Modifier.padding(top = 10.dp)) }
                Spacer(Modifier.height(12.dp))
                Button(
                    enabled = !saving && firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank(),
                    onClick = {
                        scope.launch {
                            saving = true
                            message = null
                            runCatching {
                                val id = container.tokenManager.userIdNow() ?: error("No signed-in user.")
                                container.adminRepository.updateUser(id, UpdateUserRequest(lastName.trim(), firstName.trim(), email.trim()))
                            }.onSuccess { message = "Profile updated." }
                                .onFailure { message = userFriendlyErrorMessage(it) }
                            saving = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (saving) "Saving…" else "Save Changes") }

                Spacer(Modifier.height(28.dp))
                HorizontalDivider()
                Spacer(Modifier.height(24.dp))
                Text("Change password", style = MaterialTheme.typography.titleLarge, color = GSTextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Enter your current password and choose a new password with at least 8 characters.", color = GSTextSecondary)
                Spacer(Modifier.height(14.dp))

                PasswordField("Current password", currentPassword) { currentPassword = it }
                Spacer(Modifier.height(10.dp))
                PasswordField("New password", newPassword) { newPassword = it }
                Spacer(Modifier.height(10.dp))
                PasswordField("Confirm new password", confirmPassword) { confirmPassword = it }

                passwordMessage?.let {
                    Text(
                        it,
                        color = if (passwordSuccess) GSTeal else GSDanger,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    enabled = !changingPassword && currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank(),
                    onClick = {
                        passwordMessage = null
                        when {
                            newPassword.length < 8 -> passwordMessage = "New password must contain at least 8 characters."
                            newPassword != confirmPassword -> passwordMessage = "New passwords do not match."
                            else -> scope.launch {
                                changingPassword = true
                                runCatching {
                                    container.profileRepository.changePassword(currentPassword, newPassword)
                                }.onSuccess {
                                    passwordSuccess = true
                                    passwordMessage = "Password updated successfully."
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }.onFailure {
                                    passwordSuccess = false
                                    passwordMessage = userFriendlyErrorMessage(it)
                                }
                                changingPassword = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                ) { Text(if (changingPassword) "Updating…" else "Update Password") }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = GSBluePrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = GSBluePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = GSBluePrimary,
            errorBorderColor = GSDanger,
        ),
    )
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = GSBluePrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = GSBluePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = GSBluePrimary,
            errorBorderColor = GSDanger,
        ),
    )
}
