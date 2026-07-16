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
                    classesToday = dto.classesToday,
                    pendingAttendance = dto.pendingAttendance,
                    pendingAssignments = dto.pendingAssignments,
                    leaveBalance = dto.leaveBalance
                )
            } else {
                throw IOException("Failed to fetch faculty dashboard metrics: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch faculty dashboard metrics: ${e.message}")
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
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getProfile(): FacultyProfile {
        return try {
            val response = apiService.getFacultyProfile()
            if (response.isSuccessful) {
                val dto = response.body() ?: throw IOException("Empty response body")
                FacultyProfile(
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
            } else {
                throw IOException("Failed to fetch faculty profile: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch faculty profile: ${e.message}")
        }
    }

    override suspend fun getResearchEntries(): List<ResearchEntry> {
        return try {
            val response = apiService.getFacultyResearch()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
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
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
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
                throw IOException("Failed to fetch activity summary: ${response.code()}")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Failed to fetch activity summary: ${e.message}")
        }
    }

    override suspend fun getTimetable(): List<TimetableDay> {
        return try {
            val response = apiService.getFacultyTimetable()
            if (response.isSuccessful) {
                val items = response.body() ?: return emptyList()
                val grouped = items.groupBy { it.dayOfWeek }
                grouped.map { (day, dayItems) ->
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
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getNotices(): List<CollegeNotice> {
        return try {
            val response = apiService.getNotices()
            if (response.isSuccessful) {
                (response.body() ?: emptyList()).map { dto ->
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
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAssignments(): List<FacultyAssignmentDto> {
        return try {
            val response = apiService.getFacultyAssignments()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getStudents(): List<FacultyStudentDto> {
        return try {
            val response = apiService.getFacultyStudents()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getStudyMaterials(): List<FacultyMaterialDto> {
        return try {
            val response = apiService.getFacultyMaterials()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
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
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getMentorStudents(): List<FacultyMentorshipStudentDto> {
        return try {
            val response = apiService.getMentorStudents()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getMentorStudentRecord(studentId: String): FacultyMentorshipRecordDto? {
        return try {
            val response = apiService.getMentorStudentRecord(studentId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    override suspend fun saveMentorStudentRecord(studentId: String, payload: FacultyMentorshipRecordDto): FacultyMentorshipRecordDto? {
        return try {
            val response = apiService.saveMentorStudentRecord(studentId, payload)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    override suspend fun getOnlineMeetings(): List<OnlineMeetingDto> {
        return try {
            val response = apiService.getOnlineMeetings()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getFacultySalarySlips(): List<FacultySalarySlipDto> {
        return try {
            val response = apiService.getFacultySalarySlips()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getInternshipDrives(): List<FacultyInternshipDriveDto> {
        return try {
            val response = apiService.getFacultyInternshipDrives()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getLegalEvents(): List<FacultyLegalEventDto> {
        return try {
            val response = apiService.getLegalEvents()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getFacultyNotifications(): List<NotificationDto> {
        return try {
            val response = apiService.getFacultyNotifications()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) { emptyList() }
    }
}
