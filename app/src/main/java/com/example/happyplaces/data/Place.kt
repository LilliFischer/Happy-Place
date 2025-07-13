package com.example.happyplaces.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "happy_places_table") // Let's ensure this table name is what we want consistently
data class Place(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val imageUri: String, // Kept as non-nullable from your last update
    val note: String?,
    val date: String?,       // Good, nullable
    val latitude: Double?,   // Good, nullable for now
    val longitude: Double?,  // Good, nullable for now
    val address: String?     // Good, this will be our primary textual address
)



