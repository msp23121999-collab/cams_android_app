package com.example.features.academics.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Attachment(
    val name: String,
    val url: String,
    val size: String,
    val type: String
)

@JsonClass(generateAdapter = true)
data class Evaluation(
    val marksObtained: Double,
    val totalMarks: Int,
    val grade: String,
    val feedback: String,
    val remarks: String,
    val status: String,
    val gradedDate: String,
    val gradedBy: String
)

@JsonClass(generateAdapter = true)
data class SubmissionHistory(
    val submissionDate: String,
    val submittedFileName: String,
    val submittedFileUrl: String,
    val submittedFileSize: String,
    val submittedText: String? = null
)

@JsonClass(generateAdapter = true)
data class Submission(
    val id: String,
    val assignmentId: String,
    val submissionDate: String,
    val status: String,
    val submittedFileName: String,
    val submittedFileUrl: String,
    val submittedFileSize: String,
    val submittedText: String? = null,
    val evaluation: Evaluation? = null,
    val history: List<SubmissionHistory> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Assignment(
    val id: String,
    val title: String,
    val type: String, // "Case Law Analysis", "Legal Research Assignment", etc.
    val subject: String,
    val unit: String? = null,
    val topic: String? = null,
    val description: String,
    val instructions: String? = null,
    val totalMarks: Int = 100,
    val issueDate: String,
    val deadline: String,
    val status: String, // "Published", "Closed"
    val facultyName: String,
    val semester: String? = null,
    val section: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val mySubmission: Submission? = null
)

@JsonClass(generateAdapter = true)
data class StudyMaterial(
    val id: String,
    val title: String,
    val description: String = "",
    val subject: String,
    val unit: String? = null,
    val topic: String? = null,
    val category: String, // "Lecture Notes", "PPT", "Case Law"
    val keywords: List<String> = emptyList(),
    val uploadDate: String,
    val fileUrl: String,
    val fileFormat: String = "PDF",
    val status: String = "Approved",
    val facultyName: String = "",
    val isVideo: Boolean = false,
    val attachments: List<Attachment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class InternalMarkRecord(
    val id: String,
    val subjectName: String,
    val examScore: Double,
    val assignmentScore: Double,
    val presentationScore: Double,
    val vivaScore: Double,
    val attendanceScore: Double,
    val totalScore: Double,
    val isApproved: Boolean = true,
    val hodMessage: String? = null,
    val facultyReply: String? = null,
    val facultyComments: String? = null
)
