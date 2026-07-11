package com.example.features.faculty.models

data class FacultyDashboardMetrics(
    val classesToday: String = "0",
    val pendingAttendance: String = "0",
    val pendingAssignments: String = "0",
    val leaveBalance: String = "0"
)

data class FacultySubject(
    val subjectCode: String,
    val subjectName: String,
    val degreeCode: String? = null,
    val section: String = "A",
    val year: Int,
    val semester: Int,
    val batch: String
)

data class FacultyProfile(
    val facultyId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val designation: String = "",
    val departmentName: String = "",
    val email: String = "",
    val phone: String = "",
    val employeeCode: String = "",
    val employmentStatus: String = "Active",
    val facultyType: String = "Permanent",
    val profilePhotoUrl: String? = null,
    val gender: String = "",
    val dateOfBirth: String = "",
    val bloodGroup: String = "",
    val nationality: String = "",
    val maritalStatus: String = "",
    val community: String = "",
    val alternatePhone: String = "",
    val personalEmail: String = "",
    val currentAddress: String = "",
    val permanentAddress: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val dateOfJoining: String = "",
    val specialization: String = "",
    val educationalQualifications: List<Qualification> = emptyList(),
    val experienceDetails: List<Experience> = emptyList(),
    val certifications: List<String> = emptyList(),
    val responsibilities: List<String> = emptyList()
)

data class Qualification(
    val degree: String,
    val specialization: String,
    val university: String,
    val institution: String,
    val yearOfCompletion: Int,
    val percentageCgpa: String
)

data class Experience(
    val institutionName: String,
    val designation: String,
    val fromDate: String,
    val toDate: String,
    val totalYears: Double
)

data class ResearchEntry(
    val id: String = "",
    val title: String,
    val publication: String? = null,
    val grantAmount: Double? = null,
    val publisher: String? = null,
    val publicationDate: String? = null,
    val isbnIssn: String? = null,
    val researchType: String = "Journal Article"
)

data class ActivitySummary(
    val classesConducted: Int = 0,
    val attendanceMarked: Int = 0,
    val studyMaterialsUploaded: Int = 0,
    val assignmentsCreated: Int = 0,
    val leaveRequestsSubmitted: Int = 0
)
