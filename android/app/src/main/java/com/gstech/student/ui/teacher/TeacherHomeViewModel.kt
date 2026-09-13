package com.gstech.student.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.model.JustificationStatus
import com.gstech.student.model.TeacherCourse
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherHomeData(
    val name: String,
    val department: String,
    val courses: List<TeacherCourse>,
    val pendingJustifications: Int,
)

class TeacherHomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TeacherHomeData>>(UiState.Loading)
    val state: StateFlow<UiState<TeacherHomeData>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall {
                val userId = container.tokenManager.userIdNow() ?: error("No signed-in user.")
                val userDto = container.profileRepository.rawUser(userId)
                val courses = container.teacherRepository.getMyCourses()
                val pending = container.teacherRepository.getJustifications(JustificationStatus.PENDING).size
                TeacherHomeData(
                    name = "${userDto.prenom} ${userDto.nom}",
                    department = "Department of Computer Science",
                    courses = courses,
                    pendingJustifications = pending,
                )
            }
        }
    }
}
