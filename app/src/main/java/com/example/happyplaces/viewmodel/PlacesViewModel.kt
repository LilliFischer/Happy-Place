package com.example.happyplaces.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.happyplaces.data.AppDatabase
import com.example.happyplaces.data.Place
import com.example.happyplaces.data.PlaceRepository
import kotlinx.coroutines.launch
import android.util.Log
import java.util.UUID

/**
 * ViewModel for the Happy Places list/grid screen.
 * It interacts with the PlaceRepository to fetch and manage Place data.
 *
 * @param application The application context, needed to initialize the database and repository.
 */
class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelInstanceId = UUID.randomUUID().toString()
    private val repository: PlaceRepository
    val allPlaces: LiveData<List<Place>>

    init {
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId initializing...")
        val placeDao = AppDatabase.getDatabase(application).placeDao()
        repository = PlaceRepository(placeDao)
        allPlaces = repository.allPlaces.asLiveData()
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId initialized. allPlaces is set.")
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way.
     *
     * @param place The Place object to insert.
     */
    fun insertPlace(place: Place) {
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId attempting to insert place: ${place.title}")
        viewModelScope.launch {
            try {
                repository.insertPlace(place)
                Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId place insertion successful: ${place.title}")
            } catch (e: Exception) {
                Log.e("PlacesViewModel", "ViewModel instance $viewModelInstanceId error inserting place: ${place.title}", e)
            }
        }
    }

    fun getInstanceId(): String {
        return viewModelInstanceId
    }

    // You could add other functions here to interact with the repository,
    // for example, to get a specific place by ID if needed for navigation
    // to a detail screen from the grid.
    //
    // fun getPlace(id: Int): LiveData<Place?> {
    //     return repository.getPlaceById(id).asLiveData()
    // }
}

