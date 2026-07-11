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
                    publicationDate = dto.publicationDate
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
            return response.body()!!.map { dayDto ->
                TimetableDay(
                    dayName = dayDto.day,
                    periods = dayDto.periods.map { pDto ->
                        TimetablePeriod(
                            periodNo = pDto.periodNo,
                            time = pDto.time,
                            subjectName = pDto.subject,
                            subjectCode = pDto.code,
                            room = pDto.room,
                            instructor = pDto.batch // Reusing instructor field for batch in faculty view if needed
                        )
                    }
                )
            }
        }
        throw IOException("Failed to fetch faculty timetable")
    }

    override suspend fun getNotices(): List<CollegeNotice> {
        val response = apiService.getNotices() // Shared notices endpoint
        if (response.isSuccessful) {
            return response.body()!!.map { dto ->
                CollegeNotice(
                    id = dto.id,
                    title = dto.title,
                    body = dto.body,
                    publishDate = dto.date,
                    category = dto.category,
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

    override suspend fun getLectureRecordings(): List<FacultyRecordingDto> {
        val response = apiService.getFacultyRecordings()
        if (response.isSuccessful) return response.body()!!
        throw IOException("Failed to fetch lecture recordings")
    }
}
