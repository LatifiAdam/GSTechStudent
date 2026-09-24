package com.gstech.student.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSScreenHeader
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.*
import com.gstech.student.util.UiState

@Composable
fun ProfileScreen(
    container: AppContainer,
    onSignedOut: () -> Unit
) {
    val viewModel = remember { ProfileViewModel(container) }
    val state by viewModel.state.collectAsState()

    var showSignOutConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {

        // =========================
        // HEADER
        // =========================

        GSScreenHeader(
            title = "Mon profil",
            subtitle = "Informations personnelles et préférences",
            icon = Icons.Filled.Settings,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
        )

        // =========================
        // CONTENT
        // =========================

        Box(
            Modifier.fillMaxSize()
        ) {

            when (val s = state) {

                // =========================
                // LOADING
                // =========================

                is UiState.Loading -> {
                    LoadingState()
                }

                // =========================
                // ERROR
                // =========================

                is UiState.Error -> {
                    ErrorState(s.message) {
                        viewModel.load()
                    }
                }

                // =========================
                // SUCCESS
                // =========================

                is UiState.Success -> {

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(
                                start = 20.dp,
                                top = 20.dp,
                                end = 30.dp,
                                bottom = 20.dp
                            )
                            .verticalScroll(scrollState)
                    ) {

                        Spacer(Modifier.height(4.dp))

                        // =========================
                        // PROFILE HEADER
                        // =========================

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(GSBluePrimary.copy(alpha = 0.10f))
                                    .border(1.dp, GSBluePrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = GSTextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column {

                                Text(
                                    s.data.student.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GSTextPrimary
                                )

                                Text(
                                    "ID: ${
                                        s.data.student.studentNumber ?: "—"
                                    } • ${
                                        s.data.student.promotion ?: ""
                                    }",
                                    color = GSTextSecondary,
                                    fontSize = 13.sp
                                )

                                Text(
                                    "Groupe : ${s.data.student.groupName ?: "—"}",
                                    color = GSBluePrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // =========================
                        // CONTACT INFORMATION
                        // =========================

                        GSCard {

                            Text(
                                "Contact Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = GSTextPrimary
                            )

                            Spacer(Modifier.height(10.dp))

                            InfoRow(
                                "Email",
                                s.data.student.email
                            )

                            Spacer(Modifier.height(8.dp))

                            InfoRow(
                                "Phone",
                                s.data.student.telephone ?: "—"
                            )

                            Spacer(Modifier.height(8.dp))

                            InfoRow(
                                "CIN",
                                s.data.student.cin ?: "—"
                            )

                            Spacer(Modifier.height(8.dp))

                            InfoRow(
                                "Address",
                                s.data.student.adresse ?: "—"
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // =========================
                        // ACADEMIC SUMMARY
                        // =========================

                        GSCard {

                            Text(
                                "Academic Summary",
                                style = MaterialTheme.typography.titleMedium,
                                color = GSTextPrimary
                            )

                            Spacer(Modifier.height(10.dp))

                            val placeholder = remember {
                                AcademicPlaceholder()
                            }

                            InfoRow(
                                "GPA",
                                placeholder.gpa,
                                valueColor = GSBluePrimary
                            )

                            Spacer(Modifier.height(8.dp))

                            InfoRow(
                                "Earned Credits",
                                placeholder.credits,
                                valueColor = GSBluePrimary
                            )

                            Spacer(Modifier.height(8.dp))

                            InfoRow(
                                "Attendance",
                                "${s.data.summary.percentage}%",
                                valueColor = GSBluePrimary
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // =========================
                        // SETTINGS
                        // =========================

                        GSCard {

                            // =========================
                            // DARK MODE
                            // =========================

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        Icons.Filled.DarkMode,
                                        contentDescription = null,
                                        tint = GSTextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        "Theme (Dark Mode)",
                                        color = GSTextPrimary
                                    )
                                }

                                Switch(
                                    checked = com.gstech.student.ui.theme.ThemeController.darkTheme,
                                    onCheckedChange = { enabled ->
                                        com.gstech.student.ui.theme.ThemeController.setDarkMode(
                                            container.context, enabled
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = GSBluePrimary
                                    )
                                )
                            }

                            HorizontalDivider(
                                color = GSDivider,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            // =========================
                            // SIGN OUT
                            // =========================

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSignOutConfirm = true
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        Icons.Filled.WarningAmber,
                                        contentDescription = null,
                                        tint = GSDanger,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        "Sign Out",
                                        color = GSDanger,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = GSDanger,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }

                    // =========================
                    // SCROLLBAR
                    // =========================

                    if (scrollState.maxValue > 0) {

                        val scrollFraction =
                            scrollState.value.toFloat() /
                                    scrollState.maxValue.toFloat()

                        Box(
                            Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(12.dp)
                                .padding(
                                    top = 8.dp,
                                    bottom = 8.dp,
                                    end = 2.dp
                                )
                        ) {

                            // Track
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .align(Alignment.Center)
                                    .clip(
                                        RoundedCornerShape(4.dp)
                                    )
                                    .background(GSDivider)
                            )

                            // Thumb
                            Box(
                                Modifier
                                    .fillMaxHeight(
                                        fraction = 0.18f
                                    )
                                    .width(6.dp)
                                    .align(Alignment.TopCenter)
                                    .offset(
                                        y = (
                                                scrollFraction * 600f
                                                ).dp
                                    )
                                    .clip(
                                        RoundedCornerShape(6.dp)
                                    )
                                    .background(GSBluePrimary)
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================
    // SIGN OUT CONFIRMATION
    // =========================

    if (showSignOutConfirm) {

        AlertDialog(
            onDismissRequest = {
                showSignOutConfirm = false
            },

            title = {
                Text("Sign out?")
            },

            text = {
                Text(
                    "You'll need to sign in again with your institutional email to access GSTech."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        viewModel.signOut(onSignedOut)
                    }
                ) {

                    Text(
                        "Sign Out",
                        color = GSDanger
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

// =========================
// INFO ROW
// =========================

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = GSTextPrimary
) {

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            label,
            color = GSTextSecondary
        )

        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =========================
// SETTINGS ROW
// =========================

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically,
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = GSTextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                label,
                color = GSTextPrimary
            )
        }

        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = GSTextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}