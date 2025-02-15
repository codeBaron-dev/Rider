package com.codebaron.rider.presentation.home_screen.view_model

import androidx.lifecycle.ViewModel
import com.codebaron.rider.domain.rider_navigation.NavigationRoutes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class HomeScreenViewModel: ViewModel() {

	private val _navigationEvent = MutableSharedFlow<NavigationRoutes>()
	val navigationEvent = _navigationEvent.asSharedFlow()
}