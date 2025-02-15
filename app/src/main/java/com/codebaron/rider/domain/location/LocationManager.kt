package com.codebaron.rider.domain.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.codebaron.rider.data.local.location.LocationDetails
import com.codebaron.rider.data.utils.address_not_found_text
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationManager (private val context: Context) {
	private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
	private val geocoder = Geocoder(context, Locale.getDefault())

	@SuppressLint("MissingPermission")
	suspend fun getCurrentLocation(): LocationDetails? {
		return withContext(Dispatchers.IO) {
			try {
				// Using Tasks.await() instead of kotlinx extension
				val location = Tasks.await(fusedLocationClient.lastLocation)
				location?.let {
					val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
					val address = addresses?.firstOrNull()?.let { addressDetails ->
						buildString {
							append(addressDetails.getAddressLine(0))
							if (addressDetails.locality != null) {
								append(", ${addressDetails.locality}")
							}
							if (addressDetails.adminArea != null) {
								append(", ${addressDetails.adminArea}")
							}
						}
					} ?: address_not_found_text

					LocationDetails(
						latitude = it.latitude,
						longitude = it.longitude,
						address = address
					)
				}
			} catch (e: Exception) {
				null
			}
		}
	}

	fun hasLocationPermission(): Boolean {
		return ContextCompat.checkSelfPermission(
			context,
			Manifest.permission.ACCESS_FINE_LOCATION
		) == PackageManager.PERMISSION_GRANTED
	}
}