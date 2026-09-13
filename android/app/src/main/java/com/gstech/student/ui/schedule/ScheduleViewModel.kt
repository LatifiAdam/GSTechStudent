package com.gstech.student.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.gstech.student.data.AppContainer
import com.gstech.student.model.ClassSession
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class DayTab(val date: LocalDate, val apiDayName: String, val shortLabel: String, val dayNumber: String)

class ScheduleViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<ClassSession>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ClassSession>>> = _state.asStateFlow()

    // French day names, matching CRENEAU.jour_semaine ENUM in the DDL (Phase 2 section 2.2).
    private val frenchDayNames = mapOf(
        DayOfWeek.MONDAY to "lundi", DayOfWeek.TUESDAY to "mardi", DayOfWeek.WEDNESDAY to "mercredi",
        DayOfWeek.THURSDAY to "jeudi", DayOfWeek.FRIDAY to "vendredi", DayOfWeek.SATURDAY to "samedi",
        DayOfWeek.SUNDAY to "dimanche",
    )
    private val shortLabels = mapOf(
        DayOfWeek.MONDAY to "Mon", DayOfWeek.TUESDAY to "Tue", DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu", DayOfWeek.FRIDAY to "Fri", DayOfWeek.SATURDAY to "Sat",
        DayOfWeek.SUNDAY to "Sun",
    )

    val weekDays: List<DayTab> = run {
        val monday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        (0..5).map { offset ->
            val date = monday.plusDays(offset.toLong())
            DayTab(date, frenchDayNames.getValue(date.dayOfWeek), shortLabels.getValue(date.dayOfWeek), date.dayOfMonth.toString())
        }
    }

    var selectedDay by androidx.compose.runtime.mutableStateOf(
        weekDays.firstOrNull { it.date == LocalDate.now() } ?: weekDays.first(),
    )
        private set

    private var fullSchedule: List<ClassSession> = emptyList()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val result = safeCall { container.scheduleRepository.getWeeklySchedule() }
            if (result is UiState.Success) fullSchedule = result.data
            _state.value = result
        }
    }

    fun selectDay(day: DayTab) { selectedDay = day }

    fun sessionsFor(day: DayTab): List<ClassSession> =
        fullSchedule.filter { it.dayOfWeek.equals(day.apiDayName, ignoreCase = true) }
            .sortedBy { it.startTime }
}
