package com.example.happyplaces

import android.app.Application
import androidx.lifecycle.*
import com.example.happyplaces.data.AppDatabase
import com.example.happyplaces.data.Place
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).placeDao()
    val allPlaces: LiveData<List<Place>> = dao.getAll()

    fun insert(place: Place) = viewModelScope.launch {
        dao.insert(place)
    }

    fun update(place: Place) = viewModelScope.launch {
        dao.update(place)
    }

    fun delete(place: Place) = viewModelScope.launch {
        dao.delete(place)
    }
}
