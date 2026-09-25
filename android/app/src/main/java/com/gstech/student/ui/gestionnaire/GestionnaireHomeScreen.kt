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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import com.gstech.student.ui.components.GSHeader
import com.gstech.student.ui.components.GSMetricCard
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
            container.profileRepository.rawUser(id).let { u -> "Bonjour, ${u.prenom.ifBlank { u.nom }}" }
        }.getOrNull()
    }
    LaunchedEffect("counts") {
        runCatching {
            studentCount = container.adminRepository.getUsersDto(Role.ETUDIANT).size
            teacherCount = container.adminRepository.getUsersDto(Role.FORMATEUR).size
        }
    }

    Column(Modifier.fillMaxSize().background(GSBackground).verticalScroll(rememberScrollState()).padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 18.dp)) {
        GSHeader(
            roleLabel = "GESTION DOCUMENTAIRE",
            greeting = userGreeting ?: "Bonjour",
            avatarText = userGreeting?.substringAfter(",")?.trim()?.take(2),
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GSMetricCard("Stagiaires", studentCount.toString(), Modifier.weight(1f), "Dans votre EFP")
            GSMetricCard("Formateurs", teacherCount.toString(), Modifier.weight(1f), "Équipe active")
        }

        Spacer(Modifier.height(17.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onGestionDocumentaire),
            shape = RoundedCornerShape(18.dp),
            color = GSSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GSDivider),
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = GSBluePrimary.copy(alpha = .10f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Description, null, tint = GSBluePrimary) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Gestion documentaire", style = MaterialTheme.typography.titleLarge, color = GSTextPrimary)
                    Spacer(Modifier.height(3.dp))
                    Text("Documents, demandes, groupes et stagiaires de votre EFP.", color = GSTextSecondary, fontSize = 12.sp)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = GSBluePrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
