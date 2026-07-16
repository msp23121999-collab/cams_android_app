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
        return try {
            val response = apiService.getParentChildren()
            if (response.isSuccessful && response.body() != null) {
                (response.body() ?: emptyList()).map {
                    ChildSummary(
                        id = it.id,
                        fullName = it.fullName,
                        rollNo = it.rollNo,
                        profilePhotoUrl = it.profilePhotoUrl
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getChildProfile(childId: String?): ChildProfileExtended {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildProfile(idToUse)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body() ?: throw Exception("Body disappeared after null check")
                ChildProfileExtended(
                    id = dto.id,
                    fullName = dto.fullName,
                    rollNo = dto.rollNo,
                    semester = dto.semester.toString(),
                    batch = dto.batch ?: dto.batchYear?.toString() ?: "",
                    cgpa = dto.cgpa ?: 0.0,
                    mentorName = dto.mentorName ?: "N/A",
                    mentorEmail = dto.mentorEmail ?: "",
                    mentorPhone = dto.mentorPhone ?: "",
                    dob = dto.dob ?: "",
                    gender = dto.gender ?: "",
                    bloodGroup = dto.bloodGroup ?: "",
                    nationality = dto.nationality ?: "",
                    aadhaarNo = dto.aadhaarNo ?: "",
                    contactMobile = dto.contactMobile ?: "",
                    contactEmail = dto.email ?: "",
                    emergencyContact = dto.emergencyContact ?: "",
                    emergencyPhone = dto.emergencyPhone ?: "",
                    fatherName = dto.fatherName ?: "",
                    fatherOccupation = dto.fatherOccupation ?: "",
                    fatherMobile = dto.fatherMobile ?: "",
                    fatherEmail = dto.fatherEmail ?: "",
                    motherName = dto.motherName ?: "",
                    motherOccupation = dto.motherOccupation ?: "",
                    motherMobile = dto.motherMobile ?: "",
                    motherEmail = dto.motherEmail ?: "",
                    certifications = (dto.certifications ?: emptyList()).map {
                        ChildCertification(it.title, it.issuer, it.category, it.date, it.status)
                    }
                )
            } else {
                throw Exception("Failed to fetch child profile: ${response.code()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getInternalMarks(childId: String?): List<ChildInternalMark> {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildMarks(idToUse)
            if (response.isSuccessful && response.body() != null) {
                (response.body() ?: emptyList()).map {
                    ChildInternalMark(
                        subjectName = it.subjectName,
                        academicYear = it.academicYear,
                        internalExamMark = it.internalExamMark.toString(),
                        assignmentMark = it.assignmentMark.toString(),
                        presentationMark = it.presentationMark.toString(),
                        vivaVoiceMark = it.vivaVoiceMark.toString(),
                        attendanceMark = it.attendanceMark.toString(),
                        totalMark = it.totalMark.toString()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPerformanceAnalytics(childId: String?): List<PerformanceData> {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildPerformance(idToUse)
            if (response.isSuccessful && response.body() != null) {
                (response.body() ?: emptyList()).map {
                    PerformanceData(it.semester, it.gpa, it.attendance)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFeeStatus(childId: String?): ChildFeeLedger {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildFees(idToUse)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body() ?: throw Exception("Body disappeared after null check")
                ChildFeeLedger(
                    totalFees = dto.totalFees,
                    scholarshipDeduction = dto.scholarshipDeduction,
                    otherDeductions = dto.otherDeductions,
                    netFees = dto.netFees,
                    amountPaid = dto.amountPaid,
                    pendingBalance = dto.pendingBalance,
                    dueDate = dto.dueDate,
                    records = (dto.records ?: emptyList()).map {
                        FeeLedgerRecord(it.id, it.title, it.amount, it.date, it.status)
                    }
                )
            } else {
                throw Exception("Failed to fetch fee status: ${response.code()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getNotices(childId: String?): List<CollegeNotice> {
        return try {
            val response = apiService.getNotices()
            if (response.isSuccessful && response.body() != null) {
                (response.body() ?: emptyList()).map {
                    CollegeNotice(it.id, it.title, it.category ?: "General", "Medium", it.body, it.date ?: "N/A", it.date ?: "N/A", "All", "Admin", "CAMS")
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAttendance(childId: String?): AttendanceSummary? {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildAttendance(idToUse)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                AttendanceSummary(
                    percentage = body.percentage.toDouble(),
                    total = body.total,
                    present = body.present,
                    absent = body.absent,
                    od = body.od,
                    records = (body.records ?: emptyList()).map {
                        AttendanceRecord(it.date, it.status.replaceFirstChar { char -> char.uppercase() })
                    }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getSubjectAttendance(childId: String?): List<SubjectAttendance> {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildSubjectAttendance(idToUse)
            if (response.isSuccessful && response.body() != null) {
                (response.body() ?: emptyList()).map {
                    SubjectAttendance(it.subject, it.totalClasses, it.attendedClasses, it.percentage)
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTimetable(childId: String?): List<TimetableDay> {
        return try {
            val idToUse = childId ?: currentChildId
            val response = apiService.getParentChildTimetable(idToUse)
            if (response.isSuccessful && response.body() != null) {
                val slots = response.body() ?: emptyList()
                slots.groupBy { it.dayOfWeek }.map { (day, slotsForDay) ->
                    TimetableDay(
                        dayName = day,
                        periods = slotsForDay.mapIndexed { index, slot ->
                            TimetablePeriod(
                                periodNo = index + 1,
                                time = "${slot.startTime} - ${slot.endTime}",
                                subjectName = slot.subjectName,
                                subjectCode = slot.subjectCode,
                                room = slot.roomNo,
                                instructor = slot.facultyName
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

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        try {
            val response = apiService.changePassword(
                com.example.core.network.ChangePasswordRequest(currentPassword, newPassword)
            )
            if (!response.isSuccessful) {
                throw Exception("Failed to change password: ${response.code()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
