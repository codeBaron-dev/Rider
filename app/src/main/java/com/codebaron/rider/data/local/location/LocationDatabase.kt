package com.codebaron.rider.data.local.location

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codebaron.rider.data.local.drivers.DriverDao
import com.codebaron.rider.data.local.drivers.DriverEntity

/**
 * Room database class for managing local storage of location and driver data.
 *
 * This database contains two entities:
 * - `LocationEntity` for storing location-related data.
 * - `DriverEntity` for storing driver details.
 *
 * @property locationDao Provides access to location-related database operations.
 * @property driverDao Provides access to driver-related database operations.
 */
@Database(
	entities = [LocationEntity::class, DriverEntity::class],
	version = 1,
	exportSchema = true
)
abstract class LocationDatabase : RoomDatabase() {

	/**
	 * Provides access to LocationDao for performing CRUD operations on location data.
	 *
	 * @return An instance of `LocationDao`.
	 */
	abstract fun locationDao(): LocationDao

	/**
	 * Provides access to DriverDao for performing CRUD operations on driver data.
	 *
	 * @return An instance of `DriverDao`.
	 */
	abstract fun driverDao(): DriverDao
}