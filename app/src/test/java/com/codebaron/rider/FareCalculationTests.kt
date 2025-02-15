package com.codebaron.rider

import com.codebaron.rider.data.utils.TimeProvider
import com.codebaron.rider.data.utils.calculateFare
import com.codebaron.rider.data.utils.getSurgeMultiplier
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class FareCalculationTests {

	private val mockTimeProvider = mockk<TimeProvider>()

	@Test
	fun `basic fare calculation`() {
		// Given
		every { mockTimeProvider.getCurrentHour() } returns 14 // 2 PM
		val distance = 5000.0 // 5 km

		// When
		val fare = calculateFare(distance, getSurgeMultiplier(mockTimeProvider))

		// Then
		assertEquals(7.50, fare)
	}

	@Test
	fun `peak hour surge pricing`() {
		// Given
		every { mockTimeProvider.getCurrentHour() } returns 8 // 8 AM
		val distance = 8000.0 // 8 km

		// When
		val fare = calculateFare(distance, getSurgeMultiplier(mockTimeProvider))

		// Then
		assertEquals(14.00, fare) // Using your expected value
	}

	@Test
	fun `traffic surge pricing`() {
		// When
		val fare = calculateFare(6000.0, 1.3)

		// Then
		assertEquals(10.30, fare) // 2.50 + (6 * 1.3) = 10.30
	}

}