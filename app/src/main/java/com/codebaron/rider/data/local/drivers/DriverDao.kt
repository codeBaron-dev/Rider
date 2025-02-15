package com.codebaron.rider.data.local.drivers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for handling database operations related to drivers.
 */
@Dao
interface DriverDao {

	/**
	 * Inserts a list of drivers into the database.
	 * If a conflict occurs (e.g., duplicate entry), it replaces the existing data.
	 *
	 * @param drivers List of [DriverEntity] to be inserted.
	 */
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertDrivers(drivers: List<DriverEntity>)

	/**
	 * Retrieves all drivers stored in the database.
	 *
	 * @return A list of [DriverEntity] representing all drivers.
	 */
	@Query("SELECT * FROM drivers")
	suspend fun getAllDrivers(): List<DriverEntity>

	/**
	 * Updates an existing driver’s details using their car plate number.
	 *
	 * @param carPlateNumber The unique identifier for the driver’s vehicle.
	 * @param name The updated name of the driver.
	 * @param image The updated profile image URL or path of the driver.
	 * @param longitude The updated longitude position of the driver.
	 * @param latitude The updated latitude position of the driver.
	 * @param carName The updated name of the car.
	 */
	@Query("UPDATE drivers SET name = :name, image = :image, longitude = :longitude, latitude = :latitude, carName = :carName WHERE carPlateNumber = :carPlateNumber")
	suspend fun updateDriverByCarPlateNumber(
		carPlateNumber: String,
		name: String,
		image: String,
		longitude: Double,
		latitude: Double,
		carName: String
	)
}
