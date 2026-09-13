package com.gstech.student.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AppNotification
import com.gstech.student.model.AttendanceSummary
import com.gstech.student.model.ClassSession
import com.gstech.student.model.Student
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class HomeData(
    val student: Student,
    val summary: AttendanceSummary,
    val todaysClasses: List<ClassSession>,
    val latestNotification: AppNotification?,
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val state: StateFlow<UiState<HomeData>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall {
                val student = container.profileRepository.getCurrentStudent()
                val summary = container.attendanceRepository.getSummary()
                val schedule = container.scheduleRepository.getWeeklySchedule()
                val todayName = LocalDate.now().dayOfWeek
                    .getDisplayName(TextStyle.FULL, Locale.FRENCH).lowercase()
                val today = schedule.filter { it.dayOfWeek.equals(todayName, ignoreCase = true) }
                    .sortedBy { it.startTime }
                val notifications = container.notificationsRepository.getNotifications()
                HomeData(student, summary, today, notifications.firstOrNull())
            }
        }
    }
}
