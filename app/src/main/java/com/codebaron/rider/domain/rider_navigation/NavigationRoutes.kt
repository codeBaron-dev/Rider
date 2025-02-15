package com.codebaron.rider.domain.rider_navigation

import kotlinx.serialization.Serializable

sealed interface NavigationRoutes {

	@Serializable
	data object SplashScreen: NavigationRoutes
	@Serializable
	data object LocationRequestScreen: NavigationRoutes
	@Serializable
	data object HomeScreen: NavigationRoutes
}