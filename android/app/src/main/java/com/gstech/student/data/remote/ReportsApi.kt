package com.gstech.student.data.remote

import com.gstech.student.data.remote.dto.AttendanceReportDto
import com.gstech.student.data.remote.dto.RecentActivityDto
import com.gstech.student.data.remote.dto.StudentReportDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportsApi {

    @GET("reports/stagieres/{id}")
    suspend fun getStudentReport(
        @Path("id") studentId: String
    ): StudentReportDto

    @GET("reports/attendance")
    suspend fun getAttendanceReport(
        @Query("periode") periode: String? = null
    ): AttendanceReportDto

    @GET("reports/recent-activity")
    suspend fun getRecentActivity(
        @Query("limit") limit: Int = 10
    ): List<RecentActivityDto>
}