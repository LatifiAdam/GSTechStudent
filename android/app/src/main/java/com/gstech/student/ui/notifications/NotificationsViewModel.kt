package com.gstech.student.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AppNotification
import com.gstech.student.model.NotificationCategory
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NotifFilter { ALL, ACADEMIC, ADMIN, URGENT }

class NotificationsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<AppNotification>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AppNotification>>> = _state.asStateFlow()

    private val _filter = MutableStateFlow(NotifFilter.ALL)
    val filter: StateFlow<NotifFilter> = _filter.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall { container.notificationsRepository.getNotifications() }
        }
    }

    fun setFilter(f: NotifFilter) { _filter.value = f }

    fun filtered(list: List<AppNotification>): List<AppNotification> = when (_filter.value) {
        NotifFilter.ALL -> list
        NotifFilter.ACADEMIC -> list.filter { it.category == NotificationCategory.ACADEMIC }
        NotifFilter.ADMIN -> list.filter { it.category == NotificationCategory.ADMIN }
        NotifFilter.URGENT -> list.filter { it.category == NotificationCategory.URGENT }
    }

    fun markRead(notification: AppNotification) {
        if (notification.read) return
        viewModelScope.launch {
            runCatching { container.notificationsRepository.markRead(notification.id) }
            val current = (_state.value as? UiState.Success)?.data ?: return@launch
            _state.value = UiState.Success(current.map { if (it.id == notification.id) it.copy(read = true) else it })
        }
    }
}
