package com.example.happyplaces.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao // Marks this as a Data Access Object for Room
interface PlaceDao {

    /**
     * Inserts a new place into the table. If a place with the same ID already exists,
     * it will be replaced.
     * This function is a suspend function, meaning it should be called from a coroutine
     * or another suspend function, as database operations can be long-running.
     *
     * @param place The Place object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    /**
     * Retrieves all places from the happy_places_table, ordered by ID in descending order
     * (newest places first).
     *
     * @return A Flow that emits a list of Place objects. Flow allows the UI to observe
     *         changes to the data automatically and reactively update.
     */
    @Query("SELECT * FROM happy_places_table ORDER BY id DESC")
    fun getAllPlaces(): Flow<List<Place>>

    /**
     * Retrieves a single place from the table by its ID.
     * This will be useful later when you want to view the details of a specific place.
     *
     * @param placeId The ID of the place to retrieve.
     * @return A Flow that emits a single Place object, or null if no place with that ID exists.
     */
    @Query("SELECT * FROM happy_places_table WHERE id = :placeId")
    fun getPlaceById(placeId: Int): Flow<Place?> // Nullable if place might not be found
}
