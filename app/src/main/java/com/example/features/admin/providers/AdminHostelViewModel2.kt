package com.example.features.admin.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.network.HostelAllocationCreateRequest
import com.example.core.network.HostelAllocationDto
import com.example.core.network.HostelBlockCreateRequest
import com.example.core.network.HostelBlockDto
import com.example.core.network.HostelRoomCreateRequest
import com.example.core.network.HostelRoomDto
import com.example.core.repository.AdminRepository
import com.example.core.repository.HostelRepository
import com.example.features.admin.models.AdminFeeStudent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminHostelState2(
    val isLoading: Boolean = true,
    val error: String? = null,
    val blocks: List<HostelBlockDto> = emptyList(),
    val rooms: List<HostelRoomDto> = emptyList(),
    val allocations: List<HostelAllocationDto> = emptyList(),
    val studentResults: List<AdminFeeStudent> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/** Named ViewModel2 to avoid colliding with the pre-existing dead AdminHostelViewModel stub. */
class AdminHostelViewModel2(
    private val repository: HostelRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminHostelState2())
    val uiState: StateFlow<AdminHostelState2> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                coroutineScope {
                    val blocksD = async { repository.getBlocks() }
                    val roomsD = async { repository.getRooms() }
                    val allocD = async { repository.getAllocations() }
                    _uiState.update {
                        it.copy(blocks = blocksD.await(), rooms = roomsD.await(), allocations = allocD.await(), isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load hostel data") }
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

    fun createBlock(name: String, code: String, hostelType: String, wardenName: String, wardenPhone: String, address: String) = mutate {
        repository.createBlock(
            HostelBlockCreateRequest(
                name = name, code = code, hostelType = hostelType,
                wardenName = wardenName.ifBlank { null }, wardenPhone = wardenPhone.ifBlank { null }, address = address.ifBlank { null }
            )
        )
    }

    fun deleteBlock(blockId: String) = mutate { repository.deleteBlock(blockId) }

    fun createRoom(blockId: String, roomNumber: String, floor: Int, capacity: Int, roomType: String, monthlyRent: Double?) = mutate {
        repository.createRoom(
            HostelRoomCreateRequest(
                blockId = blockId, roomNumber = roomNumber, floor = floor, capacity = capacity,
                roomType = roomType.ifBlank { null }, monthlyRent = monthlyRent
            )
        )
    }

    fun deleteRoom(roomId: String) = mutate { repository.deleteRoom(roomId) }

    fun allocateRoom(roomId: String, studentId: String, remarks: String) = mutate {
        repository.createAllocation(HostelAllocationCreateRequest(roomId = roomId, studentId = studentId, remarks = remarks.ifBlank { null }))
    }

    fun vacateAllocation(allocationId: String) = mutate { repository.vacateAllocation(allocationId) }

    fun clearSaveStatus() {
        _uiState.update { it.copy(saveError = null, saveSuccess = false) }
    }
}

class AdminHostelViewModel2Factory(
    private val repository: HostelRepository,
    private val adminRepository: AdminRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminHostelViewModel2::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminHostelViewModel2(repository, adminRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
