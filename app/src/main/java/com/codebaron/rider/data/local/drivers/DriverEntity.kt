package com.codebaron.rider.data.local.drivers

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a driver entity for storing driver details in the Room database.
 *
 * @property id Auto-generated unique identifier for the driver.
 * @property name Name of the driver.
 * @property image URL or local path to the driver's image.
 * @property carPlateNumber Unique car plate number associated with the driver.
 * @property carName Name of the car the driver is using.
 * @property latitude Current latitude of the driver's location.
 * @property longitude Current longitude of the driver's location.
 */
@Entity(tableName = "drivers")
data class DriverEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val name: String,
	val image: String,
	val carPlateNumber: String,
	val carName: String,
	val latitude: Double,
	val longitude: Double
)

/**
 * Sample list of drivers used for testing or initial data population.
 */
val sampleDrivers = listOf(
	DriverEntity(
		name = "John Doe",
		image = "https://example.com/driver1.png",
		carPlateNumber = "ABC123",
		carName = "Toyota Corolla",
		latitude = 8.229220,
		longitude = 4.614050
	),
	DriverEntity(
		name = "Jane Smith",
		image = "https://example.com/driver2.png",
		carPlateNumber = "XYZ789",
		carName = "Honda Civic",
		latitude = 6.603710,
		longitude = 3.288890
	)
)