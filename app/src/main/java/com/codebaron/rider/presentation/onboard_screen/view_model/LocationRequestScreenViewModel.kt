package com.codebaron.rider.presentation.onboard_screen.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.data.local.drivers.sampleDrivers
import com.codebaron.rider.data.local.location.LocationDetails
import com.codebaron.rider.data.utils.failed_to_delete_location
import com.codebaron.rider.data.utils.failed_to_delete_locations
import com.codebaron.rider.data.utils.failed_to_save_location
import com.codebaron.rider.data.utils.search_failed
import com.codebaron.rider.data.utils.unable_to_get_location_text
import com.codebaron.rider.data.utils.unknown_error_occurred_text
import com.codebaron.rider.domain.location.LocationManager
import com.codebaron.rider.domain.rider_navigation.NavigationRoutes
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationRequestScreenViewModel(
	private val locationRepository: LocationRequestScreenRepository
) : ViewModel() {


	private var _placesClient: PlacesClient? = null // Google Places API client
	private val _searchResults = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
	val searchResults = _searchResults.asStateFlow()
	private val _selectedLocation = MutableStateFlow<Place?>(null)
	val selectedLocation = _selectedLocation.asStateFlow()
	private val _state = MutableSharedFlow<LocationRequestScreenState>(replay = 1)
	val state = _state.asSharedFlow()
	private val _navigationEvent = MutableSharedFlow<NavigationRoutes>()
	val navigationEvent = _navigationEvent.asSharedFlow()
	private val intents = MutableSharedFlow<LocationRequestScreenIntent>()
	private lateinit var locationManager: LocationManager

	/**
	 * Flow that holds the list of saved locations fetched from the repository.
	 */
	val savedLocations: StateFlow<List<LocationDetails>> = locationRepository
		.getAllLocations()
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = emptyList()
		)

	/**
	 * Initializes required dependencies like Places API client and LocationManager.
	 */
	fun initialize(context: Context) {
		_placesClient = Places.createClient(context)
		locationManager = LocationManager(context)
	}

	init {
		handleIntents()
		insertDriversLocally()
	}

	/**
	 * Sends user interactions (intents) to be processed.
	 */
	fun sendIntent(intent: LocationRequestScreenIntent) {
		viewModelScope.launch {
			intents.emit(intent)
		}
	}

	/**
	 * Handles user intents and triggers corresponding actions.
	 */
	private fun handleIntents() = viewModelScope.launch {
		intents.collect { intent ->
			when (intent) {
				LocationRequestScreenIntent.LocationPermissionActionClick -> locationRequest()
				is LocationRequestScreenIntent.LocationReceived -> handleLocationReceived(intent.location)
				is LocationRequestScreenIntent.LocationError -> handleLocationError(intent.error)
				is LocationRequestScreenIntent.GetAllSavedLocation -> getAllLocations()
				is LocationRequestScreenIntent.SendEta -> updateState { it.copy(eta = intent.eta) }
				is LocationRequestScreenIntent.SendFare -> updateState { it.copy(fare = intent.fare) }
				is LocationRequestScreenIntent.SendArrival -> updateState { it.copy(hasArrived = intent.hasArrived) }
			}
		}
	}

	/**
	 * Inserts sample driver data into the local database.
	 */
	private fun insertDriversLocally() = viewModelScope.launch {
		locationRepository.insertDrivers(sampleDrivers)
		val drivers = locationRepository.getAllDrivers()
		updateState { it.copy(drivers = drivers) }
	}

	/**
	 * Updates a driver's location in the local database.
	 */
	fun insertUpdatedDriverLatLongLocally(driver: DriverEntity) = viewModelScope.launch {
		locationRepository.updateDriver(driver.carPlateNumber, driver)
		val drivers = locationRepository.getAllDrivers()
		updateState { it.copy(drivers = drivers) }
	}

	/**
	 * Requests the user's current location, handling permissions and errors.
	 */
	private fun locationRequest() = viewModelScope.launch {
		updateState { it.copy(isLoading = true, showPermissionRequest = true) }
		try {
			if (locationManager.hasLocationPermission()) {
				locationManager.getCurrentLocation()?.let { location ->
					sendIntent(LocationRequestScreenIntent.LocationReceived(location))
				}
					?: sendIntent(LocationRequestScreenIntent.LocationError(error = unable_to_get_location_text))
			}
		} catch (e: Exception) {
			sendIntent(
				LocationRequestScreenIntent.LocationError(
					error = e.message ?: unknown_error_occurred_text
				)
			)
		}
	}

	/**
	 * Handles a received location and saves it.
	 */
	private fun handleLocationReceived(location: LocationDetails) = viewModelScope.launch {
		try {
			updateState {
				it.copy(
					isLoading = false,
					locationDetails = location,
					error = null,
					showPermissionRequest = false
				)
			}
			val locationId = locationRepository.saveLocation(location)
			//insertDrivers()
			_navigationEvent.emit(NavigationRoutes.HomeScreen)
		} catch (exception: Exception) {
			sendIntent(
				LocationRequestScreenIntent.LocationError(
					error = exception.message ?: failed_to_save_location
				)
			)
		}
	}

	/**
	 * Handles errors related to location retrieval.
	 */
	private fun handleLocationError(error: String) = viewModelScope.launch {
		updateState {
			it.copy(
				isLoading = false,
				error = error,
				showPermissionRequest = true
			)
		}
	}

	/**
	 * Updates the UI state with the given transformation.
	 */
	private suspend fun updateState(transform: (LocationRequestScreenState) -> LocationRequestScreenState) {
		val currentState = _state.replayCache.firstOrNull() ?: LocationRequestScreenState()
		val newState = transform(currentState)
		_state.emit(newState)
	}

	/**
	 * Fetches all saved locations from the repository.
	 */
	private fun getAllLocations() = viewModelScope.launch {
		try {
			updateState { it.copy(isLoading = true) }
			locationRepository.getAllLocations()
				.collect { locations ->
					updateState {
						it.copy(
							isLoading = false,
							savedLocations = locations,
							error = null,
							latitude = locations[0].latitude,
							longitude = locations[0].longitude,
							address = locations[0].address
						)
					}
				}
		} catch (e: Exception) {
			updateState {
				it.copy(
					isLoading = false,
					error = e.message ?: "Failed to fetch locations"
				)
			}
		}
	}

	/**
	 * Searches locations stored in the local database.
	 */
	fun searchLocationsLocally(keyword: String) = viewModelScope.launch {
		updateState { it.copy(isLoading = true) }
		try {
			locationRepository.searchLocations(keyword)
				.collect { locations ->
					updateState {
						it.copy(
							isLoading = false,
							savedLocations = locations
						)
					}
				}
		} catch (e: Exception) {
			handleLocationError(e.message ?: search_failed)
		}
	}

	/**
	 * Deletes a specific saved location from the local database.
	 */
	fun deleteLocationLocally(locationId: Long) = viewModelScope.launch {
		try {
			locationRepository.deleteLocation(locationId)
		} catch (e: Exception) {
			handleLocationError(e.message ?: failed_to_delete_location)
		}
	}

	fun deleteAllLocationsLocally() = viewModelScope.launch {
		try {
			locationRepository.deleteAllLocations()
		} catch (e: Exception) {
			handleLocationError(e.message ?: failed_to_delete_locations)
		}
	}

	fun searchLocations(query: String) {
		viewModelScope.launch {
			if (query.isNotEmpty()) {
				val request = FindAutocompletePredictionsRequest.builder()
					.setQuery(query)
					.build()

				try {
					val response = _placesClient?.findAutocompletePredictions(request)?.await()
					_searchResults.value = response?.autocompletePredictions!!
				} catch (e: Exception) {
					_searchResults.value = emptyList()
				}
			} else {
				_searchResults.value = emptyList()
			}
		}
	}

	fun selectPlace(placeId: String) {
		viewModelScope.launch {
			val placeFields = listOf(
				Place.Field.ID,
				Place.Field.NAME,
				Place.Field.LAT_LNG,
				Place.Field.ADDRESS
			)
			val request = FetchPlaceRequest.builder(placeId, placeFields).build()
			try {
				val response = _placesClient?.fetchPlace(request)?.await()
				_selectedLocation.value = response?.place
				_searchResults.value = emptyList()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}