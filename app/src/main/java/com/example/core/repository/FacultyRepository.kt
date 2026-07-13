package com.example.core.repository

import com.example.features.faculty.models.*
import com.example.features.parent.models.CollegeNotice
import com.example.features.parent.models.TimetableDay
import com.example.core.network.*

interface FacultyRepository {
    suspend fun getDashboardMetrics(): FacultyDashboardMetrics
    suspend fun getAssignedSubjects(): List<FacultySubject>
    suspend fun getProfile(): FacultyProfile
    suspend fun getResearchEntries(): List<ResearchEntry>
    suspend fun getActivitySummary(): ActivitySummary
    suspend fun getTimetable(): List<TimetableDay>
    suspend fun getNotices(): List<CollegeNotice>
    suspend fun getOnlineMeetings(): List<OnlineMeetingDto>
    suspend fun getAssignments(): List<FacultyAssignmentDto>
    suspend fun getStudents(): List<FacultyStudentDto>
    suspend fun getStudyMaterials(): List<FacultyMaterialDto>
    suspend fun uploadMaterialFile(file: okhttp3.MultipartBody.Part): FileUploadResponseDto
    suspend fun uploadStudyMaterial(payload: UploadMaterialRequestDto): FacultyMaterialDto

    suspend fun getLectureRecordings(): List<FacultyRecordingDto>
    suspend fun getMentorStudents(): List<FacultyMentorshipStudentDto>
    suspend fun getMentorStudentRecord(studentId: String): FacultyMentorshipRecordDto?
    suspend fun saveMentorStudentRecord(studentId: String, payload: FacultyMentorshipRecordDto): FacultyMentorshipRecordDto?
    suspend fun getFacultySalarySlips(): List<FacultySalarySlipDto>
    suspend fun getInternshipDrives(): List<FacultyInternshipDriveDto>
    suspend fun getLegalEvents(): List<FacultyLegalEventDto>
    suspend fun getFacultyNotifications(): List<NotificationDto>
}
