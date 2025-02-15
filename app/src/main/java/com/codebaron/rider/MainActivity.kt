package com.codebaron.rider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.codebaron.rider.domain.rider_navigation.RiderNavigationGraph
import com.codebaron.rider.presentation.theme.RiderTheme
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			RiderTheme {
				KoinAndroidContext { RiderNavigationGraph() }
			}
		}
	}
}