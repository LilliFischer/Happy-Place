package com.example.happyplaces.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.happyplaces.data.AppDatabase
import com.example.happyplaces.data.Place
import com.example.happyplaces.data.PlaceRepository // Ensure this is the correct repository class
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow // Keep this if getPlaceById returns Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

// Removed duplicate imports for viewModelScope and launch if AndroidViewModel already provides scope

/**
 * ViewModel for Happy Places screens.
 * It interacts with the PlaceRepository to fetch and manage Place data,
 * and also holds temporary draft state for adding/editing places.
 *
 * @param application The application context, needed to initialize the database and repository.
 */
class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    private val viewModelInstanceId = UUID.randomUUID().toString()
    private val repository: PlaceRepository
    val allPlaces: LiveData<List<Place>> // Existing LiveData for all places

    // --- State for PlacesGridScreen interactions ---
    private val _expandedPlaceId = MutableStateFlow<Int?>(null)
    val expandedPlaceId: StateFlow<Int?> = _expandedPlaceId.asStateFlow()

    private val _longPressedPlaceId = MutableStateFlow<Int?>(null)
    val longPressedPlaceId: StateFlow<Int?> = _longPressedPlaceId.asStateFlow()

    private val _placeToDelete = MutableStateFlow<Place?>(null)
    val placeToDelete: StateFlow<Place?> = _placeToDelete.asStateFlow()

    private val _showDeleteConfirmationDialog = MutableStateFlow(false)
    val showDeleteConfirmationDialog: StateFlow<Boolean> = _showDeleteConfirmationDialog.asStateFlow()

    // --- NEW: State for AddPlacesScreen Draft Data ---
    private val _draftTitle = MutableStateFlow("")
    val draftTitle: StateFlow<String> = _draftTitle.asStateFlow()

    private val _draftNote = MutableStateFlow("")
    val draftNote: StateFlow<String> = _draftNote.asStateFlow()

    private val _draftImagePath = MutableStateFlow<String?>(null)
    val draftImagePath: StateFlow<String?> = _draftImagePath.asStateFlow()

    private val _isInitialDataLoaded = MutableStateFlow(false)
    val isInitialDataLoaded: StateFlow<Boolean> = _isInitialDataLoaded.asStateFlow()

    fun loadInitialPlaces() {
        viewModelScope.launch {
            // Your actual logic to load places for the grid
            // For example, fetching from Room database
            try {
                // Simulate loading
                delay(1500) // Adjust this delay to simulate your actual load time
                // val places = allPlaces.first() // Example: if allPlaces is a Flow
                // After loading, update the state
                _isInitialDataLoaded.value = true
            } catch (e: Exception) {
                // Handle error, maybe still set loaded to true to dismiss splash
                _isInitialDataLoaded.value = true
                Log.e("PlacesViewModel", "Error loading initial places", e)
            }
        }
    }

    init {
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId initializing...")
        val placeDao = AppDatabase.getDatabase(application).placeDao()
        repository = PlaceRepository(placeDao) // Initialize your repository
        allPlaces = repository.allPlaces.asLiveData() // Assuming allPlaces is a Flow from repository
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId initialized. allPlaces is set.")

        // Call the function to load initial data and update the splash screen condition flag
        loadInitialPlaces()
    }

    // --- Functions to update draft state ---
    fun updateDraftTitle(title: String) {
        _draftTitle.value = title
    }

    fun updateDraftNote(note: String) {
        _draftNote.value = note
    }

    fun updateDraftImagePath(path: String?) {
        _draftImagePath.value = path
    }

    // --- Function to load data into draft for editing ---
    fun loadDraftData(place: Place) {
        Log.d("PlacesViewModel", "Loading draft data for: ${place.title}")
        _draftTitle.value = place.title
        _draftNote.value = place.note ?: ""
        _draftImagePath.value = place.imageUri
        // If you move lat/lon/address to ViewModel draft state, load them here too
    }

    // --- Function to clear draft data ---
    fun clearDraftData() {
        Log.d("PlacesViewModel", "Clearing draft data.")
        _draftTitle.value = ""
        _draftNote.value = ""
        _draftImagePath.value = null
        // If you move lat/lon/address to ViewModel draft state, clear them here too
    }

    // --- Existing functions for data operations ---
    fun getPlaceByIdFlow(placeId: Int): Flow<Place?> {
        // This function will be used by AddPlacesScreen to load a place for editing
        return repository.getPlaceById(placeId)
    }

    fun updatePlace(place: Place) {
        viewModelScope.launch {
            try {
                repository.updatePlace(place)
                Log.d("PlacesViewModel", "ViewModel: Place updated successfully: ${place.title}")
                clearDraftData() // Clear draft after successful update
            } catch (e: Exception) {
                Log.e("PlacesViewModel", "ViewModel: Error updating place: ${place.title}", e)
            }
        }
    }

    fun insertPlace(place: Place) {
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId attempting to insert place: ${place.title}")
        viewModelScope.launch {
            try {
                repository.insertPlace(place)
                Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId place insertion successful: ${place.title}")
                clearDraftData() // Clear draft after successful insertion
            } catch (e: Exception) {
                Log.e("PlacesViewModel", "ViewModel instance $viewModelInstanceId error inserting place: ${place.title}", e)
            }
        }
    }

    fun deletePlace(place: Place) { // Changed from confirmDeletePlace to just deletePlace
        viewModelScope.launch {
            try {
                repository.deletePlace(place)
                Log.d("PlacesViewModel", "Place deleted: ${place.title}")
            } catch (e: Exception) {
                Log.e("PlacesViewModel", "Error deleting place: ${place.title}", e)
            }
        }
    }


    // --- Existing functions for UI state management (Grid Screen) ---
    fun getInstanceId(): String {
        return viewModelInstanceId
    }

    fun onPlaceClicked(placeId: Int) {
        _expandedPlaceId.value = if (_expandedPlaceId.value == placeId) null else placeId
        _longPressedPlaceId.value = null
    }

    fun clearExpandedPlace() {
        _expandedPlaceId.value = null
    }

    fun clearLongPressedPlace() {
        _longPressedPlaceId.value = null
    }

    fun onPlaceLongPressed(placeId: Int) {
        _longPressedPlaceId.value = placeId
        _expandedPlaceId.value = null
    }

    fun requestDeleteConfirmation(place: Place) {
        _placeToDelete.value = place
        _showDeleteConfirmationDialog.value = true
    }

    fun confirmDeletePlace() { // This function now primarily handles the deletion logic after confirmation
        _placeToDelete.value?.let { place ->
            deletePlace(place) // Call the actual delete operation
        }
        _showDeleteConfirmationDialog.value = false
        _placeToDelete.value = null
        clearLongPressedPlace() // Clear selection from the grid
    }

    fun dismissDeleteConfirmationDialog() {
        _placeToDelete.value = null
        _showDeleteConfirmationDialog.value = false
        // Optionally clearLongPressedPlace() here if you want icons to disappear on cancel.
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("PlacesViewModel", "ViewModel instance $viewModelInstanceId onCleared called.")
        // You might consider calling clearDraftData() here if it makes sense for your app's lifecycle
        // clearDraftData()
    }
}


