package com.gstech.student.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.theme.*
import java.time.LocalDate
import androidx.compose.ui.graphics.Color

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit = {},
    onLanguage: () -> Unit,
    onSignOut: () -> Unit,
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {

        // =========================================================
        // HEADER
        // =========================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GSTextPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary
            )
        }

        // =========================================================
        // SCROLLABLE CONTENT
        // =========================================================

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        start = 20.dp,
                        top = 20.dp,
                        end = 32.dp,
                        bottom = 24.dp
                    )
            ) {

                // =================================================
                // ACADEMIC SETTINGS
                // =================================================

                SectionLabel("ACADEMIC SETTINGS")

                GSCard {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Academic Year",
                            color = GSTextPrimary,
                            modifier = Modifier.weight(0.8f)
                        )

                        val currentDate = remember {
                            LocalDate.now()
                        }

                        val currentYear = currentDate.year
                        val currentMonth = currentDate.monthValue

                        val defaultSchoolYear = remember {
                            if (currentMonth in 1..6) {
                                "${currentYear - 1}/$currentYear"
                            } else {
                                "$currentYear/${currentYear + 1}"
                            }
                        }

                        var schoolYear by remember {
                            mutableStateOf(defaultSchoolYear)
                        }

                        OutlinedTextField(
                            value = schoolYear,
                            onValueChange = { input ->

                                val digits = input
                                    .filter { it.isDigit() }
                                    .take(8)

                                schoolYear = when {
                                    digits.length <= 4 -> {
                                        digits
                                    }

                                    else -> {
                                        "${digits.take(4)}/${digits.drop(4)}"
                                    }
                                }
                            },
                            label = {
                                Text("School year")
                            },
                            placeholder = {
                                Text("2025/2026")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.weight(1.2f),
                        
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
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // =================================================
                // ACCOUNT SETTINGS
                // =================================================

                SectionLabel("ACCOUNT SETTINGS")

                GSCard {

                    Row2(
                        label = "Edit Profile & Password",
                        onClick = onEditProfile
                    )

                    HorizontalDivider(
                        color = GSDivider,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Two-Factor Authentication",
                            color = GSTextPrimary
                        )

                        val context = androidx.compose.ui.platform.LocalContext.current
                        Switch(
                            checked = TwoFactorPreference.enabled,
                            onCheckedChange = { TwoFactorPreference.setEnabled(context, it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = GSBluePrimary)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // =================================================
                // PREFERENCES
                // =================================================

                SectionLabel("PREFERENCES")

                GSCard {

                    Row2(
                        label = "App Language",
                        onClick = onLanguage,
                        trailingText = "English"
                    )

                    HorizontalDivider(
                        color = GSDivider,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark Mode", color = GSTextPrimary)
                        Switch(
                            checked = com.gstech.student.ui.theme.ThemeController.darkTheme,
                            onCheckedChange = { com.gstech.student.ui.theme.ThemeController.setDarkMode(context, it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = GSBluePrimary)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // =================================================
                // ABOUT SIS
                // =================================================

                SectionLabel("ABOUT SIS")

                GSCard {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Application Version",
                            color = GSTextPrimary
                        )

                        Text(
                            text = "v1.0",
                            color = GSTextSecondary
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // =================================================
                // SIGN OUT
                // =================================================

                OutlinedButton(
                    onClick = {
                        showSignOutConfirm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GSDanger
                    )
                ) {

                    Text(
                        text = "Sign out",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            // =====================================================
            // VERTICAL SCROLL BAR
            // =====================================================

            if (scrollState.maxValue > 0) {

                val scrollFraction =
                    scrollState.value.toFloat() /
                            scrollState.maxValue.toFloat()

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(8.dp)
                        .padding(
                            top = 8.dp,
                            bottom = 8.dp,
                            end = 4.dp
                        )
                        .background(
                            color = GSDivider,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.25f)
                            .offset(
                                y = ((scrollState.maxValue * scrollFraction) / 4).dp
                            )
                            .background(
                                color = GSBluePrimary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }

    // =============================================================
    // SIGN OUT CONFIRMATION
    // =============================================================

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
                    "Are you sure you want to sign out of the administrator account?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
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


// ================================================================
// SECTION LABEL
// ================================================================

@Composable
private fun SectionLabel(
    text: String
) {

    Text(
        text = text,
        color = GSTextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}


// ================================================================
// ROW
// ================================================================

@Composable
private fun Row2(
    label: String,
    onClick: () -> Unit,
    trailingText: String? = null
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),

        horizontalArrangement = Arrangement.SpaceBetween,

        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            color = GSTextPrimary
        )

        trailingText?.let {

            Text(
                text = it,
                color = GSTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}


// ================================================================
// TWO FACTOR SCREEN
// ================================================================

@Composable
fun TwoFactorScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GSTextPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "Two-Factor Security",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary
            )
        }

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
        }
    }
}


// ================================================================
// LANGUAGE SCREEN
// ================================================================

@Composable
fun LanguageScreen(
    onBack: () -> Unit
) {

    var selected by remember {
        mutableStateOf("English (US)")
    }

    val options = listOf(
        "العربية (Arabic)",
        "English (US)",
        "Français"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 0.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GSTextPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )

            Text(
                text = "Language",
                style = MaterialTheme.typography.headlineMedium,
                color = GSTextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            options.forEach { option ->

                GSCard(
                    modifier = Modifier
                        .clickable {
                            selected = option
                        }
                        .padding(vertical = 4.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = option,
                            color = GSTextPrimary
                        )

                        RadioButton(
                            selected = selected == option,
                            onClick = {
                                selected = option
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = GSBluePrimary
                            )
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }
    }
}