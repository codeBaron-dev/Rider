package com.codebaron.rider.domain.koin

import androidx.room.Room
import com.codebaron.rider.data.local.location.LocationDatabase
import com.codebaron.rider.presentation.home_screen.view_model.HomeScreenViewModel
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenRepository
import com.codebaron.rider.presentation.onboard_screen.view_model.LocationRequestScreenViewModel
import com.codebaron.rider.presentation.splash_screen.view_model.SplashScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val riderModules = module {
	viewModel { SplashScreenViewModel() }
	viewModel { HomeScreenViewModel() }
	viewModel { LocationRequestScreenViewModel(locationRepository = get()) }

	single {
		Room.databaseBuilder(
			get(),
			LocationDatabase::class.java,
			"rider_database"
		).build()
	}
	single { get<LocationDatabase>().locationDao() }
	single { get<LocationDatabase>().driverDao() }
	single { LocationRequestScreenRepository(locationDao = get(), driverDao = get()) }
}