package com.example.features.parent.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChildProfile(
    val id: String,
    val fullName: String
)

@JsonClass(generateAdapter = true)
data class ChildSummary(
    val id: String,
    val fullName: String,
    val rollNo: String?,
    val profilePhotoUrl: String?
)

@JsonClass(generateAdapter = true)
data class ChildInternalMark(
    val subjectName: String,
    val academicYear: String,
    val internalExamMark: String,
    val assignmentMark: String,
    val presentationMark: String,
    val vivaVoiceMark: String,
    val attendanceMark: String,
    val totalMark: String
)

@JsonClass(generateAdapter = true)
data class PerformanceData(
    val semester: String,
    val cgpa: Double,
    val attendance: Int
)

@JsonClass(generateAdapter = true)
data class FeeLedgerRecord(
    val recordId: String,
    val feeType: String,
    val amount: Double,
    val dueDate: String?,
    val status: String // "paid", "partially_paid", "pending"
)

@JsonClass(generateAdapter = true)
data class ChildFeeLedger(
    val totalFees: Double,
    val scholarshipDeduction: Double,
    val otherDeductions: Double,
    val netFees: Double,
    val amountPaid: Double,
    val pendingBalance: Double,
    val dueDate: String?,
    val records: List<FeeLedgerRecord>
)

@JsonClass(generateAdapter = true)
data class CollegeNotice(
    val id: String,
    val title: String,
    val category: String, // e.g. "Academic Announcement"
    val priority: String, // "High", "Medium", "Low"
    val body: String,
    val publishDate: String,
    val expiryDate: String?,
    val audienceType: String,
    val publisherName: String,
    val publisherRole: String?
)

@JsonClass(generateAdapter = true)
data class SubjectAttendance(
    val subject: String,
    val totalClasses: Int,
    val present: Int,
    val percentage: Int
)

data class AttendanceSummary(
    val percentage: Double,
    val total: Int,
    val present: Int,
    val absent: Int,
    val od: Int,
    val records: List<AttendanceRecord>
)

@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    val date: String,
    val status: String // "Present", "Absent", "Holiday"
)

@JsonClass(generateAdapter = true)
data class ChildCertification(
    val title: String,
    val authority: String,
    val category: String,
    val date: String,
    val status: String // "Verified", "Pending"
)

@JsonClass(generateAdapter = true)
data class ChildProfileExtended(
    val id: String,
    val fullName: String,
    val rollNo: String,
    val semester: String,
    val batch: String,
    val cgpa: Double,
    val mentorName: String,
    val mentorEmail: String,
    val mentorPhone: String,
    val dob: String,
    val gender: String,
    val bloodGroup: String,
    val nationality: String,
    val aadhaarNo: String,
    val contactMobile: String,
    val contactEmail: String,
    val emergencyContact: String,
    val emergencyPhone: String,
    val fatherName: String,
    val fatherOccupation: String,
    val fatherMobile: String,
    val fatherEmail: String,
    val motherName: String,
    val motherOccupation: String,
    val motherMobile: String,
    val motherEmail: String,
    val certifications: List<ChildCertification>
)

@JsonClass(generateAdapter = true)
data class TimetablePeriod(
    val periodNo: Int,
    val time: String,
    val subjectName: String,
    val subjectCode: String,
    val room: String,
    val instructor: String
)

@JsonClass(generateAdapter = true)
data class TimetableDay(
    val dayName: String,
    val periods: List<TimetablePeriod>
)
