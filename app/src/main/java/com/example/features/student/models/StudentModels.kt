package com.example.features.student.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetricSchema(
    val id: String,
    val label: String,
    val value: String
)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    val metrics: List<MetricSchema>
)

@JsonClass(generateAdapter = true)
data class StudentProfileResponse(
    val id: String,
    @Json(name = "roll_no") val rollNo: String,
    val semester: Int,
    @Json(name = "batch_year") val batchYear: Int,
    val email: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "mentor_name") val mentorName: String?,
    @Json(name = "mentor_email") val mentorEmail: String?,
    @Json(name = "mentor_phone") val mentorPhone: String?,
    val cgpa: Double?,
    val skills: List<String>?,
    @Json(name = "course_name") val courseName: String?,
    val section: String?,
    @Json(name = "class_advisor_name") val classAdvisorName: String?,
    @Json(name = "class_advisor_email") val classAdvisorEmail: String?,
    @Json(name = "class_advisor_phone") val classAdvisorPhone: String?,
    val batch: String?,
    @Json(name = "year_of_study") val yearOfStudy: String?,
    @Json(name = "department_name") val departmentName: String?,
    
    // Extended Profile Fields
    @Json(name = "verification_status") val verificationStatus: String? = "DRAFT",
    @Json(name = "date_of_birth") val dateOfBirth: String? = null,
    val gender: String? = null,
    @Json(name = "blood_group") val bloodGroup: String? = null,
    val nationality: String? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,
    @Json(name = "current_address") val currentAddress: String? = null,
    @Json(name = "permanent_address") val permanentAddress: String? = null,
    @Json(name = "aadhaar_number") val aadhaarNumber: String? = null,
    @Json(name = "passport_number") val passportNumber: String? = null,
    @Json(name = "community_category") val communityCategory: String? = null,
    val religion: String? = null,
    @Json(name = "emergency_contact_name") val emergencyContactName: String? = null,
    @Json(name = "emergency_contact_relationship") val emergencyContactRelationship: String? = null,
    @Json(name = "emergency_contact_number") val emergencyContactNumber: String? = null,
    @Json(name = "father_name") val fatherName: String? = null,
    @Json(name = "father_occupation") val fatherOccupation: String? = null,
    @Json(name = "father_mobile") val fatherMobile: String? = null,
    @Json(name = "father_email") val fatherEmail: String? = null,
    @Json(name = "father_office_address") val fatherOfficeAddress: String? = null,
    @Json(name = "mother_name") val motherName: String? = null,
    @Json(name = "mother_occupation") val motherOccupation: String? = null,
    @Json(name = "mother_mobile") val motherMobile: String? = null,
    @Json(name = "mother_email") val motherEmail: String? = null,
    @Json(name = "mother_office_address") val motherOfficeAddress: String? = null,
    @Json(name = "parent_annual_income") val parentAnnualIncome: String? = null,
    @Json(name = "languages_known") val languagesKnown: List<String>? = null,
    @Json(name = "hobbies_interests") val hobbiesInterests: List<String>? = null,
    @Json(name = "special_skills") val specialSkills: List<String>? = null,
    @Json(name = "medical_info") val medicalInfo: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null,
    @Json(name = "moot_courts") val mootCourts: List<MootCourt>? = null,
    val internships: List<Internship>? = null,
    @Json(name = "edit_request_status") val editRequestStatus: String? = null,
    @Json(name = "edit_request_reason") val editRequestReason: String? = null,
    @Json(name = "staff_remarks") val staffRemarks: String? = null,
    @Json(name = "hod_remarks") val hodRemarks: String? = null,
    
    // Document URLs
    @Json(name = "document_aadhaar_url") val documentAadhaarUrl: String? = null,
    @Json(name = "document_community_url") val documentCommunityUrl: String? = null,
    @Json(name = "document_tc_url") val documentTcUrl: String? = null,
    @Json(name = "document_other_url") val documentOtherUrl: String? = null,
    
    // Additional Profile Fields from React
    @Json(name = "publications") val publications: List<ResearchPublication>? = null,
    @Json(name = "skill_assessment") val skillAssessment: List<SkillAssessment>? = null,
    @Json(name = "aibe_readiness") val aibeReadiness: Int? = null
)

@JsonClass(generateAdapter = true)
data class ResearchPublication(
    val title: String,
    val journal: String,
    val year: String,
    val coAuthors: String?,
    @Json(name = "document_url") val documentUrl: String?
)

@JsonClass(generateAdapter = true)
data class SkillAssessment(
    val skill: String,
    val level: Int,
    val fullMark: Int = 100
)

@JsonClass(generateAdapter = true)
data class MootCourt(
    val name: String,
    val role: String,
    val rank: String,
    val date: String
)

@JsonClass(generateAdapter = true)
data class Internship(
    val organization: String?,
    val company: String?,
    val role: String?,
    val startDate: String?,
    val endDate: String?,
    val duration: String?,
    val responsibilities: String?,
    val details: String?,
    val status: String?
)

@JsonClass(generateAdapter = true)
data class MentorshipRecord(
    @Json(name = "meeting_log") val meetingLog: String?,
    @Json(name = "academic_review") val academicReview: String?,
    @Json(name = "improvement_plan") val improvementPlan: String?,
    val remarks: String?,
    @Json(name = "follow_up") val followUp: String?
)

@JsonClass(generateAdapter = true)
data class AttendanceResponse(
    val percentage: Int?
)

@JsonClass(generateAdapter = true)
data class Course(
    val id: String,
    val name: String,
    val code: String,
    val credits: Int,
    @Json(name = "overall_completion") val overallCompletion: Int = 0
)

@JsonClass(generateAdapter = true)
data class Notice(
    val id: String,
    val title: String,
    val category: String,
    @Json(name = "publisher_name") val publisherName: String,
    @Json(name = "publish_date") val publishDate: String,
    val priority: String // High, Medium, Low
)

data class CalendarEvent(
    val id: String,
    val title: String,
    val startDate: String,
    val category: String,
    val isHoliday: Boolean = false
)

data class LibraryBook(
    val title: String,
    val author: String,
    val dueDate: String
)

data class CareerStatus(
    val firm: String,
    val role: String,
    val status: String,
    val colorType: String // "purple", "emerald"
)
