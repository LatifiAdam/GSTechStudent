package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AdminUser
import com.gstech.student.model.Role
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.components.StatusPill
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.launch

@Composable
fun UsersListScreen(
    container: AppContainer,
    onOpenUser: (String) -> Unit,
    onAddUser: () -> Unit,
    showAdminUsers: Boolean = true,
    initialRole: String? = null,
    allowedRoleFilters: List<Role>? = null,
    filterLabels: Map<Role, String> = emptyMap()
) {
    var state by remember {
        mutableStateOf<UiState<List<AdminUser>>>(UiState.Loading)
    }

    // null = all users
    var roleFilter by remember {
        mutableStateOf(
            when (initialRole?.lowercase()) {
                "stagiaire", "etudiant", "student" -> Role.ETUDIANT
                "formateur", "enseignant", "teacher" -> Role.FORMATEUR
                "gestionnaire" -> Role.GESTIONNAIRE
                "directeur" -> Role.DIRECTEUR
                "srio" -> Role.SRIO
                "scq" -> Role.SCQ
                "df" -> Role.DF
                "superadmin", "admin" -> Role.SUPER_ADMIN
                else -> null
            }
        )
    }

    var query by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            state = UiState.Loading

            state = safeCall {
                val allUsers = container.adminRepository.getUsers(null)

                // Apply the role filter client-side using the canonical Role mapping.
                // This avoids empty results when a backend deployment uses a legacy
                // role spelling/query implementation.
                allUsers
                    .filter { roleFilter == null || it.role == roleFilter }
                    .filter { showAdminUsers || it.role != Role.SUPER_ADMIN }
                    .filter { allowedRoleFilters == null || it.role in allowedRoleFilters }
            }
        }
    }


    LaunchedEffect(roleFilter) {
        reload()
    }

    Scaffold(
        containerColor = GSBackground,

        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddUser,
                containerColor = GSBluePrimary
            ) {
                Icon(
                    Icons.Filled.PersonAdd,
                    contentDescription = "Add user",
                    tint = Color.White
                )
            }
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // =========================
            // HEADER
            // =========================

            Text(
                "Users",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary,
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                )
            )

            // =========================
            // SEARCH
            // =========================

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                },
                placeholder = {
                    Text("Search users, IDs, departments...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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

            Spacer(Modifier.height(12.dp))

            // =========================
            // ROLE FILTERS
            // =========================

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 20.dp)
            ) {
                if (allowedRoleFilters == null || allowedRoleFilters.size > 1) {
                    item { RolePill("All", roleFilter == null) { roleFilter = null } }
                }
                val rolesToShow = allowedRoleFilters ?: listOf(
                    Role.ETUDIANT, Role.FORMATEUR, Role.DIRECTEUR, Role.GESTIONNAIRE
                ) + if (showAdminUsers) listOf(Role.SUPER_ADMIN) else emptyList()
                rolesToShow.distinct().forEach { filterRole ->
                    val defaultLabel = when (filterRole) {
                        Role.ETUDIANT -> "Stagiaire"
                        Role.FORMATEUR -> "Formateur"
                        Role.DIRECTEUR -> "Directeur"
                        Role.GESTIONNAIRE -> "Gestionnaire"
                        Role.SRIO -> "SRIO"
                        Role.SCQ -> "SCQ"
                        Role.DF -> "DF"
                        Role.SUPER_ADMIN -> "SuperAdmin"
                    }
                    item { RolePill(filterLabels[filterRole] ?: defaultLabel, roleFilter == filterRole) { roleFilter = filterRole } }
                }
            }

            Spacer(Modifier.height(12.dp))

            // =========================
            // CONTENT
            // =========================

            when (val s = state) {

                // LOADING
                is UiState.Loading -> {
                    LoadingState()
                }

                // ERROR
                is UiState.Error -> {
                    ErrorState(s.message) {
                        reload()
                    }
                }

                // SUCCESS
                is UiState.Success -> {

                    val filtered = s.data.filter { user ->

                        query.isBlank() ||
                                user.name.contains(query, ignoreCase = true) ||
                                user.email.contains(query, ignoreCase = true) ||
                                (user.studentNumber?.contains(
                                    query,
                                    ignoreCase = true
                                ) == true) ||
                                (user.promotion?.contains(
                                    query,
                                    ignoreCase = true
                                ) == true)
                    }

                    // =========================
                    // RESULT COUNT
                    // =========================

                    Text(
                        "${filtered.size} Users Listed",
                        color = GSTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    // =========================
                    // USER LIST
                    // =========================

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        items(
                            items = filtered,
                            key = { it.id }
                        ) { user ->

                            UserRow(
                                user = user,
                                onClick = {
                                    onOpenUser(user.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RolePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) {
            GSBluePrimary
        } else {
            GSSurface
        },
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onClick
        )
    ) {

        Text(
            text = label,
            color = if (selected) {
                Color.White
            } else {
                GSTextPrimary
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
        )
    }
}

@Composable
private fun UserRow(
    user: AdminUser,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GSSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {

        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // =========================
            // AVATAR
            // =========================

            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GSDivider)
            )

            Spacer(Modifier.width(12.dp))

            // =========================
            // USER INFORMATION
            // =========================

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    user.name,
                    fontWeight = FontWeight.SemiBold,
                    color = GSTextPrimary
                )

                Text(
                    user.promotion ?: user.email,
                    color = GSTextSecondary,
                    fontSize = 12.sp
                )
            }

            // =========================
            // ROLE
            // =========================

            StatusPill(
                when (user.role) {
                    Role.DF -> "DF"
                    Role.SRIO -> "SRIO"
                    Role.SCQ -> "SCQ"
                    Role.ETUDIANT -> "Student"
                    Role.FORMATEUR -> "TEACHER"
                    Role.SUPER_ADMIN -> "ADMIN"
                    Role.DIRECTEUR -> "Dir"
                    Role.GESTIONNAIRE -> "Gestion"
                },

                when (user.role) {
                    Role.DF -> GSDanger
                    Role.SRIO -> GSDanger
                    Role.SCQ -> GSDanger
                    Role.ETUDIANT -> GSSuccess
                    Role.FORMATEUR -> GSBluePrimary
                    Role.SUPER_ADMIN -> GSDanger
                    Role.DIRECTEUR -> GSBluePrimary
                    Role.GESTIONNAIRE -> GSTextSecondary
                }
            )
        }
    }
}