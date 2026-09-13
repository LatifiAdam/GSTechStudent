package com.gstech.student.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstech.student.ui.theme.*

@Composable
fun TwoFactorVerificationScreen(email: String, onVerified: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(GSBackground).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Two-Factor Authentication", style = MaterialTheme.typography.headlineMedium, color = GSTextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Enter the 6-digit code sent to $email.", color = GSTextSecondary)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, label={Text("Verification code")}, singleLine=true, modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick=onVerified, enabled=code.length==6, modifier=Modifier.fillMaxWidth()){Text("Verify")}
        Spacer(Modifier.height(12.dp))
        Text("Email delivery is intentionally left for the backend integration phase.", color=GSTextSecondary, style=MaterialTheme.typography.bodySmall)
    }
}
