package com.gstech.student.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.Role
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun LoginScreen(container: AppContainer, onLoggedIn: (Role) -> Unit, onForgotPassword: () -> Unit = {}) {
    val viewModel = remember { LoginViewModel(container.authRepository) }
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(GSBackground)) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GSBluePrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.School, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.height(10.dp))
            Text("GSTech", color = GSBluePrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Text("SIS PLATFORM", color = GSTeal, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }

        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = GSSurface,
            shadowElevation = 8.dp,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Welcome Back", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
                Text(
                    "Sign in to manage your student journey",
                    color = GSTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Institutional Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
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
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle password visibility",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
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

                if (state is UiState.Error) {
                    Text(
                        (state as UiState.Error).message,
                        color = GSDanger,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }

                TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                    Text("Forgot Password?", color = GSBluePrimary, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { viewModel.login(email, password, onSuccess = onLoggedIn) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GSBluePrimary),
                    enabled = state !is UiState.Loading,
                ) {
                    if (state is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "Contact your institution for access or credentials.",
                    color = GSTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}
