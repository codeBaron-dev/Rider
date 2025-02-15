package com.codebaron.rider.presentation.onboard_screen.view_model

import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.data.local.location.LocationDetails

sealed class LocationRequestScreenIntent {
	data object LocationPermissionActionClick: LocationRequestScreenIntent()
	data class LocationReceived(val location: LocationDetails) : LocationRequestScreenIntent()
	data class LocationError(val error: String) : LocationRequestScreenIntent()
	data object GetAllSavedLocation : LocationRequestScreenIntent()
	data class SendEta(val eta: Int): LocationRequestScreenIntent()
	data class SendFare(val fare: Double): LocationRequestScreenIntent()
	data class SendArrival(val hasArrived: Boolean, val driver: DriverEntity): LocationRequestScreenIntent()

}