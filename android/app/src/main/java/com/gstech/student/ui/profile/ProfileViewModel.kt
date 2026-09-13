package com.gstech.student.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.gstech.student.data.AppContainer
import com.gstech.student.model.AttendanceSummary
import com.gstech.student.model.Student
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileData(val student: Student, val summary: AttendanceSummary)

// GPA / earned-credits: the cahier des charges explicitly excludes academic
// grading from this project's scope (section 2.2 "Ce que le projet ne
// couvre pas" — "La saisie et le calcul des notes... restent hors
// périmètre"). There is no backend field for these, so they're shown as
// static placeholders here until a grades module/endpoint exists.
data class AcademicPlaceholder(val gpa: String = "—", val credits: String = "—")


class ProfileViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ProfileData>>(UiState.Loading)
    val state: StateFlow<UiState<ProfileData>> = _state.asStateFlow()

    var darkMode by androidx.compose.runtime.mutableStateOf(false)
        private set

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall {
                val student = container.profileRepository.getCurrentStudent()
                val summary = container.attendanceRepository.getSummary()
                ProfileData(student, summary)
            }
        }
    }

    fun toggleDarkMode() { darkMode = !darkMode }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            container.authRepository.logout()
            onDone()
        }
    }
}
