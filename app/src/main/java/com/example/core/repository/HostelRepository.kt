package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.core.network.HostelAllocationCreateRequest
import com.example.core.network.HostelAllocationDto
import com.example.core.network.HostelBlockCreateRequest
import com.example.core.network.HostelBlockDto
import com.example.core.network.HostelRoomCreateRequest
import com.example.core.network.HostelRoomDto
import java.io.IOException

interface HostelRepository {
    suspend fun getBlocks(): List<HostelBlockDto>
    suspend fun createBlock(request: HostelBlockCreateRequest)
    suspend fun deleteBlock(blockId: String)
    suspend fun getRooms(blockId: String? = null): List<HostelRoomDto>
    suspend fun createRoom(request: HostelRoomCreateRequest)
    suspend fun deleteRoom(roomId: String)
    suspend fun getAllocations(status: String? = null): List<HostelAllocationDto>
    suspend fun createAllocation(request: HostelAllocationCreateRequest)
    suspend fun vacateAllocation(allocationId: String)
}

class HostelRepositoryImpl(private val apiService: CamsApiService) : HostelRepository {

    private fun errorDetail(response: retrofit2.Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
    } catch (e: Exception) { null }

    override suspend fun getBlocks(): List<HostelBlockDto> {
        val response = apiService.getHostelBlocks()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load hostel blocks: ${response.code()}")
    }

    override suspend fun createBlock(request: HostelBlockCreateRequest) {
        val response = apiService.createHostelBlock(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create block (error ${response.code()})")
    }

    override suspend fun deleteBlock(blockId: String) {
        val response = apiService.deleteHostelBlock(blockId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete block (error ${response.code()})")
    }

    override suspend fun getRooms(blockId: String?): List<HostelRoomDto> {
        val response = apiService.getHostelRooms(blockId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load rooms: ${response.code()}")
    }

    override suspend fun createRoom(request: HostelRoomCreateRequest) {
        val response = apiService.createHostelRoom(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create room (error ${response.code()})")
    }

    override suspend fun deleteRoom(roomId: String) {
        val response = apiService.deleteHostelRoom(roomId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete room (error ${response.code()})")
    }

    override suspend fun getAllocations(status: String?): List<HostelAllocationDto> {
        val response = apiService.getHostelAllocations(status)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load allocations: ${response.code()}")
    }

    override suspend fun createAllocation(request: HostelAllocationCreateRequest) {
        val response = apiService.createHostelAllocation(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to allocate room (error ${response.code()})")
    }

    override suspend fun vacateAllocation(allocationId: String) {
        val response = apiService.vacateHostelAllocation(allocationId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to vacate room (error ${response.code()})")
    }
}
