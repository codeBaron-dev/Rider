package com.codebaron.rider.presentation.onboard_screen.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.codebaron.rider.R
import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.data.local.location.LocationDetails
import com.codebaron.rider.data.utils.app_needs_your_location
import com.codebaron.rider.data.utils.calculateDistance
import com.codebaron.rider.data.utils.calculateETA
import com.codebaron.rider.data.utils.calculateFare
import com.codebaron.rider.data.utils.getSurgeMultiplier
import com.codebaron.rider.data.utils.grant_permission
import com.codebaron.rider.data.utils.location_required_text
import com.codebaron.rider.data.utils.simulateDriverMovement
import com.codebaron.rider.domain.location.LocationManager
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenIntent
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenState
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VehicleSelectionUI() {
	val options = listOf("Ride", "Freight", "Couriers")
	val selectedOption = remember { mutableStateOf("Ride") } // Default selection

	val icons = mapOf(
		"Ride" to R.drawable.order_ride,
		"Freight" to R.drawable.truck,
		"Couriers" to R.drawable.haulage
	)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 5.dp),
		horizontalArrangement = Arrangement.SpaceAround
	) {
		options.forEach { option ->
			val isSelected = selectedOption.value == option
			Column(
				modifier = Modifier
					.clip(RoundedCornerShape(5.dp))
					.background(if (isSelected) Color(0xFF1E3A5F) else Color.Transparent)
					.clickable { selectedOption.value = option },
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Image(
					painter = painterResource(id = icons[option]!!),
					contentDescription = option,
					modifier = Modifier.size(50.dp)
				)
				Text(
					text = option,
					fontSize = 14.sp,
					color = if (isSelected) Color.White else Color.Gray
				)
			}
		}
	}
}

@Composable
fun FindDriverScreen(
	locationRequestState: LocationRequestScreenState,
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
) {

	val context = LocalContext.current
	var text by remember { mutableStateOf("") }
	val searchResults by locationRequestScreenViewModel.searchResults.collectAsState()
	val selectedLocation by locationRequestScreenViewModel.selectedLocation.collectAsState()
	var expanded by remember { mutableStateOf(false) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
	) {
		// Current Location Row
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Icon(
				imageVector = Icons.Filled.CheckCircle,
				contentDescription = "Current Location",
				tint = Color(0xFF2F3B46),
				modifier = Modifier.size(22.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = locationRequestState.address.take(15),
				fontSize = 16.sp,
				fontWeight = FontWeight.Bold
			)
			Spacer(modifier = Modifier.weight(1f))
			Button(
				onClick = { /* Handle Entrance click */ },
				colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F3B46)),
				shape = RoundedCornerShape(20.dp),
				contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
				modifier = Modifier.height(32.dp)
			) {
				Text("Edit current location", color = Color(0xFFB5CFFF), fontSize = 14.sp)
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Destination Input
		OutlinedTextField(
			value = selectedLocation?.address ?: text,
			onValueChange = {
				text = it
				expanded = true
			},
			keyboardOptions = KeyboardOptions(
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = {
					locationRequestScreenViewModel.searchLocations(text)
					expanded = true
				}
			),
			leadingIcon = {
				Icon(
					imageVector = Icons.Default.Search,
					contentDescription = "Search",
					tint = Color.Gray
				)
			},
			placeholder = { Text("To", color = Color.Gray) },
			colors = TextFieldDefaults.colors(
				unfocusedContainerColor = Color(0xFF2F3B46),
				focusedContainerColor = Color(0xFF2F3B46),
				unfocusedTextColor = Color.White,
				focusedTextColor = Color.White
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(50.dp)
				.clip(RoundedCornerShape(10.dp))
		)

		if (expanded) {
			Spacer(modifier = Modifier.height(18.dp))
			DropdownMenu(
				expanded = expanded && searchResults.isNotEmpty(),
				onDismissRequest = { expanded = false },
				modifier = Modifier
					.fillMaxWidth()
					.background(Color(0xFF2F3B46)),
				content = {
					searchResults.forEach { prediction ->
						DropdownMenuItem(
							onClick = {
								text = prediction.getFullText(null).toString()
								locationRequestScreenViewModel.selectPlace(prediction.placeId)
								expanded = false
							},
							text = {
								Text(
									text = prediction.getFullText(null).toString(),
									color = Color.White,
									modifier = Modifier.fillMaxWidth()
								)
							}
						)
					}
				}
			)
		}
		Spacer(modifier = Modifier.height(8.dp))

		// Fare Input
		OutlinedTextField(
			value = "",
			onValueChange = {},
			leadingIcon = {
				Text("NGN", color = Color.White, fontWeight = FontWeight.Bold)
			},
			trailingIcon = {
				Icon(
					imageVector = Icons.Default.Edit,
					contentDescription = "Edit",
					tint = Color.LightGray
				)
			},
			placeholder = { Text("Offer your fare", color = Color.Gray) },
			keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
			colors = TextFieldDefaults.colors(
				unfocusedContainerColor = Color(0xFF2F3B46),
				focusedContainerColor = Color(0xFF2F3B46),
				unfocusedTextColor = Color.White,
				focusedTextColor = Color.White
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(50.dp)
				.clip(RoundedCornerShape(10.dp))
		)

		Spacer(modifier = Modifier.height(16.dp))

		// Find Driver Button
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = Icons.Default.Edit,
				contentDescription = "Money Icon",
				modifier = Modifier
					.size(30.dp)
					.padding(end = 8.dp)
			)

			Button(
				onClick = {  /*Find a driver action*/ },
				shape = RoundedCornerShape(12.dp),
				modifier = Modifier
					.weight(1f)
					.height(55.dp)
			) {
				Text(
					"Find a driver",
					color = Color.Black,
					fontSize = 18.sp,
					fontWeight = FontWeight.Bold
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Icon(
				imageVector = Icons.Default.Settings, // Replace with actual drawable
				contentDescription = "Settings Icon",
				modifier = Modifier.size(30.dp)
			)
		}
	}
}

@Composable
fun LocationPermissionHandler(
	onLocationReceived: (LocationDetails) -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	var showPermissionDialog by remember { mutableStateOf(false) }
	val context = LocalContext.current
	val locationManager = remember { LocationManager(context) }
	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission()
	) { isGranted ->
		if (isGranted) {
			// Launch coroutine to get location
			coroutineScope.launch {
				locationManager.getCurrentLocation()?.let { location ->
					onLocationReceived(location)
				}
			}
		} else {
			Toast.makeText(context, location_required_text, Toast.LENGTH_LONG).show()
		}
	}

	LaunchedEffect(Unit) {
		if (!locationManager.hasLocationPermission()) {
			showPermissionDialog = true
		} else {
			locationManager.getCurrentLocation()?.let { location ->
				onLocationReceived(location)
			}
		}
	}

	if (showPermissionDialog) {
		AlertDialog(
			onDismissRequest = { },
			title = { Text(text = location_required_text) },
			text = { Text(text = app_needs_your_location) },
			confirmButton = {
				TextButton(onClick = {
					showPermissionDialog = false
					permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
				}) {
					Text(text = grant_permission)
				}
			},
			dismissButton = {
				/*TextButton(onClick = { showPermissionDialog = true }) {
					Text("Cancel")
				}*/
			}
		)
	}
}

@Composable
fun LocationMarkerWithRipple(
	currentLocation: LatLng,
	locationRequestState: LocationRequestScreenState,
	onLocationAction: (LocationRequestScreenIntent) -> Unit,
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	onArrival: (Boolean, DriverEntity) -> Unit
) {
	var selectedDriver by remember { mutableStateOf<DriverEntity?>(null) }
	val coroutineScope = rememberCoroutineScope()
	var showAlertDialog by remember { mutableStateOf(false) }
	val infiniteTransition = rememberInfiniteTransition(label = "ripple")
	val rippleAlpha by infiniteTransition.animateFloat(
		initialValue = 1f, // Start fully visible
		targetValue = 0f,  // Fade out completely
		animationSpec = infiniteRepeatable(
			animation = tween(2000, easing = LinearEasing), // Duration of 2000ms with a linear fade
			repeatMode = RepeatMode.Restart // Restarts after each cycle
		),
		label = "ripple_alpha"
	)

	val rippleSize by infiniteTransition.animateFloat(
		initialValue = 10f, // Start with a small ripple
		targetValue = 80f, // Expand to a larger ripple
		animationSpec = infiniteRepeatable(
			animation = tween(2000, easing = LinearEasing), // Expands over 2000ms
			repeatMode = RepeatMode.Restart // Restarts after each cycle
		),
		label = "ripple_size"
	)

	Circle(
		center = currentLocation, // The location where the ripple effect happens
		radius = rippleSize.toDouble(), // Expanding ripple effect
		fillColor = Color.Blue.copy(alpha = rippleAlpha), // Dynamic alpha for fade-out effect
		strokeColor = Color.Blue.copy(alpha = rippleAlpha), // Stroke fades out with fill
		strokeWidth = 2f // Thin stroke around the ripple
	)

	Circle(
		center = currentLocation,
		radius = 30.0, // Fixed size circle representing the current location
		fillColor = Color.Blue.copy(alpha = 0.1f), // Light blue background for visibility
		strokeColor = Color.White, // White border around the circle
		strokeWidth = 2f // Thin stroke for definition
	)

	// User location marker
	Marker(
		state = MarkerState(position = currentLocation),
		title = locationRequestState.address.take(10)
	)

	//Drivers marker
	locationRequestState.drivers.forEach { driver ->
		val driverPosition = LatLng(driver.latitude, driver.longitude)
		Marker(
			icon = BitmapDescriptorFactory.fromResource(R.drawable.car_icon),
			state = MarkerState(position = driverPosition),
			title = driver.name,
			onClick = {
				coroutineScope.launch {
					val distance = calculateDistance(
						currentLocation.latitude,
						currentLocation.longitude,
						driver.latitude,
						driver.longitude
					)
					val eta = calculateETA(distance)
					onLocationAction(LocationRequestScreenIntent.SendEta(eta))

					// Set selected driver and show dialog
					selectedDriver = driver
					showAlertDialog = true
				}
				true
			}
		)
	}

// Show AlertDialog for only one driver
	if (showAlertDialog && selectedDriver != null) {

		val initialDistance = calculateDistance(
			selectedDriver!!.latitude,
			selectedDriver!!.longitude,
			currentLocation.latitude,
			currentLocation.longitude
		).toDouble()

		val surgeMultiplier = getSurgeMultiplier()
		val fare = calculateFare(initialDistance, surgeMultiplier)
		onLocationAction(LocationRequestScreenIntent.SendFare(fare))

		AlertDialog(
			onDismissRequest = { showAlertDialog = false },
			title = { Text("${selectedDriver!!.name} - ${selectedDriver!!.carName}") },
			text = {
				Text(
					"Estimated arrival time: ${locationRequestState.eta} minutes\nFare: ₦${
						String.format(
							"%,.2f",
							locationRequestState.fare
						)
					}"
				)
			},
			confirmButton = {
				Button(onClick = {
					simulateDriverMovement(
						driver = selectedDriver!!,
						userLocation = currentLocation,
						locationRequestScreenViewModel = locationRequestScreenViewModel,
						price = { fare -> },
						onArrival = { arrival -> onLocationAction(LocationRequestScreenIntent.SendArrival(arrival))}
					)
					showAlertDialog = false
				}) {
					Text("Simulate Arrival")
				}
			}
		)
	}
}

@Composable
fun LiveLocationMap(
	uiSettings: MapUiSettings,
	properties: MapProperties,
	cameraPositionState: CameraPositionState,
	currentLocation: LatLng,
	locationRequestState: LocationRequestScreenState,
	onLocationAction: (LocationRequestScreenIntent) -> Unit,
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	onArrival: (Boolean, DriverEntity) -> Unit
) {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	// Location client setup
	val fusedLocationClient = remember {
		LocationServices.getFusedLocationProviderClient(context)
	}

	// Location callback
	val locationCallback = remember {
		object : LocationCallback() {
			override fun onLocationResult(result: LocationResult) {
				result.lastLocation?.let { location ->
					LatLng(currentLocation.latitude, currentLocation.longitude)
					// Animate camera to follow location
					scope.launch {
						cameraPositionState.animate(
							update = CameraUpdateFactory.newLatLng(
								LatLng(location.latitude, location.longitude)
							),
							durationMs = 1000
						)
					}
				}
			}
		}
	}

	// Request location updates
	LaunchedEffect(Unit) {
		val locationRequest = LocationRequest.Builder(
			Priority.PRIORITY_HIGH_ACCURACY,
			5000 // Update interval in milliseconds
		).build()

		if (ActivityCompat.checkSelfPermission(
				context,
				Manifest.permission.ACCESS_FINE_LOCATION
			) == PackageManager.PERMISSION_GRANTED
		) {
			fusedLocationClient.requestLocationUpdates(
				locationRequest,
				locationCallback,
				Looper.getMainLooper()
			)
		}
	}

	// Cleanup location updates
	DisposableEffect(Unit) {
		onDispose {
			fusedLocationClient.removeLocationUpdates(locationCallback)
		}
	}

	currentLocation.let { location ->
		GoogleMap(
			modifier = Modifier.fillMaxSize(),
			cameraPositionState = cameraPositionState,
			properties = properties,
			uiSettings = uiSettings
		) {
			LocationMarkerWithRipple(
				currentLocation = location,
				locationRequestState = locationRequestState,
				onLocationAction = onLocationAction,
				locationRequestScreenViewModel = locationRequestScreenViewModel,
				onArrival = { hasArrived, driver -> onArrival(hasArrived, driver) }
			)
		}
	}
}

@Composable
fun ArrivalNotification(driver: DriverEntity) {
	var showNotification by remember { mutableStateOf(true) }

	// Auto-hide after 5 seconds
	LaunchedEffect(showNotification) {
		if (showNotification) {
			delay(5000)
			showNotification = false
		}
	}

	AnimatedVisibility(
		visible = showNotification,
		enter = slideInVertically(
			animationSpec = tween(durationMillis = 300),
			initialOffsetY = { -it }
		),
		exit = slideOutVertically(
			animationSpec = tween(durationMillis = 300),
			targetOffsetY = { -it }
		),
		modifier = Modifier.fillMaxWidth()
	) {
		Surface(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			shape = RoundedCornerShape(8.dp),
			tonalElevation = 4.dp
		) {
			Row(
				modifier = Modifier.padding(16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				//TODO: Driver Image

				// Driver Information
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = "Driver Arrived!",
						style = MaterialTheme.typography.titleMedium
					)
					Text(
						text = driver.name,
						style = MaterialTheme.typography.bodyMedium
					)
					Text(
						text = "${driver.carName} • ${driver.carPlateNumber}",
						style = MaterialTheme.typography.bodySmall,
						color = Color.White.copy(alpha = 0.8f)
					)
				}

				// Close Button
				IconButton(
					onClick = { showNotification = false },
					modifier = Modifier.size(24.dp)
				) {
					Icon(
						imageVector = Icons.Default.Close,
						contentDescription = "Close notification",
						tint = Color.White
					)
				}
			}
		}
	}
}

@Composable
fun RiderButton(
	resource: String,
	actionClick: () -> Unit,
	enabled: Boolean
) {
	Button(
		modifier = Modifier
			.fillMaxWidth()
			.height(42.dp),
		onClick = {
			actionClick.invoke()
		},
		enabled = enabled,
		content = {
			Text(
				text = resource,
				fontSize = 13.sp,
				color = Color.White
			)
		}
	)
}