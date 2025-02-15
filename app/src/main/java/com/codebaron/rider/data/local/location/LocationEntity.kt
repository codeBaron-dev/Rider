package com.codebaron.rider.data.local.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Long = 0,
	val latitude: Double,
	val longitude: Double,
	val address: String,
	@ColumnInfo(name = "created_at")
	val createdAt: Long = System.currentTimeMillis()
)
