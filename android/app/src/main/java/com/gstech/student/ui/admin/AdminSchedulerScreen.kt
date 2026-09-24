package com.gstech.student.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.gstech.student.data.AppContainer
import com.gstech.student.data.remote.dto.AffectationDto
import com.gstech.student.data.remote.dto.ClassDto
import com.gstech.student.data.remote.dto.CreneauDto
import com.gstech.student.data.remote.dto.CreateCreneauRequest
import com.gstech.student.data.remote.dto.UpdateCreneauRequest
import com.gstech.student.ui.components.ErrorState
import com.gstech.student.ui.components.GSCard
import com.gstech.student.ui.components.LoadingState
import com.gstech.student.ui.theme.GSBackground
import com.gstech.student.ui.theme.GSBluePrimary
import com.gstech.student.ui.theme.GSDanger
import com.gstech.student.ui.theme.GSTextPrimary
import com.gstech.student.ui.theme.GSTextSecondary
import com.gstech.student.ui.theme.GSTeal
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import com.gstech.student.util.userFriendlyErrorMessage

private val schedulerDays =
    listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")

private val schedulerTimes = listOf("08:00", "10:30", "13:00", "15:30")
private val schedulerEnds = listOf("10:30", "13:00", "15:30", "18:00")

@Composable
fun AdminSchedulerScreen(container: AppContainer, onOpenClasses: (() -> Unit)? = null) {
    val repository = container.adminManagementRepository
    val scope = rememberCoroutineScope()

    var classes by remember { mutableStateOf<List<ClassDto>>(emptyList()) }
    var selectedClass by remember { mutableStateOf<ClassDto?>(null) }
    var classMenuExpanded by remember { mutableStateOf(false) }

    var assignments by remember {
        mutableStateOf<List<AffectationDto>>(emptyList())
    }

    var slots by remember {
        mutableStateOf<List<CreneauDto>>(emptyList())
    }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var editor by remember {
        mutableStateOf<SlotEditor?>(null)
    }

    var selectedSlot by remember {
        mutableStateOf<CreneauDto?>(null)
    }

    fun loadClasses() {
        scope.launch {
            loading = true
            error = null

            runCatching {
                repository.classes()
            }.onSuccess { result ->
                classes = result

                if (selectedClass == null && result.isNotEmpty()) {
                    selectedClass = result.first()
                }
            }.onFailure {
                error = userFriendlyErrorMessage(it) ?: "Unable to load classes."
            }

            loading = false
        }
    }

    fun loadClassSchedule(clazz: ClassDto) {
        scope.launch {
            loading = true
            error = null

            runCatching {
                val classAssignments =
                    repository.assignmentsForClass(clazz.idClasse)

                val classSlots =
                    repository.scheduleForClass(clazz.idClasse)

                classAssignments to classSlots
            }.onSuccess { result ->
                assignments = result.first
                slots = result.second
            }.onFailure {
                error = userFriendlyErrorMessage(it) ?: "Unable to load timetable."
            }

            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadClasses()
    }

    LaunchedEffect(selectedClass?.idClasse) {
        selectedClass?.let {
            loadClassSchedule(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GSBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Scheduler",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GSTextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedClass?.nomClasse ?: "Select a class",
                    style = MaterialTheme.typography.titleLarge,
                    color = GSTextPrimary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    text = "Click an empty cell to create a slot",
                    color = GSTextSecondary,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            if (onOpenClasses != null) {
                OutlinedButton(
                    onClick = onOpenClasses,
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text("Classes", color = GSTeal)
                }
            } else {
                Box {
                    OutlinedButton(
                        onClick = { classMenuExpanded = true },
                        shape = RoundedCornerShape(28.dp),
                    ) {
                        Text("Classes", color = GSTeal)
                    }
                    DropdownMenu(
                        expanded = classMenuExpanded,
                        onDismissRequest = { classMenuExpanded = false },
                    ) {
                        classes.forEach { clazz ->
                            DropdownMenuItem(
                                text = { Text(clazz.nomClasse) },
                                onClick = {
                                    selectedClass = clazz
                                    classMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        if (loading && classes.isEmpty()) {
            LoadingState()
            return@Column
        }

        error?.let { message ->
            ErrorState(message) {
                if (selectedClass == null) {
                    loadClasses()
                } else {
                    selectedClass?.let {
                        loadClassSchedule(it)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        if (classes.isEmpty()) {
            GSCard {
                Text(
                    text = "No classes have been created yet.",
                    color = GSTextSecondary,
                )
            }

            return@Column
        }

        Spacer(Modifier.height(12.dp))

        selectedClass?.let { clazz ->

            if (assignments.isEmpty()) {
                GSCard {
                    Text(
                        text = clazz.nomClasse,
                        style = MaterialTheme.typography.titleLarge,
                        color = GSTextPrimary,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Aucune affectation Formateur pour cette classe. Créez d’abord une affectation.",
                        color = GSTextSecondary,
                    )
                }
            } else {
                TimetableGrid(
                    slots = slots,
                    assignments = assignments,
                    onEmptyCell = { day, time ->
                        editor = SlotEditor(
                            slot = null,
                            day = day,
                            start = time,
                            end = schedulerEnds.getOrNull(schedulerTimes.indexOf(time)) ?: "18:00",
                            room = "",
                            assignmentId = null,
                        )
                    },
                    onSlotClick = {
                        selectedSlot = it
                    },
                )
            }
        }
    }

    editor?.let { currentEditor ->

        SlotEditorDialog(
            title = if (currentEditor.slot == null) {
                "Create timetable slot"
            } else {
                "Edit timetable slot"
            },
            editor = currentEditor,
            assignments = assignments,
            onDismiss = {
                editor = null
            },
            onSave = { values ->

                val clazz =
                    selectedClass
                        ?: return@SlotEditorDialog

                val assignmentId =
                    values.assignmentId
                        ?: run {
                            error = "Please select a course and teacher."
                            return@SlotEditorDialog
                        }

                scope.launch {

                    runCatching {

                        if (values.slot == null) {

                            val conflict =
                                repository.hasScheduleConflict(
                                    clazz.idClasse,
                                    values.day,
                                    values.start,
                                    values.end,
                                )

                            check(!conflict) {
                                "This class already has a timetable slot during this period."
                            }

                            repository.createSlot(
                                CreateCreneauRequest(
                                    jourSemaine = values.day,
                                    heureDebut = values.start,
                                    heureFin = values.end,
                                    salle = values.room,
                                    idAffectation = assignmentId,
                                )
                            )

                        } else {

                            val conflict =
                                repository.hasScheduleConflict(
                                    clazz.idClasse,
                                    values.day,
                                    values.start,
                                    values.end,
                                    values.slot.idCreneau,
                                )

                            check(!conflict) {
                                "This class already has another slot during this period."
                            }

                            repository.updateSlot(
                                values.slot.idCreneau,
                                UpdateCreneauRequest(
                                    jourSemaine = values.day,
                                    heureDebut = values.start,
                                    heureFin = values.end,
                                    salle = values.room,
                                    idAffectation = assignmentId,
                                ),
                            )
                        }

                    }.onSuccess {

                        editor = null
                        loadClassSchedule(clazz)

                    }.onFailure {
                        error =
                            userFriendlyErrorMessage(it)
                    }
                }
            },
        )
    }

    selectedSlot?.let { slot ->

        ExistingSlotDialog(
            slot = slot,
            assignments = assignments,
            onDismiss = {
                selectedSlot = null
            },
            onEdit = {

                val assignmentId =
                    slot.idAffectation
                        ?: slot.affectation?.idAffectation

                if (assignmentId == null) {
                    error = "This timetable slot has no assignment."
                    selectedSlot = null
                    return@ExistingSlotDialog
                }

                selectedSlot = null

                editor = SlotEditor(
                    slot = slot,
                    day = slot.jourSemaine,
                    start = slot.heureDebut,
                    end = slot.heureFin,
                    room = slot.salle.orEmpty(),
                    assignmentId = assignmentId,
                )
            },
            onDelete = {

                selectedSlot = null

                scope.launch {

                    runCatching {
                        repository.deleteSlot(
                            slot.idCreneau
                        )
                    }.onSuccess {
                        selectedClass?.let {
                            loadClassSchedule(it)
                        }
                    }.onFailure {
                        error =
                            userFriendlyErrorMessage(it)
                    }
                }
            },
        )
    }
}

private data class SlotEditor(
    val slot: CreneauDto?,
    val day: String,
    val start: String,
    val end: String,
    val room: String,
    val assignmentId: String?,
)

private fun nextHour(time: String): String {
    val parts = time.split(":")

    val hour =
        parts
            .firstOrNull()
            ?.toIntOrNull()
            ?: 8

    return "%02d:00".format(
        (hour + 1).coerceAtMost(23)
    )
}

@Composable
private fun ClassSelector(
    classes: List<ClassDto>,
    selected: ClassDto?,
    onSelected: (ClassDto) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = selected?.nomClasse ?: "Select class",
                modifier = Modifier.weight(1f),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {

            classes.forEach { clazz ->

                DropdownMenuItem(
                    text = {
                        Text(clazz.nomClasse)
                    },
                    onClick = {
                        onSelected(clazz)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TimetableGrid(
    slots: List<CreneauDto>,
    assignments: List<AffectationDto>,
    onEmptyCell: (String, String) -> Unit,
    onSlotClick: (CreneauDto) -> Unit,
) {
    val horizontal = rememberScrollState()
    val slotHeight = 108.dp
    val dayWidth = 128.dp

    Box(Modifier.fillMaxWidth().horizontalScroll(horizontal)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(68.dp).height(44.dp))
                schedulerDays.forEach { day ->
                    Box(Modifier.width(dayWidth).height(44.dp), contentAlignment = Alignment.Center) {
                        Text(day.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = GSTextPrimary)
                    }
                }
            }

            schedulerTimes.forEachIndexed { index, time ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.width(68.dp).height(slotHeight), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(time, fontSize = 11.sp, color = GSTextSecondary, modifier = Modifier.padding(top = 12.dp))
                        Text(schedulerEnds[index], fontSize = 11.sp, color = GSTextSecondary)
                    }

                    schedulerDays.forEach { day ->
                        val slot = slots.firstOrNull {
                            it.jourSemaine.equals(day, ignoreCase = true) && it.heureDebut.take(5) == time
                        }

                        if (slot == null) {
                            OutlinedButton(
                                onClick = { onEmptyCell(day, time) },
                                modifier = Modifier.width(dayWidth).height(slotHeight).padding(4.dp),
                                contentPadding = PaddingValues(2.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GSTeal),
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                        } else {
                            val resolvedAssignment = slot.affectation
                                ?: assignments.firstOrNull { it.idAffectation == slot.idAffectation }
                            val course = slot.cours?.nomCours
                                ?: resolvedAssignment?.cours?.nomCours
                                ?: "Course"
                            val teacher = resolvedAssignment?.formateur?.utilisateur
                                ?.let { "${it.prenom} ${it.nom}".trim() }
                                ?.takeIf { it.isNotBlank() }
                                ?: slot.cours?.formateurNom
                                ?: "Formateur"
                            val room = slot.salle?.takeIf { it.isNotBlank() }
                                ?: slot.cours?.salle?.takeIf { it.isNotBlank() }
                                ?: "—"

                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = GSTeal,
                                modifier = Modifier.width(dayWidth).height(slotHeight).padding(4.dp),
                                onClick = { onSlotClick(slot) },
                            ) {
                                Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.Center) {
                                    Text(course, color = Color(0xFF0B3D62), fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(room, color = Color(0xFF0B3D62).copy(alpha = 0.9f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(teacher, color = Color(0xFF0B3D62).copy(alpha = 0.85f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotEditorDialog(
    title: String,
    editor: SlotEditor,
    assignments: List<AffectationDto>,
    onDismiss: () -> Unit,
    onSave: (SlotEditor) -> Unit,
) {
    var day by remember(editor) {
        mutableStateOf(editor.day)
    }

    var start by remember(editor) {
        mutableStateOf(editor.start)
    }

    var end by remember(editor) {
        mutableStateOf(editor.end)
    }

    var room by remember(editor) {
        mutableStateOf(editor.room)
    }

    var assignmentId by remember(editor) {
        mutableStateOf(editor.assignmentId)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedAssignment =
        assignments.firstOrNull {
            it.idAffectation == assignmentId
        }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(title)
        },

        text = {

            Column {

                AssignmentSelector(
                    assignments = assignments,
                    selected = selectedAssignment,
                    expanded = expanded,
                    onExpand = {
                        expanded = true
                    },
                    onDismiss = {
                        expanded = false
                    },
                    onSelected = {
                        assignmentId =
                            it.idAffectation

                        expanded = false
                    },
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = day,
                    onValueChange = {
                        day = it.lowercase()
                    },
                    label = {
                        Text("Day")
                    },
                    singleLine = true,
                
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

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = start,
                    onValueChange = {
                        start = it
                    },
                    label = {
                        Text("Start HH:mm")
                    },
                    singleLine = true,
                
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

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = end,
                    onValueChange = {
                        end = it
                    },
                    label = {
                        Text("End HH:mm")
                    },
                    singleLine = true,
                
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

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = {
                        room = it
                    },
                    label = {
                        Text("Room")
                    },
                    singleLine = true,
                
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
        },

        confirmButton = {

            TextButton(
                enabled =
                    assignmentId != null &&
                            room.isNotBlank() &&
                            day.isNotBlank() &&
                            start.isNotBlank() &&
                            end.isNotBlank(),

                onClick = {

                    onSave(
                        editor.copy(
                            day = day,
                            start = start,
                            end = end,
                            room = room.trim(),
                            assignmentId = assignmentId,
                        )
                    )
                },
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun AssignmentSelector(
    assignments: List<AffectationDto>,
    selected: AffectationDto?,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelected: (AffectationDto) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedButton(
            onClick = onExpand,
            modifier = Modifier.fillMaxWidth(),
            enabled = assignments.isNotEmpty(),
        ) {
            Text(
                text = assignmentLabel(selected),
                modifier = Modifier.weight(1f),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
        ) {

            assignments.forEach { assignment ->

                DropdownMenuItem(
                    text = {
                        Text(
                            assignmentLabel(
                                assignment
                            )
                        )
                    },
                    onClick = {
                        onSelected(assignment)
                    },
                )
            }
        }
    }
}

private fun assignmentLabel(
    assignment: AffectationDto?
): String {

    if (assignment == null) {
        return "Select course + teacher"
    }

    val course =
        assignment.cours?.nomCours
            ?: assignment.idCours

    val teacher =
        assignment.formateur
            ?.utilisateur
            ?.let {
                "${it.prenom} ${it.nom}".trim()
            }
            ?: assignment.idFormateur

    return "$course • $teacher"
}

@Composable
private fun ExistingSlotDialog(
    slot: CreneauDto,
    assignments: List<AffectationDto>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val assignment =
        assignments.firstOrNull {
            it.idAffectation ==
                    (
                            slot.idAffectation
                                ?: slot.affectation?.idAffectation
                            )
        }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                slot.cours?.nomCours
                    ?: slot.affectation
                        ?.cours
                        ?.nomCours
                    ?: "Scheduled course"
            )
        },

        text = {
            Column {

                Text(
                    text =
                        "${slot.jourSemaine} • " +
                                "${slot.heureDebut}–" +
                                slot.heureFin,
                    color = GSTextPrimary,
                )

                Text(
                    text =
                        "${slot.salle.orEmpty()} • " +
                                assignmentLabel(assignment),
                    color = GSTextSecondary,
                )
            }
        },

        confirmButton = {
            TextButton(
                onClick = onEdit
            ) {
                Text("Edit")
            }
        },

        dismissButton = {
            Row {

                TextButton(
                    onClick = onDelete
                ) {
                    Text(
                        "Delete",
                        color = GSDanger
                    )
                }

                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Close")
                }
            }
        },
    )
}
