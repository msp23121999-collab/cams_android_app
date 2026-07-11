package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.core.database.entities.*
import com.example.core.database.dao.*

@Database(
    entities = [
        BackupConfigurationsEntity::class,
        DepartmentsEntity::class,
        RegulationsEntity::class,
        SystemSettingsEntity::class,
        UsersEntity::class,
        WorkingDayConfigEntity::class,
        AcademicYearsEntity::class,
        ActivityLogsEntity::class,
        AuditLogsEntity::class,
        BackupHistoryEntity::class,
        CoursesEntity::class,
        FacultyAbsencesEntity::class,
        FacultyProfilesEntity::class,
        FacultyResearchEntity::class,
        FacultyWorkloadEntity::class,
        FeeStructureEntity::class,
        GrievancesEntity::class,
        LeaveBalancesEntity::class,
        LeavesEntity::class,
        MessagesEntity::class,
        NoticesEntity::class,
        NotificationsEntity::class,
        SalaryEntity::class,
        StaffAttendanceEntity::class,
        StudentsEntity::class,
        SystemSettingHistoryEntity::class,
        DeductionsEntity::class,
        ExamsEntity::class,
        FeeRecordsEntity::class,
        LeaveApprovalsEntity::class,
        ParentStudentMapEntity::class,
        SalarySlipsEntity::class,
        SectionsEntity::class,
        AssignmentsEntity::class,
        AttendanceEntity::class,
        ExamSettingsEntity::class,
        MarksEntity::class,
        PaymentsEntity::class,
        StudyMaterialsEntity::class,
        TimetableEntity::class,
        TimetableApprovalsEntity::class,
        ChatSessionsEntity::class,
        ChatMessagesEntity::class,
        NoticeAcknowledgementsEntity::class,
        ClassAdvisorsEntity::class,
        FacultyAssignmentsEntity::class,
        AcademicEventsEntity::class,
        JsonAssignmentsEntity::class,
        AttendanceCorrectionsEntity::class,
        ClubsEntity::class,
        ClubMembershipsEntity::class,
        InternshipApplicationsEntity::class,
        InternshipDrivesEntity::class,
        LectureRecordingsEntity::class,
        LegalEventsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CamsDatabase : RoomDatabase() {
    abstract fun backupConfigurationsDao(): BackupConfigurationsDao
    abstract fun departmentsDao(): DepartmentsDao
    abstract fun regulationsDao(): RegulationsDao
    abstract fun systemSettingsDao(): SystemSettingsDao
    abstract fun usersDao(): UsersDao
    abstract fun workingDayConfigDao(): WorkingDayConfigDao
    abstract fun academicYearsDao(): AcademicYearsDao
    abstract fun activityLogsDao(): ActivityLogsDao
    abstract fun auditLogsDao(): AuditLogsDao
    abstract fun backupHistoryDao(): BackupHistoryDao
    abstract fun coursesDao(): CoursesDao
    abstract fun facultyAbsencesDao(): FacultyAbsencesDao
    abstract fun facultyProfilesDao(): FacultyProfilesDao
    abstract fun facultyResearchDao(): FacultyResearchDao
    abstract fun facultyWorkloadDao(): FacultyWorkloadDao
    abstract fun feeStructureDao(): FeeStructureDao
    abstract fun grievancesDao(): GrievancesDao
    abstract fun leaveBalancesDao(): LeaveBalancesDao
    abstract fun leavesDao(): LeavesDao
    abstract fun messagesDao(): MessagesDao
    abstract fun noticesDao(): NoticesDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun salaryDao(): SalaryDao
    abstract fun staffAttendanceDao(): StaffAttendanceDao
    abstract fun studentsDao(): StudentsDao
    abstract fun systemSettingHistoryDao(): SystemSettingHistoryDao
    abstract fun deductionsDao(): DeductionsDao
    abstract fun examsDao(): ExamsDao
    abstract fun feeRecordsDao(): FeeRecordsDao
    abstract fun leaveApprovalsDao(): LeaveApprovalsDao
    abstract fun parentStudentMapDao(): ParentStudentMapDao
    abstract fun salarySlipsDao(): SalarySlipsDao
    abstract fun sectionsDao(): SectionsDao
    abstract fun assignmentsDao(): AssignmentsDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun examSettingsDao(): ExamSettingsDao
    abstract fun marksDao(): MarksDao
    abstract fun paymentsDao(): PaymentsDao
    abstract fun studyMaterialsDao(): StudyMaterialsDao
    abstract fun timetableDao(): TimetableDao
    abstract fun timetableApprovalsDao(): TimetableApprovalsDao
    abstract fun chatSessionsDao(): ChatSessionsDao
    abstract fun chatMessagesDao(): ChatMessagesDao
    abstract fun noticeAcknowledgementsDao(): NoticeAcknowledgementsDao
    abstract fun classAdvisorsDao(): ClassAdvisorsDao
    abstract fun facultyAssignmentsDao(): FacultyAssignmentsDao
    abstract fun academicEventsDao(): AcademicEventsDao
    abstract fun jsonAssignmentsDao(): JsonAssignmentsDao
    abstract fun attendanceCorrectionsDao(): AttendanceCorrectionsDao
    abstract fun clubsDao(): ClubsDao
    abstract fun clubMembershipsDao(): ClubMembershipsDao
    abstract fun internshipApplicationsDao(): InternshipApplicationsDao
    abstract fun internshipDrivesDao(): InternshipDrivesDao
    abstract fun lectureRecordingsDao(): LectureRecordingsDao
    abstract fun legalEventsDao(): LegalEventsDao
}
