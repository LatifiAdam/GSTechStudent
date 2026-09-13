package com.gstech.student.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.JustificationItem
import com.gstech.student.model.JustificationStatus
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JustificationsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<JustificationItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<JustificationItem>>> = _state.asStateFlow()

    private val _filter = MutableStateFlow(JustificationStatus.PENDING)
    val filter: StateFlow<JustificationStatus> = _filter.asStateFlow()

    init { load() }

    fun setFilter(status: JustificationStatus) {
        _filter.value = status
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall { container.teacherRepository.getJustifications(_filter.value) }
        }
    }

    fun approve(item: JustificationItem) {
        viewModelScope.launch {
            runCatching { container.teacherRepository.approve(item.id) }
            load()
        }
    }

    fun reject(item: JustificationItem, motif: String) {
        viewModelScope.launch {
            runCatching { container.teacherRepository.reject(item.id, motif) }
            load()
        }
    }
}
