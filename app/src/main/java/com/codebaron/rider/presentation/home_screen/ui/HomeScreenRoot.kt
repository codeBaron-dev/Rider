@file:OptIn(ExperimentalMaterial3Api::class)

package com.codebaron.rider.presentation.home_screen.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.codebaron.rider.data.local.drivers.DriverEntity
import com.codebaron.rider.presentation.home_screen.view_model.HomeScreenViewModel
import com.codebaron.rider.presentation.onboard_screen.ui.ArrivalNotification
import com.codebaron.rider.presentation.onboard_screen.ui.FindDriverScreen
import com.codebaron.rider.presentation.onboard_screen.ui.LiveLocationMap
import com.codebaron.rider.presentation.onboard_screen.ui.VehicleSelectionUI
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenIntent
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenState
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun HomeScreenRoot(
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	homeScreenViewModel: HomeScreenViewModel,
	navController: NavHostController
) {

	val locationRequestState by locationRequestScreenViewModel.state.collectAsStateWithLifecycle(
		initialValue = LocationRequestScreenState()
	)

	HomeScreen(
		onLocationAction = { intent -> locationRequestScreenViewModel.sendIntent(intent) },
		locationRequestState = locationRequestState,
		locationRequestScreenViewModel,
		navController = navController,
		homeScreenViewModel = homeScreenViewModel
	)
}

@Composable
fun HomeScreen(
	onLocationAction: (LocationRequestScreenIntent) -> Unit,
	locationRequestState: LocationRequestScreenState,
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	navController: NavHostController,
	homeScreenViewModel: HomeScreenViewModel
) {
	val context = LocalContext.current
	var hasArrived by remember { mutableStateOf(false) }
	var selectedDriver by remember { mutableStateOf<DriverEntity?>(null) }
	val scope = rememberCoroutineScope()
	val myCurrentLocation = LatLng(locationRequestState.latitude, locationRequestState.longitude)
	//val myCurrentLocation = LatLng(6.613480, 3.297420)
	val cameraPositionState = rememberCameraPositionState {
		position = CameraPosition.fromLatLngZoom(myCurrentLocation, 15f)
	}
	val uiSettings by remember {
		mutableStateOf(
			MapUiSettings(
				zoomControlsEnabled = true,
				myLocationButtonEnabled = true
			)
		)
	}
	val properties by remember {
		mutableStateOf(
			MapProperties(
				mapType = MapType.TERRAIN,
				isTrafficEnabled = true,
				isBuildingEnabled = true,
				minZoomPreference = 10f,  // Minimum zoom level
				maxZoomPreference = 18f   // Maximum zoom level
			)
		)
	}
	val sheetState = rememberStandardBottomSheetState(
		initialValue = SheetValue.PartiallyExpanded,
		confirmValueChange = { it == SheetValue.PartiallyExpanded } // Prevents full expansion or hiding
	)
	LaunchedEffect(key1 = Unit) {
		launch { onLocationAction(LocationRequestScreenIntent.GetAllSavedLocation) }
		cameraPositionState.animate(
			update = CameraUpdateFactory.newCameraPosition(
				CameraPosition.Builder()
					.target(myCurrentLocation)
					.zoom(18f)  // Closer zoom level (1-20, where 20 is closest)
					.bearing(0f)
					.tilt(45f)  // Adds a nice tilt to the map
					.build()
			),
			durationMs = 1000  // Animation duration in milliseconds
		)
		homeScreenViewModel.navigationEvent.collect { destination ->
			navController.navigate(route = destination)
		}
	}

	LiveLocationMap(
		uiSettings,
		properties,
		cameraPositionState,
		myCurrentLocation,
		locationRequestState,
		onLocationAction,
		locationRequestScreenViewModel,
		onArrival = { arrived, driver ->
			hasArrived = arrived
			selectedDriver = driver
		}
	)
	BottomSheetScaffold(
		scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState),
		sheetPeekHeight = 400.dp, // Half-expanded height
		sheetSwipeEnabled = false, // Prevents user from swiping to dismiss
		sheetContent = { DefaultSheet(locationRequestState, locationRequestScreenViewModel) },
		topBar = {
			TopAppBar(
				title = {},
				navigationIcon = {},
				modifier = Modifier.padding(horizontal = 10.dp),
				colors = TopAppBarColors(
					containerColor = Color.Transparent,
					scrolledContainerColor = Color.Transparent,
					navigationIconContentColor = Color.Transparent,
					titleContentColor = Color.Transparent,
					actionIconContentColor = Color.Transparent
				),
				actions = {
					Box(
						contentAlignment = Alignment.TopEnd,
						content = {
							FloatingActionButton(
								onClick = {
									// Animate camera back to user's location
									scope.launch {
										cameraPositionState.animate(
											update = CameraUpdateFactory.newCameraPosition(
												CameraPosition.Builder()
													.target(myCurrentLocation)
													.zoom(18f)
													.bearing(0f)
													.tilt(45f)
													.build()
											),
											durationMs = 1000
										)
									}
								},
								content = {
									Icon(
										imageVector = Icons.Default.LocationOn,
										contentDescription = "Edit"
									)
								}
							)
						}
					)
				}
			)
		},
		content = {
			// Handle arrival notification
			if (locationRequestState.hasArrived) {
				Toast.makeText(context, "Your driver has arrived", Toast.LENGTH_SHORT).show()
				ArrivalNotification(driver = locationRequestState.driver!!)
			}
		}
	)
}

@Composable
fun DefaultSheet(locationRequestState: LocationRequestScreenState, locationRequestScreenViewModel: LocationRequestScreenViewModel) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		content = {
			VehicleSelectionUI()
			FindDriverScreen(locationRequestState, locationRequestScreenViewModel)
		}
	)
}