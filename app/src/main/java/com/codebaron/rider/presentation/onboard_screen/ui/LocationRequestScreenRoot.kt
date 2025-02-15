package com.codebaron.rider.presentation.onboard_screen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.codebaron.rider.R
import com.codebaron.rider.data.utils.location_permission_body_text
import com.codebaron.rider.data.utils.location_permission_header_text
import com.codebaron.rider.domain.rider_navigation.NavigationRoutes
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenIntent
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenState
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenViewModel
import kotlinx.coroutines.launch

@Composable
fun LocationRequestScreenRoot(
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	navController: NavHostController
) {
	val locationRequestState by locationRequestScreenViewModel.state.collectAsStateWithLifecycle(
		initialValue = LocationRequestScreenState()
	)

	LocationRequestScreen(
		onAction = { intent -> locationRequestScreenViewModel.sendIntent(intent) },
		locationRequestState = locationRequestState,
		locationRequestScreenViewModel = locationRequestScreenViewModel,
		navController = navController
	)
}

@Composable
fun LocationRequestScreen(
	onAction: (LocationRequestScreenIntent) -> Unit,
	locationRequestState: LocationRequestScreenState,
	locationRequestScreenViewModel: LocationRequestScreenViewModel,
	navController: NavHostController
) {

	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	LaunchedEffect(key1 = Unit) {
		launch { locationRequestScreenViewModel.initialize(context = context) }
		locationRequestScreenViewModel.navigationEvent.collect { destination ->
			navController.navigate(route = destination) {
				popUpTo(route = NavigationRoutes.LocationRequestScreen) { inclusive = true }
			}
		}
	}

	Scaffold(
		topBar = {},
		bottomBar = {
			BottomAppBar(
				containerColor = Color.Transparent,
				modifier = Modifier
					.fillMaxWidth()
					.height(250.dp)
					.padding(horizontal = 16.dp),
				tonalElevation = 0.dp,
				content = {
					Column(
						modifier = Modifier.fillMaxWidth(),
						verticalArrangement = Arrangement.Center,
						content = {
							Text(
								text = location_permission_header_text,
								textAlign = TextAlign.Start,
								fontSize = 32.sp
							)
							Text(
								text = location_permission_body_text,
								textAlign = TextAlign.Start,
							)
							Spacer(modifier = Modifier.height(10.dp))
							RiderButton(
								resource = "Allow",
								actionClick = {
									scope.launch {
										//trigger location request
										onAction(LocationRequestScreenIntent.LocationPermissionActionClick)
									}
								},
								enabled = true
							)
						}
					)
				}
			)
		},
		content = { paddingValues ->
			when {
				locationRequestState.showPermissionRequest -> {
					LocationPermissionHandler(
						onLocationReceived = { location ->
							scope.launch {
								onAction(
									LocationRequestScreenIntent.LocationReceived(location)
								)
							}
						}
					)
				}
			}

			Box(
				modifier = Modifier
					.padding(paddingValues = paddingValues)
					.fillMaxSize()
					.windowInsetsPadding(WindowInsets.safeDrawing),
				contentAlignment = Alignment.Center,
				content = {
					Image(
						modifier = Modifier
							.width(200.dp)
							.height(250.dp),
						painter = painterResource(id = R.drawable.map_dark),
						contentScale = ContentScale.FillWidth,
						contentDescription = null
					)
				}
			)
		}
	)
}