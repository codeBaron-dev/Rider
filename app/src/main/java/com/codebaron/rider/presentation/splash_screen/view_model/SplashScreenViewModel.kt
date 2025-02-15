package com.codebaron.rider.presentation.splash_screen.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebaron.rider.domain.rider_navigation.NavigationRoutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SplashScreenViewModel: ViewModel() {

	private val _state = MutableStateFlow(SplashScreenState())
	val state = _state.asSharedFlow()
	private val intents = MutableSharedFlow<SplashScreenIntent>()
	private val _navigationEvent = MutableSharedFlow<NavigationRoutes>()
	val navigationEvent = _navigationEvent.asSharedFlow()

	init {
		handleIntents()
		viewModelScope.launch {
			_state.emit(SplashScreenState())
		}
	}

	fun sendIntent(intent: SplashScreenIntent) {
		viewModelScope.launch {
			intents.emit(intent)
		}
	}

	private fun handleIntents() = viewModelScope.launch {
		intents.collect { intent ->
			when (intent) {
				is SplashScreenIntent.NavigateToHomeScreen -> _navigationEvent.emit(value = NavigationRoutes.HomeScreen)
				SplashScreenIntent.NavigateToLocationScreen -> _navigationEvent.emit(value = NavigationRoutes.LocationRequestScreen)
			}
		}
	}
}