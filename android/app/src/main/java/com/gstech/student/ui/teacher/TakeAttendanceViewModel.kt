package com.gstech.student.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AttendanceCall
import com.gstech.student.model.AttendanceStatus
import com.gstech.student.model.RosterStudent
import com.gstech.student.util.UiState
import com.gstech.student.util.userFriendlyErrorMessage
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TakeAttendanceViewModel(private val container: AppContainer, private val creneauId: String) : ViewModel() {

    private val _state = MutableStateFlow<UiState<AttendanceCall>>(UiState.Loading)
    val state: StateFlow<UiState<AttendanceCall>> = _state.asStateFlow()

    private val _submitted = MutableStateFlow(false)
    val submitted: StateFlow<Boolean> = _submitted.asStateFlow()

    init { openCall() }

    fun openCall() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall { container.teacherRepository.openAttendanceCall(creneauId) }
        }
    }

    fun setStatus(studentId: String, status: AttendanceStatus) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        val updated = current.roster.map { if (it.id == studentId) it.copy(status = status) else it }
        _state.value = UiState.Success(current.copy(roster = updated))
    }

    fun markAllPresent() {
        val current = (_state.value as? UiState.Success)?.data ?: return
        _state.value = UiState.Success(current.copy(roster = current.roster.map { it.copy(status = AttendanceStatus.PRESENT) }))
    }

    fun submit() {
        val current = (_state.value as? UiState.Success)?.data ?: return

        // The backend locks an attendance call after validation (RG3).
        // If the teacher reopened an already validated call, do not send
        // POST /validate again; simply leave the screen.
        if (current.validated) {
            _submitted.value = true
            return
        }

        viewModelScope.launch {
            runCatching { container.teacherRepository.submitAttendance(current.id, current.roster) }
                .onSuccess { _submitted.value = true }
                .onFailure { error ->
                    _state.value = UiState.Error(userFriendlyErrorMessage(error))
                }
        }
    }

    fun counts(roster: List<RosterStudent>): Triple<Int, Int, Int> = Triple(
        roster.count { it.status == AttendanceStatus.PRESENT },
        roster.count { it.status == AttendanceStatus.ABSENT },
        roster.count { it.status == AttendanceStatus.LATE },
    )
}
