package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.CreateUserRequest
import com.gstech.student.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ArrowDropDown
import kotlin.math.max
import com.gstech.student.util.userFriendlyErrorMessage

@Composable
fun AddUserScreen(
    container: AppContainer,
    onDone: () -> Unit,
    allowedRoles: Set<String> = setOf("superadmin", "df", "srio", "scq", "directeur", "gestionnaire", "formateur", "stagiaire")
) {
    var userId by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(if (allowedRoles.contains("superadmin")) "superadmin" else allowedRoles.firstOrNull() ?: "stagiaire") }
    var numeroEtudiant by remember { mutableStateOf("") }
    var promotion by remember { mutableStateOf("") }
    var module by remember { mutableStateOf("") }
    var niveauAcces by remember { mutableStateOf(if (allowedRoles.contains("superadmin")) "technical" else "") }
    var region by remember { mutableStateOf("") }
    var idEtablissement by remember { mutableStateOf("") }
    var cin by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // =========================
            // HEADER
            // =========================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 20.dp, 20.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onDone
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GSTextPrimary
                    )
                }

                Text(
                    "Add User",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GSTextPrimary
                )
            }

            // =========================
            // SCROLLABLE CONTENT
            // =========================

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, top = 20.dp, end = 24.dp, bottom = 20.dp)
                    .verticalScroll(scrollState)
            ) {

                // =========================
                // ROLE
                // =========================

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 20.dp)
                ) {
                    val labels = listOf(
                        "superadmin" to "Super Admin", "df" to "DF", "srio" to "SRIO", "scq" to "SCQ", "directeur" to "Directeur",
                        "gestionnaire" to "Gestionnaire", "formateur" to "Formateur", "stagiaire" to "Stagiaire"
                    )
                    labels.filter { allowedRoles.contains(it.first) }.forEach { (value, label) ->
                        item { RoleChip(label = label, selected = role == value) { role = value } }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // =========================
                // BASIC INFORMATION
                // =========================

                field(
                    label = "User ID (optional)",
                    value = userId,
                    onChange = { userId = it }
                )

                field(
                    label = "First name",
                    value = prenom,
                    onChange = { prenom = it }
                )

                field(
                    label = "Last name",
                    value = nom,
                    onChange = { nom = it }
                )

                field(
                    label = "Email",
                    value = email,
                    onChange = { email = it }
                )

                field(
                    label = "Temporary password",
                    value = password,
                    onChange = { password = it }
                )

                // =========================
                // PERSONAL INFORMATION
                // =========================

                Spacer(Modifier.height(4.dp))

                Text(
                    "Personal Information",
                    color = GSTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(10.dp))

                field(
                    label = "CIN",
                    value = cin,
                    onChange = { cin = it }
                )

                field(
                    label = "Telephone",
                    value = telephone,
                    onChange = { telephone = it }
                )

                field(
                    label = "Address",
                    value = adresse,
                    onChange = { adresse = it }
                )

                // =========================
                // ROLE-SPECIFIC INFORMATION
                // =========================

                @OptIn(ExperimentalMaterial3Api::class)
                when (role) {
                    "srio", "scq" -> {
                        val regions = listOf(
                            "Rabat-Salé-Kénitra",
                            "Casablanca-Settat",
                            "Tanger-Tétouan-Al Hoceïma",
                            "Fès-Meknès",
                            "Marrakech-Safi",
                            "Oriental",
                            "Béni Mellal-Khénifra",
                            "Drâa-Tafilalet",
                            "Souss-Massa",
                            "Guelmim-Oued Noun"
                        )
                        var regionExpanded by remember { mutableStateOf(false) }

                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = regionExpanded,
                            onExpandedChange = { regionExpanded = !regionExpanded }
                        ) {
                            OutlinedTextField(
                                value = region.ifBlank { "Sélectionner une région" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Région") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(regionExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = regionExpanded,
                                onDismissRequest = { regionExpanded = false }
                            ) {
                                regions.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            region = r
                                            regionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "L'affectation à un EFP se fait ensuite depuis l'onglet EFP régional.",
                            color = GSTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    "stagiaire" -> {

                        field(
                            label = "Student number",
                            value = numeroEtudiant,
                            onChange = { numeroEtudiant = it }
                        )

                        field(
                            label = "Promotion",
                            value = promotion,
                            onChange = { promotion = it }
                        )
                    }

                    "formateur" -> {
                        field(label = "Subject (module)", value = module, onChange = { module = it })
                    }
                    "superadmin", "admin" -> {
                        field(label = "Access level", value = niveauAcces, onChange = { niveauAcces = it })
                    }
                }

                // =========================
                // ERROR
                // =========================

                error?.let {

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = it,
                        color = GSDanger,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // =========================
                // CREATE USER
                // =========================

                Button(
                    onClick = {

                        val studentFieldsValid = role != "stagiaire" ||
                            (numeroEtudiant.isNotBlank() && promotion.isNotBlank())

                        if (!studentFieldsValid) {
                            error = "Student number and promotion are required for a Stagiaire."
                            return@Button
                        }

                        saving = true
                        error = null

                        scope.launch {

                            runCatching {

                                container.adminRepository.createUser(
                                    CreateUserRequest(
                                        idUtilisateur = userId.ifBlank { null },
                                        nom = nom,
                                        prenom = prenom,
                                        email = email,
                                        password = password,
                                        role = role,

                                        module = module.ifBlank {
                                            null
                                        },

                                        numeroEtudiant = numeroEtudiant.trim(),

                                        promotion = promotion.trim(),

                                        region = region.ifBlank { null },

                                        niveauAcces = niveauAcces.ifBlank {
                                            null
                                        },

                                        cin = cin.ifBlank {
                                            null
                                        },

                                        telephone = telephone.ifBlank {
                                            null
                                        },

                                        adresse = adresse.ifBlank {
                                            null
                                        },
                                        idEtablissement = idEtablissement.ifBlank { null }
                                    )
                                )

                            }.onSuccess {

                                saving = false
                                onDone()

                            }.onFailure {

                                saving = false
                                error = userFriendlyErrorMessage(it)
                                    ?: "Failed to create user."
                            }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                    shape = RoundedCornerShape(26.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = GSBluePrimary
                    ),

                    enabled =
                        !saving &&
                                nom.isNotBlank() &&
                                prenom.isNotBlank() &&
                                email.isNotBlank() &&
                                password.length >= 8 &&
                                (role != "stagiaire" || (numeroEtudiant.isNotBlank() && promotion.isNotBlank()))
                ) {

                    if (saving) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            "Create User",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(30.dp))
            }
        }

        // =========================
        // RIGHT SCROLL BAR
        // =========================

        if (scrollState.maxValue > 0) {

            RightScrollbar(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}


// ============================================================
// RIGHT SCROLLBAR
// ============================================================

@Composable
private fun RightScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {

    val maxScroll = max(1, scrollState.maxValue)

    Box(
        modifier = modifier
            .width(8.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
    ) {

        // Track

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = GSDivider,
                    shape = RoundedCornerShape(50)
                )
        )

        // Thumb

        val viewportHeight = scrollState.viewportSize.toFloat()
        val contentHeight =
            (scrollState.maxValue + scrollState.viewportSize).toFloat()

        val thumbRatio =
            if (contentHeight > 0f) {
                (viewportHeight / contentHeight)
                    .coerceIn(0.1f, 1f)
            } else {
                1f
            }


        val thumbPosition =
            scrollState.value.toFloat() / maxScroll.toFloat()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(thumbRatio)
                .offset(
                    y = 0.dp
                )
                .drawWithContent {

                    drawContent()

                    val availableHeight =
                        size.height / thumbRatio - size.height

                    val y =
                        availableHeight *
                                thumbPosition

                    drawRoundRect(
                        color = GSBluePrimary,
                        topLeft = Offset(
                            0f,
                            y
                        ),
                        size = size.copy(
                            height = size.height
                        ),
                        cornerRadius = CornerRadius(
                            size.width,
                            size.width
                        )
                    )
                }
        )
    }
}


// ============================================================
// FIELD
// ============================================================

@Composable
private fun field(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {

    Text(
        label,
        color = GSTextSecondary,
        fontSize = 13.sp
    )

    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    
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

    Spacer(Modifier.height(10.dp))
}


// ============================================================
// ROLE CHIP
// ============================================================

@Composable
private fun RoleChip(
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
            label,
            color = if (selected) {
                Color.White
            } else {
                GSTextPrimary
            },

            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
        )
    }
}
