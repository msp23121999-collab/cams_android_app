package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.parent.models.*

class ParentRepositoryImpl(
    private val apiService: CamsApiService
) : ParentRepository {

    override suspend fun getChildProfile(): ChildProfileExtended {
        val response = apiService.getParentChildProfile()
        if (response.isSuccessful && response.body() != null) {
            val dto = response.body()!!
            return ChildProfileExtended(
                id = dto.id,
                fullName = dto.fullName,
                rollNo = dto.rollNo,
                semester = dto.semester,
                batch = dto.batch,
                cgpa = dto.cgpa,
                mentorName = dto.mentorName,
                mentorEmail = dto.mentorEmail,
                mentorPhone = dto.mentorPhone,
                dob = dto.dob,
                gender = dto.gender,
                bloodGroup = dto.bloodGroup,
                nationality = dto.nationality,
                aadhaarNo = dto.aadhaarNo,
                contactMobile = dto.contactMobile,
                contactEmail = dto.contactEmail,
                emergencyContact = dto.emergencyContact,
                emergencyPhone = dto.emergencyPhone,
                fatherName = dto.fatherName,
                fatherOccupation = dto.fatherOccupation,
                fatherMobile = dto.fatherMobile,
                fatherEmail = dto.fatherEmail,
                motherName = dto.motherName,
                motherOccupation = dto.motherOccupation,
                motherMobile = dto.motherMobile,
                motherEmail = dto.motherEmail,
                certifications = dto.certifications.map { 
                    ChildCertification(it.title, it.issuer, it.category, it.date, it.status)
                }
            )
        }
        throw Exception("Failed to fetch child profile")
    }

    override suspend fun getInternalMarks(): List<ChildInternalMark> {
        val response = apiService.getParentChildMarks()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                ChildInternalMark(it.subject, it.academicYear, it.internal1, it.internal2, it.model, it.assignments, it.attendance, it.total)
            }
        }
        return emptyList()
    }

    override suspend fun getPerformanceAnalytics(): List<PerformanceData> {
        val response = apiService.getParentChildPerformance()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                PerformanceData(it.semester, it.gpa, it.attendance)
            }
        }
        return emptyList()
    }

    override suspend fun getFeeStatus(): ChildFeeLedger {
        val response = apiService.getParentChildFees()
        if (response.isSuccessful && response.body() != null) {
            val dto = response.body()!!
            return ChildFeeLedger(
                totalFees = dto.totalFees,
                scholarshipDeduction = dto.scholarshipDeduction,
                otherDeductions = dto.otherDeductions,
                netFees = dto.netFees,
                amountPaid = dto.amountPaid,
                pendingBalance = dto.pendingBalance,
                dueDate = dto.dueDate,
                records = dto.records.map { 
                    FeeLedgerRecord(it.id, it.title, it.amount, it.date, it.status)
                }
            )
        }
        throw Exception("Failed to fetch fee status")
    }

    override suspend fun getNotices(): List<CollegeNotice> {
        val response = apiService.getNotices()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                CollegeNotice(it.id, it.title, it.category, "Medium", it.body, it.date, it.date, "All", "Admin", "CAMS")
            }
        }
        return emptyList()
    }

    override suspend fun getAttendance(): List<AttendanceRecord> {
        val response = apiService.getParentChildAttendance()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                AttendanceRecord(it.date, it.status)
            }
        }
        return emptyList()
    }

    override suspend fun getSubjectAttendance(): List<SubjectAttendance> {
        val response = apiService.getParentChildSubjectAttendance()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                SubjectAttendance(it.subject, it.totalClasses, it.attendedClasses, it.percentage)
            }
        }
        return emptyList()
    }

    override suspend fun getTimetable(): List<TimetableDay> {
        val response = apiService.getParentChildTimetable()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { dayDto ->
                TimetableDay(dayDto.day, dayDto.periods.map { pDto ->
                    TimetablePeriod(pDto.periodNo, pDto.time, pDto.subject, pDto.code, pDto.room, pDto.faculty)
                })
            }
        }
        return emptyList()
    }
}
