package com.codebaron.rider.data.utils

import java.util.Calendar

//These classes were created for testing purposes only.
interface TimeProvider {
	fun getCurrentHour(): Int
}

class RealTimeProvider : TimeProvider {
	override fun getCurrentHour(): Int {
		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
	}
}