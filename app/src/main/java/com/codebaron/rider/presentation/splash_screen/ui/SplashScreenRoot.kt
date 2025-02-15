package com.codebaron.rider.presentation.splash_screen.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codebaron.rider.R
import com.codebaron.rider.domain.internt_config.ConnectivityCheckerProvider
import com.codebaron.rider.domain.rider_navigation.NavigationRoutes
import com.codebaron.rider.presentation.splash_screen.view_model.SplashScreenIntent
import com.codebaron.rider.presentation.splash_screen.view_model.SplashScreenState
import com.codebaron.rider.presentation.splash_screen.view_model.SplashScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreenRoot(splashScreenViewModel: SplashScreenViewModel, navController: NavHostController) {
	val splashScreenState by splashScreenViewModel.state.collectAsState(initial = SplashScreenState())

	SplashScreen(
		splashScreenState = splashScreenState,
		splashScreenViewModel = splashScreenViewModel,
		onAction = { intent -> splashScreenViewModel.sendIntent(intent) },
		navController = navController
	)
}

@Composable
fun SplashScreen(
	splashScreenState: SplashScreenState, // Holds the UI state of the splash screen
	splashScreenViewModel: SplashScreenViewModel, // ViewModel for handling business logic and state
	onAction: (SplashScreenIntent) -> Unit, // Lambda function for handling user actions
	navController: NavHostController // Navigation controller for managing screen transitions
) {
	val context = LocalContext.current // Get the current context
	val checker = ConnectivityCheckerProvider.getConnectivityChecker(context = context) // Initialize the connectivity checker
	var isDeviceConnectedToInternet by remember { mutableStateOf(false) } // State to track internet connectivity

	LaunchedEffect(key1 = Unit) { // Effect that runs when the composable is first launched
		launch {
			isDeviceConnectedToInternet = checker.getConnectivityState().isConnected // Check internet connectivity
		}
		launch {
			delay(2000) // Delay for 2 seconds to show the splash screen
			onAction(SplashScreenIntent.NavigateToLocationScreen) // Trigger navigation intent
		}
		splashScreenViewModel.navigationEvent.collect { destination -> // Observe navigation events from ViewModel
			navController.navigate(route = destination) { // Navigate to the specified destination
				popUpTo(route = NavigationRoutes.SplashScreen) { inclusive = true } // Remove SplashScreen from the back stack
			}
		}
	}

	Scaffold(
		content = { paddingValues ->
			Box(
				modifier = Modifier
					.padding(paddingValues = paddingValues)
					.fillMaxSize()
					.windowInsetsPadding(WindowInsets.safeDrawing),
				contentAlignment = Alignment.Center,
				content = {
					Image(
						modifier = Modifier.width(100.dp).height(150.dp),
						painter = painterResource(id = R.drawable.taxi_logo),
						contentScale = ContentScale.FillWidth,
						contentDescription = null
					)
				}
			)
		}
	)
}
