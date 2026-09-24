package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.gstech.student.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AdminActivity
import com.gstech.student.model.AdminDashboard
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSHeader
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import androidx.compose.foundation.clickable
import com.gstech.student.ui.components.AnimatedSection

data class AdminQuickAction(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val onClick: () -> Unit)

@Composable
fun AdminHomeScreen(
    container: AppContainer,
    onOpenSettings: () -> Unit,
    headerTitle: String = "GSTech Admin",
    directorMode: Boolean = false,
    quickActions: List<AdminQuickAction> = emptyList(),
) {
    var dashboardState by remember {
        mutableStateOf<UiState<AdminDashboard>>(
            UiState.Loading
        )
    }

    var activityState by remember {
        mutableStateOf<UiState<List<AdminActivity>>>(
            UiState.Loading
        )
    }

    var currentUserGreeting by remember { mutableStateOf<String?>(null) }
    var detectedRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentUserGreeting = runCatching {
            val id = container.profileRepository.currentUserId()
            container.profileRepository.rawUser(id).let { user ->
                listOf(user.nom, user.prenom).filter { it.isNotBlank() }.joinToString(" ").ifBlank { null }
            }
        }.getOrNull()
    }

    LaunchedEffect(Unit) {

        dashboardState = safeCall {
            if (directorMode) container.adminRepository.getDirectorDashboard()
            else {
                detectedRole = container.authRepository.currentRole()?.name?.lowercase() ?: when {
                    headerTitle.contains("SRIO", ignoreCase = true) -> "srio"
                    headerTitle.contains("SCQ", ignoreCase = true) -> "scq"
                    headerTitle.contains("Directeur", ignoreCase = true) -> "directeur"
                    headerTitle.contains("Gestionnaire", ignoreCase = true) -> "gestionnaire"
                    else -> null
                }
                container.adminRepository.getDashboardForRole(detectedRole)
            }
        }

        activityState = if (directorMode) UiState.Success<List<AdminActivity>>(emptyList()) else safeCall { container.adminRepository.getRecentActivity(10) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {

        // ========================================================
        // CONTENT
        // ========================================================

        when (val state = dashboardState) {

            // ----------------------------------------------------
            // LOADING
            // ----------------------------------------------------

            is UiState.Loading -> {

                LoadingState()
            }

            // ----------------------------------------------------
            // ERROR
            // ----------------------------------------------------

            is UiState.Error -> {

                ErrorState(
                    state.message
                ) {

                    dashboardState = UiState.Loading
                    activityState = UiState.Loading

                }
            }

            // ----------------------------------------------------
            // SUCCESS
            // ----------------------------------------------------

            is UiState.Success -> {

                AnimatedSection {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {

                    // ==================================================
                    // HEADER
                    // ==================================================
                    GSHeader(
                        roleLabel = headerTitle.uppercase(),
                        greeting = currentUserGreeting ?: "Bonjour",
                        avatarText = currentUserGreeting?.take(2),
                    )
                    Spacer(Modifier.height(18.dp))

                    // ==================================================
                    // VISUAL OVERVIEW
                    // ==================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GSBluePrimary.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GSBluePrimary.copy(alpha = 0.14f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Insights,
                                        contentDescription = null,
                                        tint = GSBluePrimary,
                                        modifier = Modifier.size(27.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (directorMode) "Pilotage de votre EFP" else "Vue synthétique",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GSTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (directorMode) "Suivez les effectifs et l'activité de votre établissement." else "Une vue rapide des principaux indicateurs de votre périmètre.",
                                    color = GSTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Row(
                                modifier = Modifier.height(54.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                listOf(0.42f, 0.68f, 0.55f, 0.86f).forEach { level ->
                                    Box(
                                        modifier = Modifier
                                            .width(7.dp)
                                            .fillMaxHeight(level)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(GSBluePrimary.copy(alpha = 0.65f))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ==================================================
                    // STATISTICS
                    // ==================================================

                    if (directorMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(label = "TOTAL STUDENTS", value = "${state.data.totalStudents}", modifier = Modifier.weight(1f))
                            StatCard(label = "TOTAL TEACHERS", value = "${state.data.totalTeachers}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        StatCard(label = "TOTAL GESTIONNAIRES", value = "${state.data.totalGestionnaires}", modifier = Modifier.fillMaxWidth())
                    } else if (detectedRole == "srio") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(label = "TOTAL GESTIONNAIRES", value = "${state.data.totalGestionnaires}", modifier = Modifier.weight(1f))
                            StatCard(label = "TOTAL EFP", value = "${state.data.totalEfp}", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        StatCard(label = "TOTAL DIRECTEURS", value = "${state.data.totalDirectors}", modifier = Modifier.fillMaxWidth())
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(label = "TOTAL USERS", value = "${state.data.totalUsers}", modifier = Modifier.weight(1f))
                            StatCard(label = "TOTAL ADMINS", value = "${state.data.totalAdmins}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        StatCard(label = "TOTAL DIRECTORS", value = "${state.data.totalDirectors}", modifier = Modifier.fillMaxWidth())
                    }

                    if (quickActions.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text("Accès rapide", style = MaterialTheme.typography.titleMedium, color = GSTextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            quickActions.take(4).forEach { action ->
                                OutlinedButton(onClick = action.onClick, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(action.icon, contentDescription = action.label, tint = GSBluePrimary, modifier = Modifier.size(19.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text(action.label, fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // ==================================================
                    // RECENT ACTIVITY
                    // ==================================================

                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    when (val activities = activityState) {

                        // --------------------------------------------
                        // ACTIVITY LOADING
                        // --------------------------------------------

                        is UiState.Loading -> {

                            Text(
                                text = "Loading activity...",
                                color = GSTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        // --------------------------------------------
                        // ACTIVITY ERROR
                        // --------------------------------------------

                        is UiState.Error -> {

                            Text(
                                text = "Unable to load recent activity.",
                                color = GSTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        // --------------------------------------------
                        // ACTIVITY SUCCESS
                        // --------------------------------------------

                        is UiState.Success -> {

                            if (activities.data.isEmpty()) {

                                Text(
                                    text = "No recent activity.",
                                    color = GSTextSecondary,
                                    fontSize = 13.sp
                                )

                            } else {

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    activities.data.forEach { activity ->

                                        ActivityCard(
                                            activity = activity
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // ==================================================
                    // INFORMATION
                    // ==================================================



                    }
                }
            }
        }
    }
}


// ================================================================
// STAT CARD
// ================================================================

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = GSBluePrimary
) {

    GSCard(
        modifier = modifier
    ) {

        Text(
            text = label,
            color = GSTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
    }
}


// ================================================================
// ACTIVITY CARD
// ================================================================

@Composable
private fun ActivityCard(
    activity: AdminActivity
) {

    GSCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ActivityIcon(
                type = activity.type
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = activity.message,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = formatActivityDate(activity.date),
                    color = GSTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}


// ================================================================
// ACTIVITY ICON
// ================================================================

@Composable
private fun ActivityIcon(
    type: String
) {

    val icon = when (type.lowercase()) {

        "annonce" ->
            Icons.Filled.Campaign

        "document" ->
            Icons.Filled.Description

        "justification" ->
            Icons.Filled.UploadFile

        "appel" ->
            Icons.Filled.EventAvailable

        else ->
            Icons.Filled.Description
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = GSBluePrimary
    )
}


// ================================================================
// DATE FORMAT
// ================================================================

private fun formatActivityDate(
    value: String
): String {
    return value
}