package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.features.parent.models.*
import com.example.core.datastore.ParentPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ParentRepositoryImpl(
    private val apiService: CamsApiService,
    private val parentPreferences: ParentPreferences
) : ParentRepository {

    override val selectedChildId: Flow<String?> = parentPreferences.getSelectedChild()

    private var currentChildId: String? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            selectedChildId.collect { id ->
                currentChildId = id
            }
        }
    }

    override suspend fun setSelectedChildId(childId: String) {
        parentPreferences.saveSelectedChild(childId)
    }

    override suspend fun getChildrenList(): List<ChildSummary> {
        val response = apiService.getParentChildren()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map {
                ChildSummary(
                    id = it.id,
                    fullName = it.fullName,
                    rollNo = it.rollNo,
                    profilePhotoUrl = it.profilePhotoUrl
                )
            }
        }
        return emptyList()
    }

    override suspend fun getChildProfile(childId: String?): ChildProfileExtended {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildProfile(idToUse)
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

    override suspend fun getInternalMarks(childId: String?): List<ChildInternalMark> {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildMarks(idToUse)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                ChildInternalMark(it.subject, it.academicYear, it.internal1, it.internal2, it.model, it.assignments, it.attendance, it.total)
            }
        }
        return emptyList()
    }

    override suspend fun getPerformanceAnalytics(childId: String?): List<PerformanceData> {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildPerformance(idToUse)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                PerformanceData(it.semester, it.gpa, it.attendance)
            }
        }
        return emptyList()
    }

    override suspend fun getFeeStatus(childId: String?): ChildFeeLedger {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildFees(idToUse)
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

    override suspend fun getNotices(childId: String?): List<CollegeNotice> {
        val response = apiService.getNotices()
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                CollegeNotice(it.id, it.title, it.category ?: "General", "Medium", it.body, it.date ?: "N/A", it.date ?: "N/A", "All", "Admin", "CAMS")
            }
        }
        return emptyList()
    }

    override suspend fun getAttendance(childId: String?): List<AttendanceRecord> {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildAttendance(idToUse)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                AttendanceRecord(it.date, it.status)
            }
        }
        return emptyList()
    }

    override suspend fun getSubjectAttendance(childId: String?): List<SubjectAttendance> {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildSubjectAttendance(idToUse)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { 
                SubjectAttendance(it.subject, it.totalClasses, it.attendedClasses, it.percentage)
            }
        }
        return emptyList()
    }

    override suspend fun getTimetable(childId: String?): List<TimetableDay> {
        val idToUse = childId ?: currentChildId
        val response = apiService.getParentChildTimetable(idToUse)
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.map { dayDto ->
                TimetableDay(dayDto.day, dayDto.periods.map { pDto ->
                    TimetablePeriod(pDto.periodNo, pDto.time, pDto.subject, pDto.code, pDto.room, pDto.faculty)
                })
            }
        }
        return emptyList()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val response = apiService.changePassword(
            com.example.core.network.ChangePasswordRequest(currentPassword, newPassword)
        )
        if (!response.isSuccessful) {
            throw Exception("Failed to change password: ${response.code()}")
        }
    }
}
