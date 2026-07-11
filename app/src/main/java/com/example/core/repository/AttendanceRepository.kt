package com.example.core.repository

import com.example.core.database.dao.AttendanceDao
import com.example.core.database.entities.AttendanceEntity
import com.example.core.network.AttendanceRecordDto
import com.example.core.network.CamsApiService
import com.example.features.attendance.models.AttendanceRecord
import com.example.features.attendance.models.AttendanceSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

interface AttendanceRepository {
    fun getAttendanceFlow(): Flow<AttendanceSummary?>
    suspend fun refreshAttendance()
}

class OfflineFirstAttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val apiService: CamsApiService
) : AttendanceRepository {

    override fun getAttendanceFlow(): Flow<AttendanceSummary?> {
        return attendanceDao.getAll().map { entities ->
            if (entities.isEmpty()) return@map null
            
            val records = entities.map { it.toDomainModel() }
            val total = records.size
            val present = records.count { it.status == "present" }
            val absent = records.count { it.status == "absent" }
            val od = records.count { it.status == "od" }
            val percentage = if (total > 0) {
                ((present + od).toDouble() / total * 100)
            } else 0.0

            AttendanceSummary(
                percentage = Math.round(percentage * 10) / 10.0,
                total = total,
                present = present,
                absent = absent,
                od = od,
                records = records.sortedByDescending { it.date }
            )
        }
    }

    override suspend fun refreshAttendance() {
        try {
            val response = apiService.getAttendance()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val dtos = body.records
                    attendanceDao.deleteAll()
                    attendanceDao.insertAll(dtos.map { it.toEntity() })
                }
            }
        } catch (e: Exception) {
            // Log error or handle failure
        }
    }

    private fun AttendanceEntity.toDomainModel() = AttendanceRecord(
        id = id,
        date = date ?: "",
        status = status ?: "absent",
        subjectName = "Subject Name", // In real app, join with courses table
        subjectCode = "SUB001",
        sectionName = "A"
    )

    private fun AttendanceRecordDto.toEntity() = AttendanceEntity(
        id = id,
        studentId = null, // Set based on session
        sectionId = null,
        date = date,
        status = status,
        createdAt = null,
        updatedAt = null,
        isDeleted = false,
        deletedAt = null
    )
}
