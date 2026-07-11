package com.example.core.repository

import com.example.features.parent.models.*
import kotlinx.coroutines.flow.Flow

interface ParentRepository {
    suspend fun getChildProfile(): ChildProfileExtended
    suspend fun getInternalMarks(): List<ChildInternalMark>
    suspend fun getPerformanceAnalytics(): List<PerformanceData>
    suspend fun getFeeStatus(): ChildFeeLedger
    suspend fun getNotices(): List<CollegeNotice>
    suspend fun getAttendance(): List<AttendanceRecord>
    suspend fun getSubjectAttendance(): List<SubjectAttendance>
    suspend fun getTimetable(): List<TimetableDay>
}
