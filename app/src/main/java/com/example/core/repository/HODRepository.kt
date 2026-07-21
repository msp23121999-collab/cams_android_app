package com.example.core.repository

import com.example.features.hod.models.HODDashboardMetrics
import com.example.features.hod.models.HODActivity

interface HODRepository {
    suspend fun getDashboardMetrics(): HODDashboardMetrics
    suspend fun getRecentActivities(): List<HODActivity>
    suspend fun getActiveFaculty(): List<com.example.core.network.HODFacultyResponseDto>
    suspend fun getHODManagementStudents(): com.example.core.network.HODManagementStudentsDto
    suspend fun verifyStudentProfile(studentId: String, action: String, remarks: String?): Boolean
    suspend fun getPendingLeaveApprovals(): List<com.example.core.network.LeaveRequestDto>
    suspend fun approveLeave(id: String, status: String, remarks: String? = null)
    suspend fun getTimetableMetadata(): com.example.core.network.HODTimetableMetadataDto
    suspend fun getTimetableSection(sectionId: String): List<com.example.core.network.TimetableSlotDto>
    suspend fun submitTimetable(sectionId: String, slots: List<com.example.core.network.TimetableSlotInputDto>)

    suspend fun getTeachingLogsDashboard(): com.example.core.network.HODTeachingLogsDashboardDto
    suspend fun getSyllabusMetadata(): com.example.core.network.HODSyllabusMetadataDto
    suspend fun getSyllabusCourses(): List<com.example.core.network.HODCourseDto>
    suspend fun getCoursePlan(courseName: String): Map<String, List<String>>
    suspend fun saveCoursePlan(courseName: String, units: Map<String, List<String>>)
    suspend fun getAttendanceMonitoring(): List<com.example.core.network.HODAttendanceMonitoringDto>
    suspend fun getPendingEntries(): List<com.example.core.network.HODPendingEntryDto>
    suspend fun getDepartmentReports(): com.example.core.network.HODDepartmentReportDto
    suspend fun getStudentReports(): com.example.core.network.HODStudentReportDto
    suspend fun exportDepartmentReportCsv(): String
    suspend fun exportStudentReportCsv(): String

    suspend fun getCalendarEvents(): List<com.example.core.network.HODCalendarEventDto>
    suspend fun createCalendarEvent(request: com.example.core.network.HODCalendarEventCreateRequest): com.example.core.network.HODCalendarEventDto
    suspend fun deleteCalendarEvent(eventId: String)

    suspend fun getResearchMonitoring(): List<com.example.core.network.HODResearchMonitoringDto>
    suspend fun getPendingProofs(): List<com.example.core.network.HODPendingProofDto>
    suspend fun verifyResearchProof(proofId: String, request: com.example.core.network.VerificationRequestDto): Map<String, Any>

    suspend fun getHODWorkloads(): List<com.example.core.network.HODWorkloadDto>
    suspend fun getHODPendingMarks(): List<com.example.core.network.HODPendingMarksGroupDto>
    suspend fun approveHODMarks(sectionId: String, subjectId: String, academicYear: String)
    suspend fun getHODMentors(): com.example.core.network.HODMentorsPayloadDto
    suspend fun getHodPendingMaterials(): List<com.example.core.network.HodPendingMaterialDto>
    suspend fun reviewHodMaterial(materialId: String, status: String, remarks: String)
    suspend fun getHodClasses(): com.example.core.network.ClassAdvisorSetupDto
    suspend fun assignClassAdvisor(academicYearId: String, batch: String, sectionName: String, facultyId: String)
    suspend fun getAttendanceCorrectionRequests(): List<com.example.core.network.AttendanceCorrectionDto>
    suspend fun approveAttendanceCorrection(requestId: String)
    suspend fun rejectAttendanceCorrection(requestId: String, remarks: String)
    suspend fun getPendingProfileUpdateRequests(): List<com.example.core.network.FacultyProfileUpdateRequestDto>
    suspend fun reviewProfileUpdateRequest(requestId: String, action: String, comments: String?)
    suspend fun assignHODMentor(facultyId: String, studentIds: List<String>)

    // Module 4 Methods
    suspend fun getAcademicSetup(): Result<com.example.core.network.AcademicSetupDto>
    suspend fun getSubjectAllocations(): Result<List<com.example.core.network.SubjectAllocationDto>>
    suspend fun getAllocationSubjects(): Result<List<com.example.core.network.SubjectInfoDto>>
    suspend fun getCourseSections(courseId: String): Result<List<com.example.core.network.AcademicSetupSectionDto>>
    suspend fun getAllocationFaculty(): Result<List<com.example.core.network.FacultyWorkloadInfoDto>>
    suspend fun allocateSubjects(allocations: List<com.example.core.network.SubjectAllocationCreateDto>): Result<Unit>

    suspend fun getSubstitutions(): Result<List<com.example.core.network.HODSubstitutionDto>>
    suspend fun getAvailableSubstituteFaculty(): List<com.example.core.network.HODFacultyResponseDto>
    suspend fun assignSubstitution(
        absentFacultyId: String, absentFacultyName: String,
        substituteFacultyId: String, substituteFacultyName: String,
        subject: String, section: String, date: String, periodLabel: String
    )
}
