package com.example.happyplaces.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao // Marks this as a Data Access Object for Room
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    @Query("SELECT * FROM happy_places_table ORDER BY id DESC")
    fun getAllPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM happy_places_table WHERE id = :placeId")
    fun getPlaceById(placeId: Int): Flow<Place?> // Nullable if place might not be found

    @Delete
    suspend fun deletePlace(place: Place)

    @Update
    suspend fun updatePlace(place: Place)
}
