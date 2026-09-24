package com.gstech.student.data.repository

import com.gstech.student.data.remote.GradingApi
import com.gstech.student.data.remote.dto.StudentGradeDto

class GradingRepository(private val api: GradingApi) {
    suspend fun getMyGrades(): List<StudentGradeDto> = api.studentGrades()
}
