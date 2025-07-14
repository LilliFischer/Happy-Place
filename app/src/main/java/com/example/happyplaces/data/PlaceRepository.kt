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

    val allPlaces: Flow<List<Place>> = placeDao.getAllPlaces()

    fun getPlaceById(placeId: Int): Flow<Place?> {
        return placeDao.getPlaceById(placeId)
    }

    suspend fun insertPlace(place: Place) {
        Log.d("PlaceRepository", "DAO insert called for: ${place.title}")
        placeDao.insertPlace(place)
        Log.d("PlaceRepository", "DAO insert finished for: ${place.title}")
    }

    suspend fun deletePlace(place: Place) {
        Log.d("PlaceRepository", "DAO delete called for: ${place.title}")
        placeDao.deletePlace(place) // This should now correctly call the DAO method
        Log.d("PlaceRepository", "DAO delete finished for: ${place.title}")
    }

    suspend fun updatePlace(place: Place) {
        Log.d("PlaceRepository", "DAO update called for: ${place.title}, ID: ${place.id}")
        placeDao.updatePlace(place)
        Log.d("PlaceRepository", "DAO update finished for: ${place.title}, ID: ${place.id}")
    }
}
