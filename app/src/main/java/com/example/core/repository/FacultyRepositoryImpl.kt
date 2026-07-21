package com.example.core.repository

import com.example.core.network.*
import com.example.features.faculty.models.*
import com.example.features.parent.models.CollegeNotice
import com.example.features.parent.models.TimetableDay
import com.example.features.parent.models.TimetablePeriod
import java.io.IOException

class FacultyRepositoryImpl(private val apiService: CamsApiService) : FacultyRepository {
    override suspend fun getDashboardMetrics(): FacultyDashboardMetrics {
        return try {
            val response = apiService.getFacultyDashboardMetrics()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                FacultyDashboardMetrics(
                    classesToday = dto.metrics.find { it.id == "classes_today" }?.value ?: "0",
                    pendingAttendance = dto.metrics.find { it.id == "pending_attendance" }?.value ?: "0",
                    pendingAssignments = dto.metrics.find { it.id == "pending_assignments" }?.value ?: "0",
                    leaveBalance = dto.metrics.find { it.id == "leave_balance" }?.value ?: "0"
                )
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch faculty dashboard metrics")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to fetch faculty dashboard metrics: ${e.message}")
        }
    }

    override suspend fun getAssignedSubjects(): List<FacultySubject> {
        return try {
            val response = apiService.getFacultySubjects()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    FacultySubject(
                        subjectCode = dto.subjectCode,
                        subjectName = dto.subjectName,
                        degreeCode = dto.degreeCode,
                        section = dto.section,
                        year = dto.year,
                        semester = dto.semester,
                        batch = dto.batch
                    )
                }
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch assigned subjects")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to fetch assigned subjects: ${e.message}")
        }
    }

    private fun mapProfileDto(dto: com.example.core.network.FacultyProfileDto): FacultyProfile {
        return FacultyProfile(
            facultyId = dto.facultyId ?: "",
            userId = dto.userId,
            fullName = dto.fullName,
            designation = dto.designation,
            departmentName = dto.departmentName ?: "",
            email = dto.email,
            phone = dto.phone ?: "",
            employeeCode = dto.employeeCode ?: "",
            employmentStatus = dto.employmentStatus ?: "Active",
            facultyType = dto.facultyType ?: "Permanent",
            profilePhotoUrl = dto.profilePhotoUrl,
            gender = dto.gender ?: "",
            dateOfBirth = dto.dateOfBirth ?: "",
            bloodGroup = dto.bloodGroup ?: "",
            nationality = dto.nationality ?: "",
            maritalStatus = dto.maritalStatus ?: "",
            community = dto.community ?: "",
            alternatePhone = dto.alternatePhone ?: "",
            personalEmail = dto.personalEmail ?: "",
            currentAddress = dto.currentAddress ?: "",
            permanentAddress = dto.permanentAddress ?: "",
            city = dto.city ?: "",
            state = dto.state ?: "",
            pincode = dto.pincode ?: "",
            dateOfJoining = dto.dateOfJoining ?: "",
            specialization = dto.specialization ?: "",
            educationalQualifications = (dto.qualifications ?: emptyList()).map { q ->
                Qualification(q.degree, q.specialization, q.university, q.institution, q.yearOfCompletion, q.percentageCgpa)
            },
            experienceDetails = (dto.experience ?: emptyList()).map { e ->
                Experience(e.institutionName, e.designation, e.fromDate, e.toDate, e.totalYears)
            },
            certifications = dto.certificationsAchievements ?: emptyList(),
            responsibilities = dto.academicResponsibilities ?: emptyList()
        )
    }

    override suspend fun getProfile(): FacultyProfile {
        return try {
            val response = apiService.getFacultyProfile()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                mapProfileDto(dto)
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch faculty profile")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to fetch faculty profile: ${e.message}")
        }
    }

    override suspend fun updateProfile(update: com.example.core.network.FacultyProfileUpdateRequest): FacultyProfile {
        return try {
            val response = apiService.updateFacultyProfile(update)
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                mapProfileDto(dto)
            } else {
                throw Exception("HTTP ${response.code()} Failed to update faculty profile")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to update faculty profile: ${e.message}")
        }
    }

    override suspend fun getResearchEntries(): List<ResearchEntry> {
        val response = apiService.getFacultyResearch()
        if (response.isSuccessful) {
            return (response.body() ?: emptyList()).map { dto ->
                ResearchEntry(
                    id = dto.id,
                    title = dto.title,
                    publication = dto.publication,
                    researchType = dto.researchType ?: "Journal Article",
                    publicationDate = dto.publicationDate,
                    grantAmount = dto.grantAmount,
                    publisher = dto.publisher,
                    status = dto.status
                )
            }
        }
        throw Exception("Failed to load research entries: ${response.code()}")
    }

    override suspend fun createResearchEntry(request: com.example.core.network.ResearchEntryRequest): com.example.core.network.ResearchEntryDto {
        val response = apiService.createFacultyResearch(request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to create research entry: ${response.code()}")
    }

    override suspend fun updateResearchEntry(researchId: String, request: com.example.core.network.ResearchEntryRequest): com.example.core.network.ResearchEntryDto {
        val response = apiService.updateFacultyResearch(researchId, request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to update research entry: ${response.code()}")
    }

    override suspend fun deleteResearchEntry(researchId: String): Boolean {
        val response = apiService.deleteFacultyResearch(researchId)
        if (response.isSuccessful) return true
        throw Exception("Failed to delete research entry: ${response.code()}")
    }

    override suspend fun getActivitySummary(): ActivitySummary {
        return try {
            val response = apiService.getFacultyActivitySummary()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                ActivitySummary(
                    classesConducted = dto.classesConducted,
                    attendanceMarked = dto.attendanceMarked,
                    studyMaterialsUploaded = dto.studyMaterialsUploaded,
                    assignmentsCreated = dto.assignmentsCreated,
                    leaveRequestsSubmitted = dto.leaveRequestsSubmitted
                )
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch activity summary")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to fetch activity summary: ${e.message}")
        }
    }

    override suspend fun getTimetable(): List<TimetableDay> {
        return try {
            val response = apiService.getFacultyTimetable()
            if (response.isSuccessful) {
                val items = response.body() ?: return emptyList()
                val grouped = items.groupBy { it.dayOfWeek.lowercase().replaceFirstChar { c -> c.uppercase() } }
                grouped.map { (day, dayItems) ->
                    TimetableDay(
                        dayName = day,
                        periods = dayItems.sortedBy { it.startTime }.mapIndexed { index, itemDto ->
                            com.example.features.parent.models.TimetablePeriod(
                                periodNo = index + 1,
                                time = "${itemDto.startTime} - ${itemDto.endTime}",
                                subjectName = itemDto.subjectName,
                                subjectCode = itemDto.subjectCode,
                                room = itemDto.room,
                                instructor = itemDto.sectionName
                            )
                        }
                    )
                }
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch timetable")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch timetable: ${e.message}")
        }
    }

    override suspend fun getNotices(): List<CollegeNotice> {
        return try {
            val response = apiService.getFacultyNotices()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
                    CollegeNotice(
                        id = dto.id,
                        title = dto.title,
                        body = dto.body,
                        publishDate = dto.date ?: "N/A",
                        category = dto.category ?: "General",
                        priority = dto.priority ?: "Medium",
                        publisherName = dto.publisherName ?: "Admin",
                        audienceType = dto.audienceType ?: "Faculty",
                        expiryDate = null,
                        publisherRole = dto.publisherRole ?: "Admin",
                        attachmentUrl = dto.attachmentUrl
                    )
                }
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch notices")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to fetch notices: ${e.message}")
        }
    }

    override suspend fun getAssignments(): List<FacultyAssignmentDto> {
        return try {
            val response = apiService.getFacultyAssignments()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch assignments")
        } catch (e: Exception) {
            throw Exception("Failed to fetch assignments: ${e.message}")
        }
    }

    override suspend fun createAssignment(request: CreateAssignmentRequest): FacultyAssignmentDto {
        return try {
            val response = apiService.createAssignment(request)
            if (response.isSuccessful) response.body() ?: throw IOException("Empty response body")
            else throw Exception("HTTP ${response.code()} Failed to create assignment")
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to create assignment: ${e.message}")
        }
    }

    override suspend fun updateAssignment(assignmentId: String, request: CreateAssignmentRequest): FacultyAssignmentDto {
        return try {
            val response = apiService.updateAssignment(assignmentId, request)
            if (response.isSuccessful) response.body() ?: throw IOException("Empty response body")
            else throw Exception("HTTP ${response.code()} Failed to update assignment")
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to update assignment: ${e.message}")
        }
    }

    override suspend fun deleteAssignment(assignmentId: String): Boolean {
        return try {
            val response = apiService.deleteAssignment(assignmentId)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to delete assignment")
        } catch (e: Exception) {
            throw Exception("Failed to delete assignment: ${e.message}")
        }
    }

    override suspend fun getAssignmentSubmissions(): List<FacultyAssignmentSubmissionDto> {
        return try {
            val response = apiService.getAssignmentSubmissions()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch submissions")
        } catch (e: Exception) {
            throw Exception("Failed to fetch submissions: ${e.message}")
        }
    }

    override suspend fun gradeSubmission(submissionId: String, request: GradeSubmissionRequest): FacultyAssignmentSubmissionDto {
        return try {
            val response = apiService.gradeSubmission(submissionId, request)
            if (response.isSuccessful) response.body() ?: throw IOException("Empty response body")
            else throw Exception("HTTP ${response.code()} Failed to grade submission")
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to grade submission: ${e.message}")
        }
    }

    override suspend fun getStudents(semester: Int?): List<FacultyStudentDto> {
        return try {
            val response = apiService.getFacultyStudents(semester)
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch students")
        } catch (e: Exception) {
            throw Exception("Failed to fetch students: ${e.message}")
        }
    }

    override suspend fun getAcademicCalendar(): List<CalendarEventDto> {
        return try {
            val response = apiService.getAcademicCalendar()
            if (response.isSuccessful) response.body()?.events ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch academic calendar")
        } catch (e: Exception) {
            throw Exception("Failed to fetch academic calendar: ${e.message}")
        }
    }

    override suspend fun getFacultyActivityPoints(): List<ActivityPointDto> {
        val response = apiService.getFacultyActivityPoints()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load activity point applications: ${response.code()}")
    }

    override suspend fun reviewActivityPoints(applicationId: String, status: String, approvedPoints: Double, remarks: String?): ActivityPointDto {
        val response = apiService.reviewActivityPoints(applicationId, ActivityPointReviewRequest(status, approvedPoints, remarks))
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to review application: ${response.code()}")
    }

    override suspend fun getActivityPointCategories(): List<ActivityPointCategoryDto> {
        val response = apiService.getActivityPointCategories()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load categories: ${response.code()}")
    }

    override suspend fun createActivityPointCategory(request: ActivityPointCategoryRequest): ActivityPointCategoryDto {
        val response = apiService.createActivityPointCategory(request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to create category: ${response.code()}")
    }

    override suspend fun updateActivityPointCategory(categoryId: String, request: ActivityPointCategoryRequest): ActivityPointCategoryDto {
        val response = apiService.updateActivityPointCategory(categoryId, request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to update category: ${response.code()}")
    }

    override suspend fun deleteActivityPointCategory(categoryId: String): Boolean {
        val response = apiService.deleteActivityPointCategory(categoryId)
        if (response.isSuccessful) return true
        throw Exception("Failed to delete category: ${response.code()}")
    }

    override suspend fun getClassroomActivities(): List<ClassroomActivityDto> {
        return try {
            val response = apiService.getClassroomActivities()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch activities")
        } catch (e: Exception) {
            throw Exception("Failed to fetch activities: ${e.message}")
        }
    }

    override suspend fun createClassroomActivity(request: CreateClassroomActivityRequest): Boolean {
        return try {
            val response = apiService.createClassroomActivity(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to log activity")
        } catch (e: Exception) {
            throw Exception("Failed to log activity: ${e.message}")
        }
    }

    override suspend fun getClassroomInteractions(): List<StudentInteractionDto> {
        return try {
            val response = apiService.getClassroomInteractions()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch polls")
        } catch (e: Exception) {
            throw Exception("Failed to fetch polls: ${e.message}")
        }
    }

    override suspend fun createClassroomInteraction(request: CreateInteractionRequest): Boolean {
        return try {
            val response = apiService.createClassroomInteraction(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to create poll")
        } catch (e: Exception) {
            throw Exception("Failed to create poll: ${e.message}")
        }
    }

    override suspend fun getSessionSummaries(): List<SessionSummaryDto> {
        return try {
            val response = apiService.getSessionSummaries()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch session summaries")
        } catch (e: Exception) {
            throw Exception("Failed to fetch session summaries: ${e.message}")
        }
    }

    override suspend fun createSessionSummary(request: CreateSessionSummaryRequest): Boolean {
        return try {
            val response = apiService.createSessionSummary(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to save session summary")
        } catch (e: Exception) {
            throw Exception("Failed to save session summary: ${e.message}")
        }
    }

    override suspend fun getInternalMarks(sectionId: String, subjectId: String, academicYear: String?): List<InternalMarkStudentDto> {
        return try {
            val response = apiService.getInternalMarks(sectionId, subjectId, academicYear)
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch internal marks")
        } catch (e: Exception) {
            throw Exception("Failed to fetch internal marks: ${e.message}")
        }
    }

    override suspend fun saveInternalMarks(request: SaveInternalMarksRequest): Boolean {
        return try {
            val response = apiService.saveInternalMarks(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to save marks")
        } catch (e: Exception) {
            throw Exception("Failed to save marks: ${e.message}")
        }
    }

    override suspend fun submitInternalMarks(request: SubmitMarksRequest): Boolean {
        return try {
            val response = apiService.submitInternalMarks(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to submit marks")
        } catch (e: Exception) {
            throw Exception("Failed to submit marks: ${e.message}")
        }
    }

    override suspend fun getAttendanceSections(): List<FacultyAttendanceSectionDto> {
        return try {
            val response = apiService.getFacultyAttendanceSections()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch attendance sections")
        } catch (e: Exception) {
            throw Exception("Failed to fetch attendance sections: ${e.message}")
        }
    }

    override suspend fun getAttendanceStudents(sectionId: String, subjectId: String): List<FacultyAttendanceStudentDto> {
        return try {
            val response = apiService.getFacultyAttendanceStudents(sectionId, subjectId)
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch students for attendance")
        } catch (e: Exception) {
            throw Exception("Failed to fetch students for attendance: ${e.message}")
        }
    }

    override suspend fun markAttendanceBulk(request: BulkAttendanceMarkRequest): Boolean {
        return try {
            val response = apiService.markFacultyAttendanceBulk(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to submit attendance")
        } catch (e: Exception) {
            throw Exception("Failed to submit attendance: ${e.message}")
        }
    }

    override suspend fun getStudyMaterials(): List<FacultyMaterialDto> {
        return try {
            val response = apiService.getFacultyMaterials()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch study materials")
        } catch (e: Exception) {
            throw Exception("Failed to fetch study materials: ${e.message}")
        }
    }

    override suspend fun editStudyMaterial(materialId: String, payload: UploadMaterialRequestDto): FacultyMaterialDto {
        return try {
            val response = apiService.editStudyMaterial(materialId, payload)
            if (response.isSuccessful) response.body() ?: throw IOException("Empty response body")
            else throw Exception("HTTP ${response.code()} Failed to update study material")
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to update study material: ${e.message}")
        }
    }

    override suspend fun archiveStudyMaterial(materialId: String): Boolean {
        return try {
            val response = apiService.archiveStudyMaterial(materialId)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to delete study material")
        } catch (e: Exception) {
            throw Exception("Failed to delete study material: ${e.message}")
        }
    }

    override suspend fun uploadMaterialFile(file: okhttp3.MultipartBody.Part): FileUploadResponseDto {
        return try {
            val response = apiService.uploadMaterialFile(file)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to upload material file: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to upload material file: ${e.message}")
        }
    }

    override suspend fun uploadStudyMaterial(payload: UploadMaterialRequestDto): FacultyMaterialDto {
        return try {
            val response = apiService.uploadStudyMaterial(payload)
            if (response.isSuccessful) {
                response.body() ?: throw IOException("Empty response body")
            } else {
                throw IOException("Failed to upload study material: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to upload study material: ${e.message}")
        }
    }

    override suspend fun getLectureRecordings(): List<FacultyRecordingDto> {
        return try {
            val response = apiService.getFacultyRecordings()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch lecture recordings")
        } catch (e: Exception) {
            throw Exception("Failed to fetch lecture recordings: ${e.message}")
        }
    }

    override suspend fun createRecording(request: CreateRecordingRequest): Boolean {
        return try {
            val response = apiService.createRecording(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to add recording")
        } catch (e: Exception) {
            throw Exception("Failed to add recording: ${e.message}")
        }
    }

    override suspend fun deleteRecording(recordingId: String): Boolean {
        return try {
            val response = apiService.deleteRecording(recordingId)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to delete recording")
        } catch (e: Exception) {
            throw Exception("Failed to delete recording: ${e.message}")
        }
    }

    override suspend fun getMentorStudents(): List<FacultyMentorshipStudentDto> {
        val response = apiService.getMentorStudents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load mentees: HTTP ${response.code()}")
    }

    override suspend fun getMentorStudentRecord(studentId: String): FacultyMentorshipRecordDto? {
        val response = apiService.getMentorStudentRecord(studentId)
        if (response.isSuccessful) return response.body()
        // 404 legitimately means "no mentorship record yet" — not an error.
        if (response.code() == 404) return null
        throw Exception("Failed to load mentorship record: HTTP ${response.code()}")
    }

    override suspend fun saveMentorStudentRecord(studentId: String, payload: FacultyMentorshipRecordDto): FacultyMentorshipRecordDto? {
        val response = apiService.saveMentorStudentRecord(studentId, payload)
        if (response.isSuccessful) return response.body()
        throw Exception("Failed to save mentorship record: ${response.code()}")
    }

    override suspend fun getOnlineMeetings(): List<OnlineMeetingDto> {
        val response = apiService.getOnlineMeetings()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load meetings: ${response.code()}")
    }

    override suspend fun createOnlineMeeting(request: CreateMeetingRequest): Boolean {
        val response = apiService.createOnlineMeeting(request)
        if (response.isSuccessful) return true
        throw Exception("Failed to schedule meeting: ${response.code()}")
    }

    override suspend fun deleteOnlineMeeting(meetingId: String): Boolean {
        val response = apiService.deleteOnlineMeeting(meetingId)
        if (response.isSuccessful) return true
        throw Exception("Failed to delete meeting: ${response.code()}")
    }

    override suspend fun getFacultySalarySlips(): List<FacultySalarySlipDto> {
        val response = apiService.getFacultySalarySlips()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load salary slips: ${response.code()}")
    }

    override suspend fun getInternshipDrives(): List<FacultyInternshipDriveDto> {
        val response = apiService.getFacultyInternshipDrives()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load internship drives: ${response.code()}")
    }

    override suspend fun getInternshipApplications(): List<InternshipApplicationDto> {
        val response = apiService.getFacultyInternshipApplications()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load applications: ${response.code()}")
    }

    override suspend fun reviewInternshipApplication(applicationId: String, status: String): InternshipApplicationDto {
        val response = apiService.reviewInternshipApplication(applicationId, InternshipApplicationReviewRequest(status))
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to update application: ${response.code()}")
    }

    override suspend fun getPartnerCompanies(): List<PartnerCompanyDto> {
        val response = apiService.getPartnerCompanies()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load partners: ${response.code()}")
    }

    override suspend fun createPartnerCompany(request: PartnerCompanyRequest): PartnerCompanyDto {
        val response = apiService.createPartnerCompany(request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to create partner: ${response.code()}")
    }

    override suspend fun updatePartnerCompany(partnerId: String, request: PartnerCompanyRequest): PartnerCompanyDto {
        val response = apiService.updatePartnerCompany(partnerId, request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to update partner: ${response.code()}")
    }

    override suspend fun deletePartnerCompany(partnerId: String): Boolean {
        val response = apiService.deletePartnerCompany(partnerId)
        if (response.isSuccessful) return true
        throw Exception("Failed to delete partner: ${response.code()}")
    }

    override suspend fun getLegalEvents(): List<FacultyLegalEventDto> {
        return try {
            val response = apiService.getLegalEvents()
            if (response.isSuccessful) response.body() ?: emptyList()
            else throw Exception("HTTP ${response.code()} Failed to fetch legal events")
        } catch (e: Exception) {
            throw Exception("Failed to fetch legal events: ${e.message}")
        }
    }

    override suspend fun postLegalEvent(request: CreateLegalEventRequest): Boolean {
        return try {
            val response = apiService.postLegalEvent(request)
            if (response.isSuccessful) true
            else throw Exception("HTTP ${response.code()} Failed to submit event")
        } catch (e: Exception) {
            throw Exception("Failed to submit event: ${e.message}")
        }
    }

    override suspend fun getFacultyNotifications(): List<NotificationDto> {
        return try {
            val response = apiService.getFacultyNotifications()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("HTTP ${response.code()} Failed to fetch notifications")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Failed to fetch notifications: ${e.message}")
        }
    }

    override suspend fun markFacultyNotificationRead(notificationId: String): Boolean {
        val response = apiService.markFacultyNotificationRead(notificationId)
        if (response.isSuccessful) return true
        throw Exception("Failed to mark notification read: ${response.code()}")
    }

    override suspend fun markAllFacultyNotificationsRead(): Boolean {
        val response = apiService.markAllFacultyNotificationsRead()
        if (response.isSuccessful) return true
        throw Exception("Failed to mark all notifications read: ${response.code()}")
    }

    override suspend fun getLeaveBalances(): LeaveBalanceDto {
        val response = apiService.getLeaveBalances()
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to load leave balances: ${response.code()}")
    }

    override suspend fun getLeaveHistory(): List<LeaveRequestDto> {
        val response = apiService.getLeaves()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load leave history: ${response.code()}")
    }

    override suspend fun applyForLeave(type: String, fromDate: String, toDate: String, reason: String, emergencyContact: String): LeaveRequestDto {
        val response = apiService.applyLeaveMultipart(
            type = com.example.core.network.MultipartUploadHelper.createPartFromString(type),
            fromDate = com.example.core.network.MultipartUploadHelper.createPartFromString(fromDate),
            toDate = com.example.core.network.MultipartUploadHelper.createPartFromString(toDate),
            reason = com.example.core.network.MultipartUploadHelper.createPartFromString(reason),
            emergencyContact = com.example.core.network.MultipartUploadHelper.createPartFromString(emergencyContact),
            file = null
        )
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to submit leave application: ${response.code()}")
    }

    override suspend fun cancelLeave(leaveId: String): Boolean {
        val response = apiService.cancelLeave(leaveId)
        if (response.isSuccessful) return true
        throw Exception("Failed to cancel leave: ${response.code()}")
    }

    override suspend fun getClassDiaries(): List<ClassDiaryDto> {
        val response = apiService.getClassDiaries()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load diary entries: ${response.code()}")
    }

    override suspend fun createClassDiary(request: ClassDiaryRequest): ClassDiaryDto {
        val response = apiService.createClassDiary(request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        if (response.code() == 403) throw Exception("You are not assigned to teach this subject/section")
        throw Exception("Failed to save diary entry: ${response.code()}")
    }

    override suspend fun updateClassDiary(id: String, request: ClassDiaryRequest): ClassDiaryDto {
        val response = apiService.updateClassDiary(id, request)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to update diary entry: ${response.code()}")
    }

    override suspend fun reviewClassDiary(id: String, hodStatus: String, hodRemarks: String?): ClassDiaryDto {
        val response = apiService.reviewClassDiary(id, DiaryReviewRequest(hodStatus, hodRemarks))
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to review diary entry: ${response.code()}")
    }

    override suspend fun getAdvisorAssignment(): AdvisorAssignmentDto {
        val response = apiService.getAdvisorAssignment()
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to load advisor assignment: ${response.code()}")
    }

    override suspend fun getAdvisorClassStudents(): List<AdvisorStudentDto> {
        val response = apiService.getAdvisorClassStudents()
        if (response.isSuccessful) return response.body() ?: emptyList()
        if (response.code() == 403) return emptyList()
        throw Exception("Failed to load class roster: ${response.code()}")
    }

    override suspend fun getAdvisorStudentLeaves(): List<AdvisorLeaveDto> {
        val response = apiService.getAdvisorStudentLeaves()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load student leaves: ${response.code()}")
    }

    override suspend fun advisorApproveLeave(leaveId: String, status: String, remarks: String?): AdvisorLeaveDto {
        val response = apiService.advisorApproveLeave(leaveId, LeaveApprovalRequest(status, remarks))
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to update leave: ${response.code()}")
    }

    override suspend fun getMessageContacts(): List<MessageContactDto> {
        val response = apiService.getMessageContacts()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load contacts: ${response.code()}")
    }

    override suspend fun getConversations(): List<ConversationDto> {
        val response = apiService.getConversations()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load conversations: ${response.code()}")
    }

    override suspend fun getMessageThread(userId: String): List<MessageDto> {
        val response = apiService.getMessageThread(userId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load messages: ${response.code()}")
    }

    override suspend fun sendMessage(receiverId: String, body: String): MessageDto {
        val response = apiService.sendMessage(SendMessageRequest(receiverId, body))
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty response")
        throw Exception("Failed to send message: ${response.code()}")
    }

    override suspend fun markThreadRead(userId: String): Boolean {
        val response = apiService.markThreadRead(userId)
        if (response.isSuccessful) return true
        throw Exception("Failed to mark thread read: ${response.code()}")
    }
}
