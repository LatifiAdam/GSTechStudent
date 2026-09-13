package com.gstech.student.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.AppContainer
import com.gstech.student.data.repository.CourseAnnouncement
import com.gstech.student.model.ClassSession
import com.gstech.student.util.UiState
import com.gstech.student.util.safeCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CourseDetailData(
    val session: ClassSession?,
    val attendancePercent: Int,
    val attended: Int,
    val total: Int,
    val announcements: List<CourseAnnouncement>,
)

class CourseDetailViewModel(
    private val container: AppContainer,
    private val courseId: String,
    private val courseName: String,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CourseDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<CourseDetailData>> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = safeCall {
                val schedule = container.scheduleRepository.getWeeklySchedule()
                val session = schedule.firstOrNull { it.courseId == courseId || it.courseName == courseName }
                val (pct, attendedTotal) = container.attendanceRepository.getCourseAttendance(courseId, courseName)
                val announcements = runCatching { container.courseRepository.getAnnouncementsForCourse(courseId) }
                    .getOrDefault(emptyList())
                CourseDetailData(session, pct, attendedTotal.first, attendedTotal.second, announcements)
            }
        }
    }
}

