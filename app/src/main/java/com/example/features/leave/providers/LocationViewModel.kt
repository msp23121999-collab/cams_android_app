package com.example.features.leave.providers

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LocationState(
    val location: Location? = null,
    val distanceToCampus: Float? = null,
    val isWithinCampus: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)
    
    private val _uiState = MutableStateFlow(LocationState())
    val uiState: StateFlow<LocationState> = _uiState.asStateFlow()

    // Campus coordinates (e.g. from the React code)
    private val CAMPUS_LAT = 11.3410
    private val CAMPUS_LON = 77.7172
    private val CAMPUS_RADIUS_METERS = 500f // 500 meters

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Using Priority.PRIORITY_HIGH_ACCURACY
                val location: Location? = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                if (location != null) {
                    val campusLocation = Location("").apply {
                        latitude = CAMPUS_LAT
                        longitude = CAMPUS_LON
                    }
                    val distance = location.distanceTo(campusLocation)
                    val isWithin = distance <= CAMPUS_RADIUS_METERS

                    _uiState.update {
                        it.copy(
                            location = location,
                            distanceToCampus = distance,
                            isWithinCampus = isWithin,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to get location") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Location error") }
            }
        }
    }
}
