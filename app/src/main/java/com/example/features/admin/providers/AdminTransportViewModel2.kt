package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.TransportPassCreateRequest
import com.example.core.network.TransportPassDto
import com.example.core.network.TransportRouteCreateRequest
import com.example.core.network.TransportRouteDto
import com.example.core.network.TransportVehicleCreateRequest
import com.example.core.network.TransportVehicleDto
import com.example.core.repository.AdminRepository
import com.example.core.repository.TransportRepository
import com.example.features.admin.models.AdminFeeStudent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminTransportState2(
    val isLoading: Boolean = true,
    val error: String? = null,
    val routes: List<TransportRouteDto> = emptyList(),
    val vehicles: List<TransportVehicleDto> = emptyList(),
    val passes: List<TransportPassDto> = emptyList(),
    val studentResults: List<AdminFeeStudent> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/** Named ViewModel2 to avoid colliding with the pre-existing dead AdminTransportViewModel stub. */
class AdminTransportViewModel2(
    private val repository: TransportRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminTransportState2())
    val uiState: StateFlow<AdminTransportState2> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val routesD = async { repository.getRoutes() }
                    val vehiclesD = async { repository.getVehicles() }
                    val passesD = async { repository.getPasses() }
                    _uiState.update {
                        it.copy(routes = routesD.await(), vehicles = vehiclesD.await(), passes = passesD.await(), isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load transport data") }
            }
        }
    }

    fun searchStudents(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(studentResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = adminRepository.searchStudentsForFees(query) ?: emptyList()
                _uiState.update { it.copy(studentResults = results, isSearching = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSearching = false, error = e.message) }
            }
        }
    }

    private fun mutate(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, saveSuccess = false) }
            try {
                block()
                loadAll()
                _uiState.update { it.copy(isSaving = false, saveSuccess = true, studentResults = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Operation failed") }
            }
        }
    }

    fun createRoute(name: String, code: String, startPoint: String, endPoint: String, fare: Double?) = mutate {
        repository.createRoute(TransportRouteCreateRequest(name = name, code = code, startPoint = startPoint, endPoint = endPoint, fare = fare))
    }

    fun deleteRoute(routeId: String) = mutate { repository.deleteRoute(routeId) }

    fun createVehicle(registrationNo: String, vehicleType: String, capacity: Int, driverName: String, driverPhone: String, routeId: String?) = mutate {
        repository.createVehicle(
            TransportVehicleCreateRequest(
                registrationNo = registrationNo, vehicleType = vehicleType.ifBlank { null }, capacity = capacity,
                driverName = driverName.ifBlank { null }, driverPhone = driverPhone.ifBlank { null }, routeId = routeId
            )
        )
    }

    fun deleteVehicle(vehicleId: String) = mutate { repository.deleteVehicle(vehicleId) }

    fun issuePass(routeId: String, studentId: String, validFrom: String, validTo: String, farePaid: Double?) = mutate {
        repository.createPass(
            TransportPassCreateRequest(routeId = routeId, studentId = studentId, validFrom = validFrom.ifBlank { null }, validTo = validTo.ifBlank { null }, farePaid = farePaid)
        )
    }

    fun cancelPass(passId: String) = mutate { repository.cancelPass(passId) }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminTransportViewModel2Factory(
    private val repository: TransportRepository,
    private val adminRepository: AdminRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminTransportViewModel2::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminTransportViewModel2(repository, adminRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
