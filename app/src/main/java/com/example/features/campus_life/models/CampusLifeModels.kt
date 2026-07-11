package com.example.features.campus_life.models

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

data class CampusLifeStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Long,
    val bgColor: Long
)

data class ExperienceModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val path: String,
    val gradientStart: Long,
    val gradientEnd: Long,
    val textColor: Long
)

data class CampusLifeEvent(
    val id: Int,
    val title: String,
    val type: String,
    val date: String,
    val time: String,
    val venue: String,
    val registered: Boolean,
    val participants: Int,
    val imageUrl: String
)

data class Achievement(
    val title: String,
    val description: String,
    val timeAgo: String,
    val icon: ImageVector,
    val color: Long,
    val bgColor: Long
)

data class CircularNotice(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val priority: String,
    val publishDate: String,
    val expiryDate: String? = null,
    val publisherName: String,
    val publisherRole: String? = null,
    val audienceType: String,
    val attachmentUrl: String? = null
)

data class Club(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val membersCount: Int,
    val role: String = "None",
    val president: String = "Dr. Sarah Mitchell",
    val contact: String = "clubs@lexnova.edu",
    val phone: String = "+91 98765 43210",
    val nextMeeting: String? = "Every Tuesday at 4:00 PM",
    val recentActivities: List<String> = emptyList(),
    val upcomingEvents: List<ClubEvent> = emptyList(),
    val gradientStart: Long = 0xFFF8FAFC,
    val gradientEnd: Long = 0xFFF1F5F9,
    val borderColor: Long = 0xFFE2E8F0,
    val icon: ImageVector
)

data class ClubEvent(
    val title: String,
    val date: String
)

data class ClubAnnouncement(
    val title: String,
    val club: String,
    val date: String,
    val isUrgent: Boolean
)

data class GrievanceTicket(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val priority: String,
    val status: String,
    val submissionDate: String,
    val resolutionDate: String? = null
)

data class CouncilRepresentative(
    val name: String,
    val role: String,
    val year: String,
    val imageUrl: String
)

data class CouncilInitiative(
    val id: Int,
    val title: String,
    val status: String,
    val progress: Float, // 0.0 to 1.0
    val category: String
)

data class StudentFeedback(
    val id: Int,
    val topic: String,
    val status: String,
    val votes: Int
)

data class Grievance(
    val id: String,
    val date: String,
    val studentName: String,
    val regNo: String,
    val category: String,
    val subject: String,
    val priority: String,
    val assignedOfficer: String,
    val status: String,
    val description: String,
    val resolutionDate: String = "-",
    val rating: Int = 0,
    val feedback: String = ""
)

enum class MeetingStatus(val label: String) {
    LIVE_NOW("Live Now"),
    UPCOMING("Upcoming"),
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    RESCHEDULED("Rescheduled")
}

enum class MeetingPlatform(val label: String) {
    ZOOM("Zoom"),
    GOOGLE_MEET("Google Meet"),
    TEAMS("Microsoft Teams"),
    CUSTOM("Custom")
}

data class OnlineMeeting(
    val id: String,
    val title: String,
    val category: String,
    val organizer: String,
    val date: String,
    val time: String,
    val duration: String,
    val platform: MeetingPlatform,
    val meetingLink: String,
    val status: MeetingStatus,
    val participants: Int,
    val attended: Boolean,
    val agenda: List<String>,
    val description: String,
    val notes: String = "",
    val recordingAvailable: Boolean = false,
    val recordingUrl: String? = null,
    val room: String? = null
)

// --- LexNova Models ---

data class LexNovaKPI(
    val label: String,
    val value: String,
    val subText: String,
    val icon: ImageVector,
    val color: Color
)

data class TimetableEntry(
    val time: String,
    val course: String,
    val professor: String,
    val type: String,
    val room: String,
    val isLive: Boolean = false
)

data class KnowledgeDocument(
    val title: String,
    val author: String,
    val type: String,
    val size: String,
    val icon: ImageVector,
    val color: Color
)

data class AlumniMentor(
    val name: String,
    val firm: String,
    val year: String,
    val imageUrl: String? = null
)

// --- Legal Events Models ---

enum class EventStatus(val label: String) {
    UPCOMING("Upcoming"),
    LIVE_NOW("Live Now"),
    REG_OPEN("Registration Open"),
    REG_CLOSED("Registration Closed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class Speaker(
    val name: String,
    val designation: String,
    val type: String,
    val bio: String,
    val initials: String
)

data class LegalEvent(
    val id: String,
    val title: String,
    val category: String,
    val speaker: Speaker,
    val date: String,
    val time: String,
    val duration: String,
    val status: EventStatus,
    val mode: String,
    val platform: String,
    val meetingLink: String,
    val totalSeats: Int,
    val availableSeats: Int,
    val registrationDeadline: String,
    val description: String,
    val agenda: List<String>,
    val activityPoints: Int,
    val recordingUrl: String? = null,
    val certificateAvailable: Boolean = true,
    val isRegistered: Boolean = false
)

data class DebateEntry(
    val id: String,
    val title: String,
    val topic: String,
    val date: String,
    val time: String,
    val status: String,
    val participants: Int,
    val maxParticipants: Int,
    val judges: List<String>,
    val registered: Boolean = false,
    val studentScore: Int? = null,
    val rank: Int? = null
)
// --- Legal Skills & LexSphere Models ---

data class LawCourse(
    val id: String,
    val title: String,
    val category: String,
    val duration: String,
    val credits: Int,
    val enrolled: Int,
    val instructor: String,
    val level: String
)

data class LearningProgress(
    val courseId: String,
    val progress: Int,
    val certificateEarned: Boolean
)

data class Workshop(
    val id: Int,
    val title: String,
    val speaker: String,
    val date: String,
    val time: String,
    val attendees: Int,
    val type: String
)

data class CaseStudy(
    val id: String,
    val title: String,
    val size: String,
    val desc: String,
    val type: String
)

data class MootActivity(
    val id: Int,
    val name: String,
    val role: String,
    val status: String,
    val date: String
)

data class InternshipDrive(
    val id: String,
    val name: String,
    val role: String,
    val compensation: String,
    val status: String,
    val date: String
)

data class InternshipApplication(
    val driveName: String,
    val role: String,
    val studentName: String,
    val studentEmail: String,
    val rollNumber: String,
    val phone: String,
    val cgpa: String,
    val resumeUrl: String,
    val sop: String,
    val appliedAt: String
)

data class InternshipRecord(
    val id: String,
    val organization: String,
    val type: String,
    val role: String,
    val startDate: String,
    val endDate: String,
    val supervisor: String,
    val responsibilities: String,
    val status: String,
    val certificateUrl: String? = null
)

data class CertificationRecord(
    val id: String,
    val title: String,
    val authority: String,
    val date: String,
    val category: String,
    val verified: Boolean,
    val type: String,
    val fileUrl: String? = null
)

data class ActivityPointApplication(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val claimedPoints: Int,
    val approvedPoints: Int? = null,
    val status: String,
    val description: String,
    val supportingDocument: String,
    val reviewedBy: String? = null,
    val reviewedAt: String? = null,
    val facultyRemarks: String? = null
)

data class CommunityServiceOpportunity(
    val id: Int,
    val title: String,
    val ngo: String,
    val date: String,
    val location: String,
    val spots: Int,
    val hours: String,
    val tags: List<String>
)

data class CommunityServiceLog(
    val id: Int,
    val title: String,
    val date: String,
    val hours: Int,
    val status: String,
    val certificate: Boolean
)

data class InnovationProject(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val mentor: String,
    val team: List<String>,
    val likes: Int,
    val comments: Int,
    val badges: List<String>
)

data class ResearchPaper(
    val id: String,
    val title: String,
    val abstract: String,
    val category: String,
    val guide: String,
    val team: List<String>,
    val submissionDate: String,
    val fileSize: String,
    val status: String,
    val featured: Boolean = false,
    val awards: List<String> = emptyList(),
    val fileUrl: String? = null
)

data class JudgeQuestion(
    val id: String,
    val eventId: String,
    val eventTitle: String,
    val question: String,
    val topic: String,
    val status: String,
    val submittedAt: String
)
