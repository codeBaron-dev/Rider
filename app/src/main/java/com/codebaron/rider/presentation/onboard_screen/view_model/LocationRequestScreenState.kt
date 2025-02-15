package com.codebaron.rider.presentation.onboard_screen.view_model

import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.data.local.location.LocationDetails

data class LocationRequestScreenState(
	val isLoading: Boolean = false,
	val locationDetails: LocationDetails? = null,
	val error: String? = null,
	val showPermissionRequest: Boolean = false,
	val savedLocations: List<LocationDetails> = emptyList(),
	val drivers: List<DriverEntity> = emptyList(),
	val latitude: Double = 0.0,
	val longitude: Double = 0.0,
	val address: String = "",
	val eta: Int = 0,
	val fare: Double = 0.0,
	val hasArrived: Boolean = false
)