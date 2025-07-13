package com.example.happyplaces.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

/**
 * Repository module for handling data operations.
 * This class abstracts the data source (Room database in this case) from the
 * ViewModel.
 *
 * @param placeDao The Data Access Object for Place entities.
 */
class PlaceRepository(private val placeDao: PlaceDao) {

    /**
     * Retrieves all places from the database as a Flow.
     * The Flow will automatically update when the data changes in the database.
     *
     * @return A Flow emitting a list of Place objects.
     */
    val allPlaces: Flow<List<Place>> = placeDao.getAllPlaces()

    /**
     * Retrieves a single place by its ID from the database as a Flow.
     *
     * @param placeId The ID of the place to retrieve.
     * @return A Flow emitting the Place object, or null if not found.
     */
    fun getPlaceById(placeId: Int): Flow<Place?> {
        return placeDao.getPlaceById(placeId)
    }

    /**
     * Inserts a new place into the database.
     * This is a suspend function, as database operations should be performed off the main thread.
     *
     * @param place The Place object to insert.
     */
    suspend fun insertPlace(place: Place) {
        Log.d("PlaceRepository", "DAO insert called for: ${place.title}")
        placeDao.insertPlace(place)
        Log.d("PlaceRepository", "DAO insert finished for: ${place.title}")
    }

    // You can add other methods here later, like updatePlace or deletePlace,
    // and they would also call the corresponding methods in the placeDao.
    // For example:
    //
    // suspend fun updatePlace(place: Place) {
    //     placeDao.updatePlace(place) // Assuming you add updatePlace to PlaceDao
    // }
    //
    // suspend fun deletePlace(place: Place) {
    //     placeDao.deletePlace(place) // Assuming you add deletePlace to PlaceDao
    // }
}
