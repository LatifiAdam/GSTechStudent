package com.gstech.student.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.TeacherCourse
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyCoursesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<TeacherCourse>>>(UiState.Loading)
    val state: StateFlow<UiState<List<TeacherCourse>>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall { container.teacherRepository.getMyCourses() }
        }
    }
}
