package com.codebaron.rider.presentation.splash_screen.view_model

sealed class SplashScreenIntent {
	data object NavigateToLocationScreen: SplashScreenIntent()
	data object NavigateToHomeScreen: SplashScreenIntent()
}