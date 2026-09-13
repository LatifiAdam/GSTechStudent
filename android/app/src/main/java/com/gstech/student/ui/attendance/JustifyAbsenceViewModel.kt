package com.gstech.student.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AttendanceRecord
import com.gstech.student.model.AttendanceStatus
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JustifyAbsenceViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<AttendanceRecord>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AttendanceRecord>>> = _state.asStateFlow()

    private val _submitted = MutableStateFlow(false)
    val submitted: StateFlow<Boolean> = _submitted.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall {
                container.attendanceRepository.getHistory()
                    .filter { it.status == AttendanceStatus.ABSENT || it.status == AttendanceStatus.LATE }
            }
        }
    }

    fun submit(presenceId: String, motif: String, onDone: () -> Unit) {
        if (presenceId.isBlank() || motif.isBlank()) return
        _submitting.value = true
        viewModelScope.launch {
            runCatching { container.attendanceRepository.submitJustification(presenceId, motif, null) }
                .onSuccess { _submitting.value = false; _submitted.value = true; onDone() }
                .onFailure { _submitting.value = false }
        }
    }
}
