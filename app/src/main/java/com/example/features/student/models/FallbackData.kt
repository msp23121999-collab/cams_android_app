package com.example.features.student.models

object FallbackData {
    val dashboard = DashboardResponse(
        metrics = listOf(
            MetricSchema("1", "Attendance", "85%"),
            MetricSchema("2", "CGPA", "8.5"),
            MetricSchema("3", "Upcoming Exams", "3"),
            MetricSchema("4", "Pending Assignments", "2")
        )
    )
    
    val profile = StudentProfileResponse(
        id = "1", rollNo = "101", semester = 5, batchYear = 2021, email = "student@cams.edu",
        fullName = "Offline Student", mentorName = "Dr. Smith", mentorEmail = "smith@cams.edu", mentorPhone = "1234567890",
        cgpa = 8.5, skills = listOf("Java", "Kotlin"), courseName = "B.Tech", section = "A",
        classAdvisorName = "Prof. Jane", classAdvisorEmail = "jane@cams.edu", classAdvisorPhone = "0987654321",
        batch = "2021-2025", yearOfStudy = "3rd Year", departmentName = "CSE", verificationStatus = "VERIFIED",
        dateOfBirth = "2000-01-01", gender = "Male", bloodGroup = "O+", nationality = "Indian",
        mobileNumber = "1234567890", currentAddress = "Offline City", permanentAddress = "Offline City",
        aadhaarNumber = "1234 5678 9012", communityCategory = "General", fatherName = "Father", motherName = "Mother"
    )
    
    val courses = listOf(
        Course(id = "1", name = "Data Structures", code = "CS101", credits = 4)
    )
    
    val notices = listOf(
        Notice(id = "1", title = "Mid Semester Exams", category = "Exam", publisherName = "Admin", publishDate = "2024-03-01", priority = "High")
    )
    
    val calendarEvents = listOf(
        CalendarEvent(id = "1", title = "Mid Term Exams", startDate = "2024-03-01", category = "Exam")
    )
    
    val borrowedBooks = listOf(
        LibraryBook(title = "Introduction to Algorithms", author = "CLRS", dueDate = "2024-02-15")
    )
}
