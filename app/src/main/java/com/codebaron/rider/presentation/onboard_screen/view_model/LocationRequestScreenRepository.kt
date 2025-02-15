package com.codebaron.rider.presentation.onboard_screen.view_model

import com.codebaron.rider.data.local.drivers.DriverDao
import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.data.local.location.LocationDao
import com.codebaron.rider.data.local.location.LocationDetails
import com.codebaron.rider.data.utils.toEntity
import com.codebaron.rider.data.utils.toLocationDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRequestScreenRepository(
	private val locationDao: LocationDao,
	private val driverDao: DriverDao
) {
	suspend fun saveLocation(locationDetails: LocationDetails): Long {
		return locationDao.insertLocation(locationDetails.toEntity())
	}

	suspend fun updateLocation(id: Long, locationDetails: LocationDetails) {
		locationDao.updateLocation(locationDetails.toEntity().copy(id = id))
	}

	suspend fun deleteLocation(id: Long) {
		locationDao.getLocationById(id)?.let { locationDao.deleteLocation(it) }
	}

	suspend fun deleteAllLocations() {
		locationDao.deleteAllLocations()
	}

	fun getAllLocations(): Flow<List<LocationDetails>> {
		return locationDao.getAllLocations().map { entities ->
			entities.map { it.toLocationDetails() }
		}
	}

	fun searchLocations(keyword: String): Flow<List<LocationDetails>> {
		return locationDao.searchLocations(keyword).map { entities ->
			entities.map { it.toLocationDetails() }
		}
	}
	suspend fun getAllDrivers(): List<DriverEntity> {
		return driverDao.getAllDrivers()
	}

	suspend fun insertDrivers(drivers: List<DriverEntity>) {
		driverDao.insertDrivers(drivers)
	}

	suspend fun updateDriver(carPlateNumber: String, updatedDriver: DriverEntity) {
		driverDao.updateDriverByCarPlateNumber(
			carPlateNumber = carPlateNumber,
			name = updatedDriver.name,
			image = updatedDriver.image,
			longitude = updatedDriver.longitude,
			latitude = updatedDriver.latitude,
			carName = updatedDriver.carName
		)
	}

}