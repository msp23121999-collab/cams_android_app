package com.example.core.di

import android.content.Context
import androidx.room.Room
import com.example.core.database.CamsDatabase
import com.example.core.network.ApiClient
import com.example.core.network.AuthInterceptor
import com.example.core.network.AuthManager
import com.example.core.network.AuthManagerImpl
import com.example.core.network.CamsApiService
import com.example.core.repository.AuthRepository
import com.example.core.repository.OfflineFirstAuthRepository
import com.example.core.repository.AttendanceRepository
import com.example.core.repository.OfflineFirstAttendanceRepository
import com.example.core.repository.StudentRepository
import com.example.core.repository.StudentRepositoryImpl
import com.example.core.repository.ParentRepository
import com.example.core.repository.ParentRepositoryImpl
import com.example.core.datastore.ParentPreferences
import com.example.core.repository.FacultyRepository
import com.example.core.repository.FacultyRepositoryImpl
import com.example.core.repository.HODRepository
import com.example.core.repository.HODRepositoryImpl
import com.example.core.repository.PrincipalRepository
import com.example.core.repository.PrincipalRepositoryImpl
import com.example.core.repository.AdminRepository
import com.example.core.repository.AdminRepositoryImpl

import com.example.core.network.NetworkMonitor

import com.example.core.database.dao.UsersDao
import com.example.core.database.dao.DepartmentsDao
import com.example.core.database.dao.AcademicYearsDao
import com.example.core.database.dao.ActivityLogsDao
import com.example.core.database.dao.CoursesDao
import com.example.core.database.dao.FacultyProfilesDao
import com.example.core.database.dao.StudentsDao
import com.example.core.database.dao.SectionsDao
import com.example.core.database.dao.AssignmentsDao
import com.example.core.database.dao.AttendanceDao
import com.example.core.database.dao.AcademicEventsDao
import com.example.core.database.dao.AttendanceCorrectionsDao
import com.example.core.database.dao.AuditLogsDao
import com.example.core.database.dao.BackupConfigurationsDao
import com.example.core.database.dao.BackupHistoryDao
import com.example.core.database.dao.ChatMessagesDao
import com.example.core.database.dao.ChatSessionsDao
import com.example.core.database.dao.ClassAdvisorsDao
import com.example.core.database.dao.ClubMembershipsDao
import com.example.core.database.dao.ClubsDao
import com.example.core.database.dao.DeductionsDao
import com.example.core.database.dao.ExamSettingsDao
import com.example.core.database.dao.ExamsDao
import com.example.core.database.dao.FacultyAbsencesDao
import com.example.core.database.dao.FacultyAssignmentsDao
import com.example.core.database.dao.FacultyResearchDao
import com.example.core.database.dao.FacultyWorkloadDao
import com.example.core.database.dao.FeeRecordsDao
import com.example.core.database.dao.FeeStructureDao
import com.example.core.database.dao.GrievancesDao
import com.example.core.database.dao.InternshipApplicationsDao
import com.example.core.database.dao.InternshipDrivesDao
import com.example.core.database.dao.JsonAssignmentsDao
import com.example.core.database.dao.LeaveApprovalsDao
import com.example.core.database.dao.LeaveBalancesDao
import com.example.core.database.dao.LeavesDao
import com.example.core.database.dao.LectureRecordingsDao
import com.example.core.database.dao.LegalEventsDao
import com.example.core.database.dao.MarksDao
import com.example.core.database.dao.MessagesDao
import com.example.core.database.dao.NoticeAcknowledgementsDao
import com.example.core.database.dao.NoticesDao
import com.example.core.database.dao.NotificationsDao
import com.example.core.database.dao.ParentStudentMapDao
import com.example.core.database.dao.PaymentsDao
import com.example.core.database.dao.RegulationsDao
import com.example.core.database.dao.SalaryDao
import com.example.core.database.dao.SalarySlipsDao
import com.example.core.database.dao.StaffAttendanceDao
import com.example.core.database.dao.StudyMaterialsDao
import com.example.core.database.dao.SystemSettingHistoryDao
import com.example.core.database.dao.SystemSettingsDao
import com.example.core.database.dao.TimetableApprovalsDao
import com.example.core.database.dao.TimetableDao
import com.example.core.database.dao.WorkingDayConfigDao

interface AppContainer {
    val networkMonitor: NetworkMonitor
    val authManager: AuthManager
    val database: CamsDatabase
    val apiService: CamsApiService
    val authRepository: AuthRepository
    val attendanceRepository: AttendanceRepository
    val studentRepository: StudentRepository
    val parentRepository: ParentRepository
    val facultyRepository: FacultyRepository
    val hodRepository: HODRepository
    val principalRepository: PrincipalRepository
    val adminRepository: AdminRepository
        val usersDao: UsersDao
    val departmentsDao: DepartmentsDao
    val academicYearsDao: AcademicYearsDao
    val activityLogsDao: ActivityLogsDao
    val coursesDao: CoursesDao
    val facultyProfilesDao: FacultyProfilesDao
    val studentsDao: StudentsDao
    val sectionsDao: SectionsDao
    val assignmentsDao: AssignmentsDao
    val attendanceDao: AttendanceDao
    val academicEventsDao: AcademicEventsDao
    val attendanceCorrectionsDao: AttendanceCorrectionsDao
    val auditLogsDao: AuditLogsDao
    val backupConfigurationsDao: BackupConfigurationsDao
    val backupHistoryDao: BackupHistoryDao
    val chatMessagesDao: ChatMessagesDao
    val chatSessionsDao: ChatSessionsDao
    val classAdvisorsDao: ClassAdvisorsDao
    val clubMembershipsDao: ClubMembershipsDao
    val clubsDao: ClubsDao
    val deductionsDao: DeductionsDao
    val examSettingsDao: ExamSettingsDao
    val examsDao: ExamsDao
    val facultyAbsencesDao: FacultyAbsencesDao
    val facultyAssignmentsDao: FacultyAssignmentsDao
    val facultyResearchDao: FacultyResearchDao
    val facultyWorkloadDao: FacultyWorkloadDao
    val feeRecordsDao: FeeRecordsDao
    val feeStructureDao: FeeStructureDao
    val grievancesDao: GrievancesDao
    val internshipApplicationsDao: InternshipApplicationsDao
    val internshipDrivesDao: InternshipDrivesDao
    val jsonAssignmentsDao: JsonAssignmentsDao
    val leaveApprovalsDao: LeaveApprovalsDao
    val leaveBalancesDao: LeaveBalancesDao
    val leavesDao: LeavesDao
    val lectureRecordingsDao: LectureRecordingsDao
    val legalEventsDao: LegalEventsDao
    val marksDao: MarksDao
    val messagesDao: MessagesDao
    val noticeAcknowledgementsDao: NoticeAcknowledgementsDao
    val noticesDao: NoticesDao
    val notificationsDao: NotificationsDao
    val parentStudentMapDao: ParentStudentMapDao
    val paymentsDao: PaymentsDao
    val regulationsDao: RegulationsDao
    val salaryDao: SalaryDao
    val salarySlipsDao: SalarySlipsDao
    val staffAttendanceDao: StaffAttendanceDao
    val studyMaterialsDao: StudyMaterialsDao
    val systemSettingHistoryDao: SystemSettingHistoryDao
    val systemSettingsDao: SystemSettingsDao
    val timetableApprovalsDao: TimetableApprovalsDao
    val timetableDao: TimetableDao
    val workingDayConfigDao: WorkingDayConfigDao
    fun provideAdminUserViewModelFactory(): com.example.features.admin.providers.AdminUserViewModelFactory
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(context)
    }

    override val authManager: AuthManager by lazy {
        AuthManagerImpl(context)
    }
    
    override val database: CamsDatabase by lazy {
        Room.databaseBuilder(
            context,
            CamsDatabase::class.java,
            "cams_database"
        ).build()
    }

    override val apiService: CamsApiService by lazy {
        val authInterceptor = AuthInterceptor(authManager)
        val okHttpClient = ApiClient.createHttpClient(authInterceptor, database.apiCacheDao())
        val retrofit = ApiClient.createRetrofit(okHttpClient)
        ApiClient.createApiService(retrofit)
    }
    
    override val authRepository: AuthRepository by lazy {
        OfflineFirstAuthRepository(
            apiService = apiService,
            userDao = database.usersDao(),
            authManager = authManager
        )
    }

    override val attendanceRepository: AttendanceRepository by lazy {
        OfflineFirstAttendanceRepository(database.attendanceDao(), apiService)
    }

    override val studentRepository: StudentRepository by lazy {
        StudentRepositoryImpl(apiService)
    }

    override val parentRepository: ParentRepository by lazy {
        ParentRepositoryImpl(apiService, ParentPreferences(context))
    }

    override val facultyRepository: FacultyRepository by lazy {
        FacultyRepositoryImpl(apiService)
    }

    override val hodRepository: HODRepository by lazy {
        HODRepositoryImpl(apiService)
    }
    
    override val principalRepository: PrincipalRepository by lazy {
        PrincipalRepositoryImpl(apiService)
    }
    
    override val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(apiService)
    }

        override val usersDao: UsersDao by lazy { database.usersDao() }
    override val departmentsDao: DepartmentsDao by lazy { database.departmentsDao() }
    override val academicYearsDao: AcademicYearsDao by lazy { database.academicYearsDao() }
    override val activityLogsDao: ActivityLogsDao by lazy { database.activityLogsDao() }
    override val coursesDao: CoursesDao by lazy { database.coursesDao() }
    override val facultyProfilesDao: FacultyProfilesDao by lazy { database.facultyProfilesDao() }
    override val studentsDao: StudentsDao by lazy { database.studentsDao() }
    override val sectionsDao: SectionsDao by lazy { database.sectionsDao() }
    override val assignmentsDao: AssignmentsDao by lazy { database.assignmentsDao() }
    override val attendanceDao: AttendanceDao by lazy { database.attendanceDao() }
    override val academicEventsDao: AcademicEventsDao by lazy { database.academicEventsDao() }
    override val attendanceCorrectionsDao: AttendanceCorrectionsDao by lazy { database.attendanceCorrectionsDao() }
    override val auditLogsDao: AuditLogsDao by lazy { database.auditLogsDao() }
    override val backupConfigurationsDao: BackupConfigurationsDao by lazy { database.backupConfigurationsDao() }
    override val backupHistoryDao: BackupHistoryDao by lazy { database.backupHistoryDao() }
    override val chatMessagesDao: ChatMessagesDao by lazy { database.chatMessagesDao() }
    override val chatSessionsDao: ChatSessionsDao by lazy { database.chatSessionsDao() }
    override val classAdvisorsDao: ClassAdvisorsDao by lazy { database.classAdvisorsDao() }
    override val clubMembershipsDao: ClubMembershipsDao by lazy { database.clubMembershipsDao() }
    override val clubsDao: ClubsDao by lazy { database.clubsDao() }
    override val deductionsDao: DeductionsDao by lazy { database.deductionsDao() }
    override val examSettingsDao: ExamSettingsDao by lazy { database.examSettingsDao() }
    override val examsDao: ExamsDao by lazy { database.examsDao() }
    override val facultyAbsencesDao: FacultyAbsencesDao by lazy { database.facultyAbsencesDao() }
    override val facultyAssignmentsDao: FacultyAssignmentsDao by lazy { database.facultyAssignmentsDao() }
    override val facultyResearchDao: FacultyResearchDao by lazy { database.facultyResearchDao() }
    override val facultyWorkloadDao: FacultyWorkloadDao by lazy { database.facultyWorkloadDao() }
    override val feeRecordsDao: FeeRecordsDao by lazy { database.feeRecordsDao() }
    override val feeStructureDao: FeeStructureDao by lazy { database.feeStructureDao() }
    override val grievancesDao: GrievancesDao by lazy { database.grievancesDao() }
    override val internshipApplicationsDao: InternshipApplicationsDao by lazy { database.internshipApplicationsDao() }
    override val internshipDrivesDao: InternshipDrivesDao by lazy { database.internshipDrivesDao() }
    override val jsonAssignmentsDao: JsonAssignmentsDao by lazy { database.jsonAssignmentsDao() }
    override val leaveApprovalsDao: LeaveApprovalsDao by lazy { database.leaveApprovalsDao() }
    override val leaveBalancesDao: LeaveBalancesDao by lazy { database.leaveBalancesDao() }
    override val leavesDao: LeavesDao by lazy { database.leavesDao() }
    override val lectureRecordingsDao: LectureRecordingsDao by lazy { database.lectureRecordingsDao() }
    override val legalEventsDao: LegalEventsDao by lazy { database.legalEventsDao() }
    override val marksDao: MarksDao by lazy { database.marksDao() }
    override val messagesDao: MessagesDao by lazy { database.messagesDao() }
    override val noticeAcknowledgementsDao: NoticeAcknowledgementsDao by lazy { database.noticeAcknowledgementsDao() }
    override val noticesDao: NoticesDao by lazy { database.noticesDao() }
    override val notificationsDao: NotificationsDao by lazy { database.notificationsDao() }
    override val parentStudentMapDao: ParentStudentMapDao by lazy { database.parentStudentMapDao() }
    override val paymentsDao: PaymentsDao by lazy { database.paymentsDao() }
    override val regulationsDao: RegulationsDao by lazy { database.regulationsDao() }
    override val salaryDao: SalaryDao by lazy { database.salaryDao() }
    override val salarySlipsDao: SalarySlipsDao by lazy { database.salarySlipsDao() }
    override val staffAttendanceDao: StaffAttendanceDao by lazy { database.staffAttendanceDao() }
    override val studyMaterialsDao: StudyMaterialsDao by lazy { database.studyMaterialsDao() }
    override val systemSettingHistoryDao: SystemSettingHistoryDao by lazy { database.systemSettingHistoryDao() }
    override val systemSettingsDao: SystemSettingsDao by lazy { database.systemSettingsDao() }
    override val timetableApprovalsDao: TimetableApprovalsDao by lazy { database.timetableApprovalsDao() }
    override val timetableDao: TimetableDao by lazy { database.timetableDao() }
    override val workingDayConfigDao: WorkingDayConfigDao by lazy { database.workingDayConfigDao() }
    override fun provideAdminUserViewModelFactory(): com.example.features.admin.providers.AdminUserViewModelFactory {
        return com.example.features.admin.providers.AdminUserViewModelFactory(adminRepository)
    }
}
