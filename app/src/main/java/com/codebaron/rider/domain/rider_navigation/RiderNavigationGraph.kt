package com.codebaron.rider.domain.rider_navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codebaron.rider.presentation.home_screen.ui.HomeScreenRoot
import com.codebaron.rider.presentation.home_screen.view_model.HomeScreenViewModel
import com.codebaron.rider.presentation.onboard_screen.ui.LocationRequestScreenRoot
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenViewModel
import com.codebaron.rider.presentation.splash_screen.ui.SplashScreenRoot
import com.codebaron.rider.presentation.splash_screen.view_model.SplashScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RiderNavigationGraph() {
	val navController = rememberNavController()

	NavHost(
		navController = navController,
		startDestination = NavigationRoutes.SplashScreen
	) {
		composable<NavigationRoutes.SplashScreen>(
			exitTransition = { fadeOut() },
			popEnterTransition = { fadeIn() },
			content = {
				val splashScreenViewModel: SplashScreenViewModel = koinViewModel()
				SplashScreenRoot(
					splashScreenViewModel = splashScreenViewModel,
					navController = navController
				)
			}
		)

		composable<NavigationRoutes.LocationRequestScreen>(
			exitTransition = { fadeOut() },
			popEnterTransition = { fadeIn() },
			content = {
				val locationRequestScreenViewModel: LocationRequestScreenViewModel = koinViewModel()
				LocationRequestScreenRoot(
					locationRequestScreenViewModel = locationRequestScreenViewModel,
					navController = navController
				)
			}
		)

		composable<NavigationRoutes.HomeScreen>(
			exitTransition = { fadeOut() },
			popEnterTransition = { fadeIn() },
			content = {
				val homeScreenViewModel: HomeScreenViewModel = koinViewModel()
				val locationRequestScreenViewModel: LocationRequestScreenViewModel = koinViewModel()
				HomeScreenRoot(
					locationRequestScreenViewModel = locationRequestScreenViewModel,
					homeScreenViewModel = homeScreenViewModel,
					navController = navController
				)
			}
		)
	}
}