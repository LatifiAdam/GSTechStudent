package com.gstech.student.ui.gestionnaire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.R
import com.gstech.student.data.AppContainer
import com.gstech.student.model.Role
import com.gstech.student.ui.theme.*

@Composable
fun GestionnaireHomeScreen(container: AppContainer, onGestionDocumentaire: () -> Unit = {}) {
    var userGreeting by remember { mutableStateOf<String?>(null) }
    var studentCount by remember { mutableStateOf(0) }
    var teacherCount by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        userGreeting = runCatching {
            val id = container.profileRepository.currentUserId()
            container.profileRepository.rawUser(id).let { u ->
                "Bonjour, ${u.nom}, ${u.prenom}"
            }
        }.getOrNull()
    }

    LaunchedEffect("counts") {
        runCatching {
            studentCount = container.adminRepository.getUsersDto(Role.ETUDIANT).size
            teacherCount = container.adminRepository.getUsersDto(Role.FORMATEUR).size
        }
    }

    Column(
        Modifier.fillMaxSize().background(GSBackground).verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = GSBluePrimary.copy(alpha=.10f), modifier = Modifier.size(52.dp)) {
                Image(
                    painter = painterResource(R.drawable.logo_v5),
                    contentDescription = "GSTech",
                    modifier = Modifier.padding(4.dp).clip(CircleShape)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = userGreeting ?: "Bonjour",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text("GSTech • Gestionnaire", color = GSTextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GestionnaireStatCard("Stagiaires", studentCount, Modifier.weight(1f))
            GestionnaireStatCard("Formateurs", teacherCount, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        AnimatedVisibility(visible = true, enter = fadeIn(tween(350)) + slideInVertically(tween(400), initialOffsetY = { it / 10 })) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onGestionDocumentaire),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GSSurface)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape=RoundedCornerShape(14.dp), color=GSBluePrimary.copy(alpha=.10f), modifier=Modifier.size(50.dp)) {
                        Box(contentAlignment=Alignment.Center) { Icon(Icons.Filled.Description, null, tint=GSBluePrimary) }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Gestion documentaire", style=MaterialTheme.typography.titleLarge, color=GSTextPrimary, fontWeight=FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Préparez les documents, suivez les demandes et gérez les groupes et les stagiaires de votre EFP.", color=GSTextSecondary)
                    }
                }
            }
        }
    }
}


@Composable
private fun GestionnaireStatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GSSurface)) {
        Column(Modifier.padding(14.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = GSBluePrimary, fontWeight = FontWeight.Bold)
            Text(label, color = GSTextSecondary, fontSize = 12.sp)
        }
    }
}
