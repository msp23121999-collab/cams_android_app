package com.example.core.repository

import com.example.features.parent.models.*
import kotlinx.coroutines.flow.Flow

interface ParentRepository {
    val selectedChildId: Flow<String?>
    suspend fun setSelectedChildId(childId: String)
    suspend fun getChildrenList(): List<ChildSummary>
    suspend fun getChildProfile(childId: String? = null): ChildProfileExtended
    suspend fun getInternalMarks(childId: String? = null): List<ChildInternalMark>
    suspend fun getPerformanceAnalytics(childId: String? = null): List<PerformanceData>
    suspend fun getFeeStatus(childId: String? = null): ChildFeeLedger
    suspend fun getNotices(childId: String? = null): List<CollegeNotice>
    suspend fun getAttendance(childId: String? = null): List<AttendanceRecord>
    suspend fun getSubjectAttendance(childId: String? = null): List<SubjectAttendance>
    suspend fun getTimetable(childId: String? = null): List<TimetableDay>
    suspend fun changePassword(currentPassword: String, newPassword: String)
}
