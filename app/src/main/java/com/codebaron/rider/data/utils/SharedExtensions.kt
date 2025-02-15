package com.codebaron.rider.data.utils

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.data.local.location.LocationDetails
import com.codebaron.rider.data.local.location.LocationEntity
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val BASE_FARE = 2.50
private const val PER_KM_RATE = 1.00

/**
 * Extension function to convert `LocationDetails` to `LocationEntity`.
 *
 * @return A `LocationEntity` instance containing the same location data.
 */
fun LocationDetails.toEntity(): LocationEntity {
	return LocationEntity(
		id = id,
		latitude = latitude,
		longitude = longitude,
		address = address
	)
}

/**
 * Extension function to convert `LocationEntity` to `LocationDetails`.
 *
 * @return A `LocationDetails` instance containing the same location data.
 */
fun LocationEntity.toLocationDetails(): LocationDetails {
	return LocationDetails(
		id = id,
		latitude = latitude,
		longitude = longitude,
		address = address
	)
}

/**
 * Extension function to convert a list of `LocationEntity` objects to a list of `LocationDetails`.
 *
 * @return A list of `LocationDetails` instances.
 */
fun List<LocationEntity>.toLocationDetailsList(): List<LocationDetails> {
	return map { it.toLocationDetails() }
}

/**
 * Calculates the Estimated Time of Arrival (ETA) based on distance and speed.
 *
 * @param distance The distance in meters.
 * @param averageSpeed The average speed in km/h (default: 50 km/h).
 * @return ETA in minutes.
 */
fun calculateETA(distance: Float, averageSpeed: Float = 50f): Int { // 50 km/h
	val speedMps = averageSpeed * 1000 / 3600 // Convert to meters per second
	return (distance / speedMps / 60).toInt()
}

/**
 * Calculates the bearing (direction) from one coordinate to another.
 *
 * @param startLat Starting latitude.
 * @param startLng Starting longitude.
 * @param endLat Destination latitude.
 * @param endLng Destination longitude.
 * @return The bearing in degrees.
 */
private fun getBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
	val startLatRad = Math.toRadians(startLat)
	val startLngRad = Math.toRadians(startLng)
	val endLatRad = Math.toRadians(endLat)
	val endLngRad = Math.toRadians(endLng)

	val deltaLng = endLngRad - startLngRad

	val y = sin(deltaLng) * cos(endLatRad)
	val x = cos(startLatRad) * sin(endLatRad) -
			sin(startLatRad) * cos(endLatRad) * cos(deltaLng)

	var bearingRad = atan2(y, x)
	var bearingDeg = Math.toDegrees(bearingRad)

	// Normalize to 0-360
	bearingDeg = (bearingDeg + 360) % 360

	return bearingDeg.toFloat()
}

/**
 * Calculates a new geographic position by moving a certain distance in a given direction.
 *
 * @param lat Current latitude.
 * @param lng Current longitude.
 * @param distance Distance to move in meters.
 * @param bearing Direction to move in degrees.
 * @return A new `LatLng` representing the updated position.
 */
private fun calculateNewPosition(
	lat: Double,
	lng: Double,
	distance: Double,
	bearing: Float
): LatLng {
	val earthRadius = 6371e3 // Earth radius in meters
	val angularDistance = distance / earthRadius
	val bearingRad = Math.toRadians(bearing.toDouble())

	val latRad = Math.toRadians(lat)
	val lngRad = Math.toRadians(lng)

	val newLatRad = asin(
		sin(latRad) * cos(angularDistance) +
				cos(latRad) * sin(angularDistance) * cos(bearingRad)
	)

	val newLngRad = lngRad + atan2(
		sin(bearingRad) * sin(angularDistance) * cos(latRad),
		cos(angularDistance) - sin(latRad) * sin(newLatRad)
	)

	return LatLng(
		Math.toDegrees(newLatRad),
		Math.toDegrees(newLngRad)
	)
}

/**
 * Simulates a driver's movement towards a user's location.
 *
 * @param driver The driver entity containing the starting position.
 * @param userLocation The destination coordinates.
 * @param locationRequestScreenViewModel The ViewModel responsible for managing the movement updates.
 * @param price A callback function to receive the fare estimate.
 */
fun simulateDriverMovement(
	driver: DriverEntity,
	userLocation: LatLng,
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	price: (Double) -> Unit,
	onArrival: (Boolean, DriverEntity) -> Unit,
) {
	locationRequestScreenViewModel.viewModelScope.launch {
		var currentLat = driver.latitude
		var currentLng = driver.longitude
		val stepSize = 0.0001 // Adjust for simulation speed

		// Calculate initial fare
		val initialDistance = calculateDistance(
			driver.latitude,
			driver.longitude,
			userLocation.latitude,
			userLocation.longitude
		).toDouble()

		val surgeMultiplier = getSurgeMultiplier()
		val fare = calculateFare(initialDistance, surgeMultiplier)
		price(fare)
		println("Fare: $fare")

		while (true) {
			val bearing = getBearing(
				currentLat,
				currentLng,
				userLocation.latitude,
				userLocation.longitude
			)

			// Move driver 50 meters towards current bearing
			val newPosition = calculateNewPosition(
				currentLat,
				currentLng,
				50.0, // meters
				bearing
			)

			currentLat = newPosition.latitude
			currentLng = newPosition.longitude

			// Update driver in database
			locationRequestScreenViewModel.insertUpdatedDriverLatLongLocally(
				driver.copy(
					latitude = currentLat,
					longitude = currentLng
				)
			)

			// Check arrival
			val distance = calculateDistance(
				currentLat,
				currentLng,
				userLocation.latitude,
				userLocation.longitude
			)

			if (distance < 10) { // 10 meters threshold
				onArrival(true, driver)
				break
			}

			delay(1000) // Update every second
		}
	}
}

/**
 * Calculates the fare based on distance and surge pricing.
 *
 * @param distanceMeters Distance traveled in meters.
 * @param surgeMultiplier Surge pricing multiplier.
 * @return The calculated fare amount.
 */
fun calculateFare(distanceMeters: Double, surgeMultiplier: Double): Double {
	val distanceKm = distanceMeters / 1000.0
	return BASE_FARE + (distanceKm * PER_KM_RATE * surgeMultiplier)
}

/**
 * Determines the current surge pricing multiplier based on peak hours.
 *
 * @return The surge multiplier (1.5x during peak hours, otherwise 1.0x).
 */
fun getSurgeMultiplier(timeProvider: TimeProvider = RealTimeProvider()): Double {
	val currentHour = timeProvider.getCurrentHour()
	return if ((currentHour in 7..9) || (currentHour in 17..19)) 1.5 else 1.0
}

/**
 * Calculates the distance between two geographic points.
 *
 * @param lat1 Latitude of the first location.
 * @param lon1 Longitude of the first location.
 * @param lat2 Latitude of the second location.
 * @param lon2 Longitude of the second location.
 * @return Distance in meters.
 */
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
	val results = FloatArray(1)
	Location.distanceBetween(lat1, lon1, lat2, lon2, results)
	return results[0]
}