package com.gstech.student.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AttendanceRecord
import com.gstech.student.model.AttendanceStatus
import com.gstech.student.model.AttendanceSummary
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AttendanceFilter { ALL, PRESENT, ABSENT, LATE }

data class AttendanceData(
    val summary: AttendanceSummary,
    val history: List<AttendanceRecord>,
)

class AttendanceViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<AttendanceData>>(UiState.Loading)
    val state: StateFlow<UiState<AttendanceData>> = _state.asStateFlow()

    private val _filter = MutableStateFlow(AttendanceFilter.ALL)
    val filter: StateFlow<AttendanceFilter> = _filter.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall {
                val summary = container.attendanceRepository.getSummary()
                val history = container.attendanceRepository.getHistory()
                AttendanceData(summary, history)
            }
        }
    }

    fun setFilter(f: AttendanceFilter) { _filter.value = f }

    fun filtered(history: List<AttendanceRecord>): List<AttendanceRecord> = when (_filter.value) {
        AttendanceFilter.ALL -> history
        AttendanceFilter.PRESENT -> history.filter { it.status == AttendanceStatus.PRESENT }
        AttendanceFilter.ABSENT -> history.filter { it.status == AttendanceStatus.ABSENT }
        AttendanceFilter.LATE -> history.filter { it.status == AttendanceStatus.LATE }
    }

    fun monthlyTrend(history: List<AttendanceRecord>): List<Pair<String, Int>> {
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val counts = LinkedHashMap<String, Int>()
        history.forEach { record ->
            if (record.status != AttendanceStatus.ABSENT) return@forEach
            val iso = record.dateTimeIso ?: return@forEach
            val monthIndex = runCatching { iso.substring(5, 7).toInt() - 1 }.getOrNull() ?: return@forEach
            if (monthIndex !in months.indices) return@forEach
            val key = months[monthIndex]
            counts[key] = (counts[key] ?: 0) + 1
        }
        return counts.entries.map { it.key to it.value }
    }
}
