package com.codebaron.rider.domain.internt_config

import android.content.Context

object ConnectivityCheckerProvider {
	fun getConnectivityChecker(context: Context): ConnectivityChecker {
		return RiderInternetConnectivityChecker(context = context)
	}
}