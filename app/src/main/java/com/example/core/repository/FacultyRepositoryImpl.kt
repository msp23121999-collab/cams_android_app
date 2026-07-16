package com.example.core.repository

import com.example.core.network.*
import com.example.features.faculty.models.*
import com.example.features.parent.models.CollegeNotice
import com.example.features.parent.models.TimetableDay
import com.example.features.parent.models.TimetablePeriod
import java.io.IOException

class FacultyRepositoryImpl(private val apiService: CamsApiService) : FacultyRepository {
    override suspend fun getDashboardMetrics(): FacultyDashboardMetrics {
        val response = apiService.getFacultyDashboardMetrics()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return FacultyDashboardMetrics(
                classesToday = dto.classesToday,
                pendingAttendance = dto.pendingAttendance,
                pendingAssignments = dto.pendingAssignments,
                leaveBalance = dto.leaveBalance
            )
        }
        throw IOException("Failed to fetch faculty dashboard metrics")
    }

    override suspend fun getAssignedSubjects(): List<FacultySubject> {
        val response = apiService.getFacultySubjects()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
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
        }
        throw IOException("Failed to fetch assigned subjects")
    }

    override suspend fun getProfile(): FacultyProfile {
        val response = apiService.getFacultyProfile()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return FacultyProfile(
                facultyId = dto.facultyId,
                fullName = dto.fullName,
                designation = dto.designation,
                departmentName = dto.departmentName,
                email = dto.email,
                phone = dto.phone,
                employeeCode = dto.employeeCode,
                specialization = dto.specialization,
                educationalQualifications = dto.qualifications.map { q ->
                    Qualification(q.degree, q.specialization, q.university, q.institution, q.yearOfCompletion, q.percentageCgpa)
                },
                experienceDetails = dto.experience.map { e ->
                    Experience(e.institutionName, e.designation, e.fromDate, e.toDate, e.totalYears)
                }
            )
        }
        throw IOException("Failed to fetch faculty profile")
    }

    override suspend fun getResearchEntries(): List<ResearchEntry> {
        val response = apiService.getFacultyResearch()
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                ResearchEntry(
                    id = dto.id,
                    title = dto.title,
                    publication = dto.publication,
                    researchType = dto.researchType,
                    publicationDate = dto.publicationDate,
                    grantAmount = dto.grantAmount,
                    publisher = dto.publisher,
                    status = dto.status
                )
            }
        }
        throw IOException("Failed to fetch research entries")
    }

    override suspend fun getActivitySummary(): ActivitySummary {
        val response = apiService.getFacultyActivitySummary()
        if (response.isSuccessful) {
            val dto = response.body()!!
            return ActivitySummary(
                classesConducted = dto.classesConducted,
                attendanceMarked = dto.attendanceMarked,
                studyMaterialsUploaded = dto.studyMaterialsUploaded,
                assignmentsCreated = dto.assignmentsCreated,
                leaveRequestsSubmitted = dto.leaveRequestsSubmitted
            )
        }
        throw IOException("Failed to fetch activity summary")
    }

    override suspend fun getTimetable(): List<TimetableDay> {
        val response = apiService.getFacultyTimetable()
        if (response.isSuccessful) {
                        val items = response.body() ?: return emptyList()
            // Group flat timetable items by dayOfWeek
            val grouped = items.groupBy { it.dayOfWeek }
            
            return grouped.map { (day, dayItems) ->
                TimetableDay(
                    dayName = day,
                    periods = dayItems.mapIndexed { index, itemDto ->
                        com.example.features.parent.models.TimetablePeriod(
                            periodNo = index + 1,
                            time = "${itemDto.startTime} - ${itemDto.endTime}",
                            subjectName = itemDto.courseName,
                            subjectCode = "",
                            room = itemDto.roomNo,
                            instructor = ""
                        )
                    }
                )
            }
        }
        throw IOException("Failed to fetch timetable")
    }

    override suspend fun getNotices(): List<CollegeNotice> {
        val response = apiService.getNotices() // Shared notices endpoint
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                CollegeNotice(
                    id = dto.id,
                    title = dto.title,
                    body = dto.body,
                    publishDate = dto.date ?: "N/A",
                    category = dto.category ?: "General",
                    priority = "Medium",
                    publisherName = "Admin",
                    audienceType = "Faculty",
                    expiryDate = null,
                    publisherRole = "Admin"
                )
            }
        }
        throw IOException("Failed to fetch notices")
    }

    override suspend fun getAssignments(): List<FacultyAssignmentDto> {
        val response = apiService.getFacultyAssignments()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch faculty assignments")
    }

    override suspend fun getStudents(): List<FacultyStudentDto> {
        val response = apiService.getFacultyStudents()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch faculty students")
    }

    override suspend fun getStudyMaterials(): List<FacultyMaterialDto> {
        val response = apiService.getFacultyMaterials()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch study materials")
    }

    override suspend fun uploadMaterialFile(file: okhttp3.MultipartBody.Part): FileUploadResponseDto {
        val response = apiService.uploadMaterialFile(file)
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to upload material file")
    }

    override suspend fun uploadStudyMaterial(payload: UploadMaterialRequestDto): FacultyMaterialDto {
        val response = apiService.uploadStudyMaterial(payload)
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to upload study material")
    }

    override suspend fun getLectureRecordings(): List<FacultyRecordingDto> {
        val response = apiService.getFacultyRecordings()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch lecture recordings")
    }

    override suspend fun getMentorStudents(): List<FacultyMentorshipStudentDto> {
        val response = apiService.getMentorStudents()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch mentorship students")
    }

    override suspend fun getMentorStudentRecord(studentId: String): FacultyMentorshipRecordDto? {
        val response = apiService.getMentorStudentRecord(studentId)
        if (response.isSuccessful) return response.body()
        throw IOException("Failed to fetch mentor student record")
    }

    override suspend fun saveMentorStudentRecord(studentId: String, payload: FacultyMentorshipRecordDto): FacultyMentorshipRecordDto? {
        val response = apiService.saveMentorStudentRecord(studentId, payload)
        if (response.isSuccessful) return response.body()
        throw IOException("Failed to save mentor student record")
    }

    override suspend fun getOnlineMeetings(): List<OnlineMeetingDto> {
        val response = apiService.getOnlineMeetings()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch online meetings")
    }

    override suspend fun getFacultySalarySlips(): List<FacultySalarySlipDto> {
        val response = apiService.getFacultySalarySlips()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch salary slips")
    }

    override suspend fun getInternshipDrives(): List<FacultyInternshipDriveDto> {
        val response = apiService.getFacultyInternshipDrives()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch internship drives")
    }

    override suspend fun getLegalEvents(): List<FacultyLegalEventDto> {
        val response = apiService.getLegalEvents()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch legal events")
    }

    override suspend fun getFacultyNotifications(): List<NotificationDto> {
        val response = apiService.getFacultyNotifications()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch notifications")
    }
}
