plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.ksp.android)
	alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {

	namespace = "com.codebaron.rider"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.codebaron.rider"
		minSdk = 27
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.navigation.compose)
	implementation(libs.play.services.location)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)

	//added dependencies
	implementation(libs.ktor.client.android)
	implementation(libs.ktor.client.json)
	implementation(libs.ktor.client.serialization)
	implementation(libs.ktor.client.logging)
	implementation(libs.koin.androidx.compose)
	implementation(libs.koin.test)
	testImplementation(libs.koin.android.test)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)
	implementation(libs.maps.compose)
	runtimeOnly(libs.androidx.material.icons.extended)
	implementation(libs.places)
	testImplementation(libs.junit)
	testImplementation(libs.mockk)
}