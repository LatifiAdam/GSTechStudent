package com.gstech.student.data.remote

import com.gstech.student.data.remote.dto.StudentGradeDto
import retrofit2.http.GET

interface GradingApi {
    @GET("grading/student")
    suspend fun studentGrades(): List<StudentGradeDto>
}
