package com.gstech.student.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gstech.student.R
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.AccountCountsDto
import com.gstech.student.data.remote.dto.DatabaseStatusDto
import com.gstech.student.data.remote.dto.ServerStatusDto
import com.gstech.student.data.remote.dto.TechnicalLogCreateDto
import com.gstech.student.data.remote.dto.TechnicalLogDto
import com.gstech.student.ui.shared.SettingsScreen
import com.gstech.student.ui.shared.EditProfileScreen
import com.gstech.student.ui.admin.UsersListScreen
import com.gstech.student.ui.admin.AddUserScreen
import com.gstech.student.ui.admin.UserDetailScreen
import com.gstech.student.model.Role
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSDanger
import com.gstech.student.ui.theme.GSSurface
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import kotlinx.coroutines.launch
import com.gstech.student.util.userFriendlyErrorMessage

private enum class SuperAdminTab(val route: String, val label: String) {
    HOME("superadmin/home", "Accueil"),
    ACTIVITY("superadmin/activity", "Activité"),
    MAINTENANCE("superadmin/maintenance", "Maintenance"),
    ACCOUNTS("superadmin/accounts", "Comptes"),
    PROFILE("superadmin/profile", "Profil")
}

@Composable
fun SuperAdminRoot(container: AppContainer, onSignedOut: () -> Unit) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val currentEntry by nav.currentBackStackEntryAsState()
    val route = currentEntry?.destination?.route
    val routes = remember { SuperAdminTab.values().map { it.route }.toSet() }

    Scaffold(
        bottomBar = {
            if (route in routes) {
                NavigationBar {
                    SuperAdminTab.values().forEach { tab ->
                        val selected = route == tab.route
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1f else 0.94f,
                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                            label = "superAdmin_${tab.name}_scale"
                        )
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        SuperAdminTab.HOME -> Icons.Filled.Home
                                        SuperAdminTab.ACTIVITY -> Icons.Filled.ListAlt
                                        SuperAdminTab.MAINTENANCE -> Icons.Filled.Build
                                        SuperAdminTab.ACCOUNTS -> Icons.Filled.Shield
                                        SuperAdminTab.PROFILE -> Icons.Filled.Settings
                                    },
                                    contentDescription = tab.label,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = SuperAdminTab.HOME.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(SuperAdminTab.HOME.route) {
                SuperAdminHome(container)
            }
            composable(SuperAdminTab.ACTIVITY.route) {
                SuperAdminActivity(container)
            }
            composable(SuperAdminTab.MAINTENANCE.route) {
                SuperAdminMaintenance(container)
            }
            composable(SuperAdminTab.ACCOUNTS.route) {
                UsersListScreen(
                    container = container,
                    onOpenUser = { id -> nav.navigate("superadmin/account/$id") },
                    onAddUser = { nav.navigate("superadmin/account/new") },
                    showAdminUsers = true,
                    initialRole = null,
                    allowedRoleFilters = listOf(
                        Role.SUPER_ADMIN, Role.DF, Role.SRIO, Role.SCQ,
                        Role.DIRECTEUR, Role.GESTIONNAIRE, Role.FORMATEUR, Role.ETUDIANT
                    ),
                    filterLabels = mapOf(
                        Role.SUPER_ADMIN to "Super Admin",
                        Role.DF to "DF",
                        Role.SRIO to "SRIO",
                        Role.SCQ to "SCQ",
                        Role.DIRECTEUR to "Directeur",
                        Role.GESTIONNAIRE to "Gestionnaire",
                        Role.FORMATEUR to "Formateur",
                        Role.ETUDIANT to "Stagiaire"
                    )
                )
            }
            composable("superadmin/account/new") {
                AddUserScreen(
                    container = container,
                    onDone = { nav.popBackStack() },
                    allowedRoles = setOf("superadmin", "df", "srio", "scq", "directeur", "gestionnaire", "formateur", "stagiaire")
                )
            }
            composable("superadmin/account/{id}") { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                UserDetailScreen(container = container, userId = id, onBack = { nav.popBackStack() })
            }
            composable(SuperAdminTab.PROFILE.route) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onEditProfile = { nav.navigate("superadmin/edit-profile") },
                    onLanguage = {},
                    onSignOut = {
                        scope.launch {
                            runCatching { container.authRepository.logout() }
                            onSignedOut()
                        }
                    }
                )
            }
            composable("superadmin/edit-profile") {
                EditProfileScreen(container) { nav.popBackStack() }
            }
        }
    }
}

@Composable
private fun SuperAdminHome(container: AppContainer) {
    var counts by remember { mutableStateOf<AccountCountsDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { container.systemApi.accountCounts() }
            .onSuccess {
                counts = it
                error = null
            }
            .onFailure {
                error = userFriendlyErrorMessage(it) ?: "Impossible de charger la répartition des comptes."
            }
    }

    val entries = counts?.let { c ->
        listOf(
            "DF" to c.df,
            "SRIO" to c.srio,
            "SCQ" to c.scq,
            "Directeurs" to c.directeur,
            "Gestionnaires" to c.gestionnaire,
            "Formateurs" to c.formateur,
            "Stagiaires" to c.stagiaire,
            "Super Admins" to c.superadmin
        )
    } ?: emptyList()

    val totalUsers = counts?.total?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = GSBluePrimary.copy(alpha = 0.10f),
                modifier = Modifier.size(54.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "GSTech",
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Bonjour, Super Admin",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text("Maintenance technique", color = GSTextSecondary)
            }
        }

        Spacer(Modifier.height(20.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300)) +
                slideInVertically(
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 8 }
                )
        ) {
            Column {
                Text(
                    "Vue globale",
                    style = MaterialTheme.typography.titleLarge,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricMiniCard(
                        "Total comptes",
                        counts?.total?.toString() ?: "—",
                        Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        "Rôles actifs",
                        entries.count { it.second > 0 }.toString(),
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    "Répartition des comptes",
                    style = MaterialTheme.typography.titleMedium,
                    color = GSTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                error?.let {
                    Text(it, color = GSDanger)
                    Spacer(Modifier.height(4.dp))
                }

                if (counts == null && error == null) {
                    Text("Chargement des comptes…", color = GSTextSecondary)
                }

                entries.forEachIndexed { index, entry ->
                    val progress = (entry.second.toFloat() / totalUsers.toFloat()).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(650, delayMillis = index * 55),
                        label = "accountProgress_$index"
                    )
                    val percentage = (progress * 100f).let {
                        if (it >= 10f) it.toInt().toString() else String.format(java.util.Locale.US, "%.1f", it)
                    }
                    Column(Modifier.padding(vertical = 5.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.first, color = GSTextPrimary, fontWeight = FontWeight.Medium)
                            Text(
                                "${entry.second} / ${counts?.total ?: 0} (${percentage}%)",
                                color = GSBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(5.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = GSBluePrimary,
                            trackColor = GSBluePrimary.copy(alpha = 0.10f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = GSSurface)
        ) {
            Row(
                Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Shield, null, tint = GSBluePrimary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Mode technique",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Surveillance du système et de la base de données.",
                        color = GSTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GSSurface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = GSTextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(5.dp))
            Text(
                value,
                color = GSBluePrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SuperAdminActivity(container: AppContainer) {
    var activities by remember {
        mutableStateOf<List<com.gstech.student.model.AdminActivity>>(emptyList())
    }

    LaunchedEffect(Unit) {
        activities = runCatching {
            container.adminRepository.getRecentActivity(20)
        }.getOrElse { emptyList() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Activité récente",
            style = MaterialTheme.typography.headlineSmall,
            color = GSTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        if (activities.isEmpty()) {
            Text("Aucune activité disponible.", color = GSTextSecondary)
        }
        activities.forEach { activity ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GSSurface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(activity.type, color = GSBluePrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(3.dp))
                    Text(activity.message, color = GSTextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activity.date,
                        color = GSTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperAdminMaintenance(container: AppContainer) {
    var selected by remember { mutableStateOf<String?>(null) }
    var server by remember { mutableStateOf<ServerStatusDto?>(null) }
    var database by remember { mutableStateOf<DatabaseStatusDto?>(null) }
    var logs by remember { mutableStateOf<List<TechnicalLogDto>>(emptyList()) }
    var report by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun openServer() {
        selected = "server"
        message = null
        scope.launch {
            runCatching { container.systemApi.health() }
                .onSuccess { server = it }
                .onFailure { message = userFriendlyErrorMessage(it) }
        }
    }

    fun openDatabase() {
        selected = "database"
        message = null
        scope.launch {
            runCatching { container.systemApi.database() }
                .onSuccess { database = it }
                .onFailure { message = userFriendlyErrorMessage(it) }
        }
    }

    fun openLogs() {
        selected = "logs"
        message = null
        scope.launch {
            runCatching { container.systemApi.logs() }
                .onSuccess { logs = it }
                .onFailure { message = userFriendlyErrorMessage(it) }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Maintenance",
            style = MaterialTheme.typography.headlineSmall,
            color = GSTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        MaintenanceCard(
            title = "État du serveur",
            body = "Disponibilité générale de l’API",
            icon = Icons.Filled.Build,
            onClick = ::openServer
        )
        MaintenanceCard(
            title = "Base de données",
            body = "État de la connexion MySQL",
            icon = Icons.Filled.Storage,
            onClick = ::openDatabase
        )
        MaintenanceCard(
            title = "Journaux système",
            body = "Rapports des changements du Super Admin",
            icon = Icons.Filled.Description,
            onClick = ::openLogs
        )

        message?.let {
            Text(it, color = GSDanger, modifier = Modifier.padding(vertical = 8.dp))
        }

        when (selected) {
            "server" -> InfoCard(
                title = "État du serveur",
                values = listOf(
                    "Statut" to (server?.status ?: "Chargement…"),
                    "API" to (server?.api ?: "—"),
                    "Uptime" to (server?.uptimeSeconds?.let(::formatUptime) ?: "—"),
                    "Dernière vérification" to (server?.timestamp ?: "—")
                )
            )

            "database" -> InfoCard(
                title = "Base de données",
                values = listOf(
                    "Statut" to (database?.status ?: "Chargement…"),
                    "Base" to (database?.database ?: "—"),
                    "Hôte" to (database?.host ?: "—"),
                    "Port" to (database?.port?.toString() ?: "—"),
                    "Réponse" to (
                        database?.responseMs?.let { ms -> "$ms ms" }
                            ?: database?.error
                            ?: "—"
                        )
                )
            )

            "logs" -> Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GSSurface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Journaux système",
                        style = MaterialTheme.typography.titleMedium,
                        color = GSTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = report,
                        onValueChange = { report = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        label = { Text("Rapport du changement") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        enabled = report.isNotBlank(),
                        onClick = {
                            scope.launch {
                                runCatching {
                                    container.systemApi.addLog(
                                        TechnicalLogCreateDto(report.trim())
                                    )
                                }
                                    .onSuccess {
                                        report = ""
                                        logs = listOf(it) + logs
                                        message = "Rapport enregistré."
                                    }
                                    .onFailure {
                                        message = userFriendlyErrorMessage(it)
                                    }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.UploadFile, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Enregistrer le rapport")
                    }
                    Spacer(Modifier.height(14.dp))
                    logs.forEach { log ->
                        Text(
                            log.date,
                            color = GSTextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            log.report,
                            color = GSTextPrimary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCard(
    title: String,
    body: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GSSurface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = GSBluePrimary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = GSTextPrimary, fontWeight = FontWeight.Bold)
                Text(body, color = GSTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, values: List<Pair<String, String>>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GSSurface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = GSTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            values.forEach { (key, value) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(key, color = GSTextSecondary)
                    Text(value, color = GSTextPrimary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return if (days > 0) "${days}j ${hours}h ${minutes}min" else "${hours}h ${minutes}min"
}
