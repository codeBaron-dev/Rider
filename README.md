# RideHailing Application - README

![App Demo](demo.gif)  
*A demonstration of the core functionality (simulated data)*

## Overview  
A ride-hailing Android application that demonstrates real-time location tracking, driver simulation, and fare estimation. Built with **Jetpack Compose**, **Room Database**, and **Google Maps SDK**, this project emphasizes offline-first functionality and spatial data management. 

---

## Key Features  

### 1. Location Services & Permissions  
- **First-Launch Workflow**:  
  - System dialog for `ACCESS_FINE_LOCATION` permission  
  - Graceful degradation to `ACCESS_COARSE_LOCATION` if denied  
  - Continuous background updates (15-second intervals)  

- **Data Persistence**:  
  - Room Database stores user/driver coordinates  
  - Schema: `UserLocation` (latitude, longitude, timestamp)  
  - DAO methods for CRUD operations with Flow support  

### 2. Driver Simulation Engine  
- **Movement Algorithm**:  
  - Bearing calculation via spherical geometry  
  - 50-meter displacement per simulation step (1Hz refresh)  
  - Linear interpolation between coordinates  

- **Arrival Detection**:  
  - 10-meter proximity threshold  
  - Coroutine-driven state machine (En Route → Arrived)  

### 3. Fare & ETA Calculation  
- **Pricing Model**:  
  - Base Fare: $2.50  
  - Distance Rate: $1.00/km (Haversine-calculated)  
  - Surge Multipliers:  
    - Time-Based (1.5x during 7-9 AM, 5-7 PM)  
    - Traffic Conditions (1.3x simulated)  

- **ETA Estimation**:  
  - Constant velocity assumption (50 km/h)  
  - Real-time updates during driver movement  

---

## Technical Architecture  

### Component Breakdown  
| Layer          | Technologies                          | Key Responsibilities              |  
|----------------|---------------------------------------|-----------------------------------|  
| **UI**         | Jetpack Compose, Google Maps Compose  | Map rendering, permission dialogs |  
| **Domain**     | Kotlin Coroutines, Flow               | Fare calculation, ETA logic       |  
| **Data**       | Room, FusedLocationProviderClient     | Location storage/retrieval        |  

### Critical Functions  
1. **Location Updates**:  
   - Utilizes `FusedLocationProviderClient`  
   - Persists to Room DB on successful fix  
   - Emits updates via `SharedFlow`  

2. **Driver Animation**:  
   - `getBearing()`: Computes directional vector  
   - `simulateDriverMovement()`: Handles coroutine lifecycle  
   - `calculateNewPosition()`: Updates Lat/Lng  

3. **Surge Pricing**:  
   - Time-of-day detection via `Calendar`  
   - Multiplier applied to base fare  

---

## Installation  

### Prerequisites  
- Android Studio Giraffe 2022.3.1+  
- Google Maps API Key (debug-only)  

### Steps  
1. Add API key to `local.properties`:  
   ```properties
   MAPS_API_KEY=YOUR_DEBUG_KEY
