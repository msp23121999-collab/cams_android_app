package com.example.core.repository

import com.example.features.hod.models.HODDashboardMetrics
import com.example.features.hod.models.HODActivity

interface HODRepository {
    suspend fun getDashboardMetrics(): HODDashboardMetrics
    suspend fun getRecentActivities(): List<HODActivity>
    suspend fun getFacultyManagementData(): List<com.example.core.network.FacultyStudentDto>
    suspend fun getStudentManagementData(): List<com.example.core.network.FacultyStudentDto>
    suspend fun getPendingLeaveApprovals(): List<com.example.core.network.LeaveRequestDto>
    suspend fun approveLeave(id: String, status: String, remarks: String? = null)
    suspend fun getTimetableMetadata(): com.example.core.network.HODTimetableMetadataDto
    suspend fun getTimetableSection(sectionId: String): List<com.example.core.network.TimetableSlotDto>

    suspend fun getTeachingLogsDashboard(): com.example.core.network.HODTeachingLogsDashboardDto
    suspend fun getSyllabusMetadata(): com.example.core.network.HODSyllabusMetadataDto
    suspend fun getSyllabusCourses(): List<com.example.core.network.HODCourseDto>
    suspend fun getAttendanceMonitoring(): List<com.example.core.network.HODAttendanceMonitoringDto>
    suspend fun getDepartmentReports(): com.example.core.network.HODDepartmentReportDto

    suspend fun getResearchMonitoring(): List<com.example.core.network.HODResearchMonitoringDto>
    suspend fun getPendingProofs(): List<com.example.core.network.HODPendingProofDto>
    suspend fun verifyResearchProof(proofId: String, request: com.example.core.network.VerificationRequestDto): Map<String, String>

    suspend fun getHODWorkloads(): List<com.example.core.network.HODWorkloadDto>
    suspend fun getHODMentors(): List<com.example.core.network.HODMentorDto>
    suspend fun assignHODMentor(studentId: String, facultyId: String)

    // Module 4 Methods
    suspend fun getAcademicSetup(): Result<com.example.core.network.AcademicSetupDto>
    suspend fun getSubjectAllocations(): Result<List<com.example.core.network.SubjectAllocationDto>>

    suspend fun getSubstitutions(): Result<List<com.example.core.network.HODSubstitutionDto>>
}
