package com.example.core.repository

import com.example.core.network.CreateOrderResponseDto
import com.example.core.network.VerifyPaymentResponseDto
import com.example.features.parent.models.*
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface ParentRepository {
    val selectedChildId: Flow<String?>
    suspend fun setSelectedChildId(childId: String)
    suspend fun getChildrenList(): List<ChildSummary>
    suspend fun getChildProfile(childId: String? = null): ChildProfileExtended
    suspend fun getInternalMarks(childId: String? = null): List<ChildInternalMark>
    suspend fun getPerformanceAnalytics(childId: String? = null): List<PerformanceData>
    suspend fun getFeeStatus(childId: String? = null): ChildFeeLedger
    suspend fun getNotices(childId: String? = null): List<CollegeNotice>
    suspend fun submitInquiry(name: String, email: String, subject: String, message: String): Boolean
    suspend fun getCollegeInfo(): com.example.core.network.CollegeInfoDto?
    suspend fun getAttendance(childId: String? = null): AttendanceSummary?
    suspend fun getSubjectAttendance(childId: String? = null): List<SubjectAttendance>
    suspend fun getTimetable(childId: String? = null): List<TimetableDay>
    suspend fun changePassword(currentPassword: String, newPassword: String)
    suspend fun createFeeOrder(recordId: String, amount: Double, childId: String? = null): Response<CreateOrderResponseDto>
    suspend fun verifyFeePayment(
        recordId: String,
        orderId: String,
        paymentId: String,
        signature: String,
        childId: String? = null
    ): Response<VerifyPaymentResponseDto>
}
