package com.codebaron.rider.domain.koin

import android.app.Application
import com.codebaron.rider.domain.internt_config.RiderInternetConnectivityChecker
import com.google.android.libraries.places.api.Places
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RiderApp: Application() {
	override fun onCreate() {
		super.onCreate()
		RiderInternetConnectivityChecker(context = this)
		Places.initialize(this, "AIzaSyA5UFQsgV7zTLuK-1KaTXzYpSZcApZQTQA")
		startKoin {
			androidLogger()
			androidContext(this@RiderApp)
			modules(riderModules)
		}
	}
}