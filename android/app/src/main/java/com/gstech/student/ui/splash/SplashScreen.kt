package com.gstech.student.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.Role
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(container: AppContainer, onReady: (role: Role?) -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        val signedIn = container.authRepository.isSignedIn()
        val role = if (signedIn) container.authRepository.currentRole() else null
        onReady(role)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0B3D63), Color(0xFF13A6A0))),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = Color(0xFF0F5FA6))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("GSTech", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 30.sp)
                    Text("SIS PLATFORM", color = Color(0xFFBFEAF0), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Your Academic Companion", color = Color(0xFFDFF3F6), fontSize = 15.sp)
        }

        Text(
            "Version 1.0",
            color = Color(0xFFCFEFF2),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        )
    }
}
