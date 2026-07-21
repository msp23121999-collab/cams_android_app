package com.example.core.repository

import com.example.core.network.CamsApiService
import com.example.core.network.TransportPassCreateRequest
import com.example.core.network.TransportPassDto
import com.example.core.network.TransportRouteCreateRequest
import com.example.core.network.TransportRouteDto
import com.example.core.network.TransportVehicleCreateRequest
import com.example.core.network.TransportVehicleDto
import java.io.IOException

interface TransportRepository {
    suspend fun getRoutes(): List<TransportRouteDto>
    suspend fun createRoute(request: TransportRouteCreateRequest)
    suspend fun updateRoute(routeId: String, payload: Map<String, Any?>)
    suspend fun deleteRoute(routeId: String)
    suspend fun getVehicles(): List<TransportVehicleDto>
    suspend fun createVehicle(request: TransportVehicleCreateRequest)
    suspend fun updateVehicle(vehicleId: String, payload: Map<String, Any?>)
    suspend fun deleteVehicle(vehicleId: String)
    suspend fun getPasses(status: String? = null): List<TransportPassDto>
    suspend fun createPass(request: TransportPassCreateRequest)
    suspend fun cancelPass(passId: String)
}

class TransportRepositoryImpl(private val apiService: CamsApiService) : TransportRepository {

    private fun errorDetail(response: retrofit2.Response<*>): String? = try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) null
        else org.json.JSONObject(body).optString("detail", "").ifBlank { null }
    } catch (e: Exception) { null }

    override suspend fun getRoutes(): List<TransportRouteDto> {
        val response = apiService.getTransportRoutes()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load routes: ${response.code()}")
    }

    override suspend fun createRoute(request: TransportRouteCreateRequest) {
        val response = apiService.createTransportRoute(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create route (error ${response.code()})")
    }

    override suspend fun updateRoute(routeId: String, payload: Map<String, Any?>) {
        val response = apiService.updateTransportRoute(routeId, payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update route (error ${response.code()})")
    }

    override suspend fun deleteRoute(routeId: String) {
        val response = apiService.deleteTransportRoute(routeId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete route (error ${response.code()})")
    }

    override suspend fun getVehicles(): List<TransportVehicleDto> {
        val response = apiService.getTransportVehicles()
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load vehicles: ${response.code()}")
    }

    override suspend fun createVehicle(request: TransportVehicleCreateRequest) {
        val response = apiService.createTransportVehicle(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create vehicle (error ${response.code()})")
    }

    override suspend fun updateVehicle(vehicleId: String, payload: Map<String, Any?>) {
        val response = apiService.updateTransportVehicle(vehicleId, payload)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to update vehicle (error ${response.code()})")
    }

    override suspend fun deleteVehicle(vehicleId: String) {
        val response = apiService.deleteTransportVehicle(vehicleId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to delete vehicle (error ${response.code()})")
    }

    override suspend fun getPasses(status: String?): List<TransportPassDto> {
        val response = apiService.getTransportPasses(status)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw IOException("Failed to load passes: ${response.code()}")
    }

    override suspend fun createPass(request: TransportPassCreateRequest) {
        val response = apiService.createTransportPass(request)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to create pass (error ${response.code()})")
    }

    override suspend fun cancelPass(passId: String) {
        val response = apiService.cancelTransportPass(passId)
        if (!response.isSuccessful) throw IOException(errorDetail(response) ?: "Failed to cancel pass (error ${response.code()})")
    }
}
