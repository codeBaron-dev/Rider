package com.codebaron.rider.data.local.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for handling database operations related to locations.
 */
@Dao
interface LocationDao {

	/**
	 * Inserts a new location into the database. If a conflict occurs, it replaces the existing entry.
	 *
	 * @param location The location entity to be inserted.
	 * @return The row ID of the newly inserted location.
	 */
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertLocation(location: LocationEntity): Long

	/**
	 * Updates an existing location in the database.
	 *
	 * @param location The location entity with updated values.
	 */
	@Update
	suspend fun updateLocation(location: LocationEntity)

	/**
	 * Deletes a specific location from the database.
	 *
	 * @param location The location entity to be deleted.
	 */
	@Delete
	suspend fun deleteLocation(location: LocationEntity)

	/**
	 * Deletes all locations from the database.
	 */
	@Query("DELETE FROM locations")
	suspend fun deleteAllLocations()

	/**
	 * Retrieves all locations from the database, ordered by creation time in descending order.
	 *
	 * @return A Flow that emits a list of locations.
	 */
	@Query("SELECT * FROM locations ORDER BY created_at DESC")
	fun getAllLocations(): Flow<List<LocationEntity>>

	/**
	 * Searches for locations in the database that match the given keyword.
	 *
	 * @param keyword The keyword to search for in location addresses.
	 * @return A Flow that emits a list of matching locations.
	 */
	@Query("SELECT * FROM locations WHERE address LIKE '%' || :keyword || '%'")
	fun searchLocations(keyword: String): Flow<List<LocationEntity>>

	/**
	 * Retrieves a location from the database by its unique ID.
	 *
	 * @param locationId The ID of the location to fetch.
	 * @return The matching LocationEntity if found, otherwise null.
	 */
	@Query("SELECT * FROM locations WHERE id = :locationId")
	suspend fun getLocationById(locationId: Long): LocationEntity?
}