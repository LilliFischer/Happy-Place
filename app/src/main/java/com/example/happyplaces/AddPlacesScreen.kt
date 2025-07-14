package com.example.happyplaces

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.happyplaces.data.Place
import com.example.happyplaces.viewmodel.PlacesViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream // For saveUriToFile
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.format(digits: Int): String = "%.${digits}f".format(Locale.US, this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlacesScreen(
    navController: NavController,
    placeIdToEdit: Int?,
    placesViewModel: PlacesViewModel = viewModel()
) {
    val context = LocalContext.current
    val isEditMode = placeIdToEdit != null

    val title by placesViewModel.draftTitle.collectAsState()
    val note by placesViewModel.draftNote.collectAsState()
    val imagePathString by placesViewModel.draftImagePath.collectAsState()

    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    var addressTextForDisplay by remember { mutableStateOf<String?>("No location set") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val updateAddressFromCoordinates = remember(context, scope) { // Key with context and scope
        { lat: Double, lon: Double ->
            scope.launch {
                addressTextForDisplay = "fetching address..."
                Log.d("AddPlacesScreen", "attempting reverse geocode for $lat, $lon")
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(lat, lon, 1)
                    }
                    addressTextForDisplay = if (addresses != null && addresses.isNotEmpty()) {
                        val foundAddress = addresses[0]
                        val sb = StringBuilder()
                        for (i in 0..foundAddress.maxAddressLineIndex) {
                            sb.append(foundAddress.getAddressLine(i))
                            if (i < foundAddress.maxAddressLineIndex) sb.append(", ")
                        }
                        sb.toString().trim().ifEmpty { "address details not found." }
                    } else {
                        "address not found for coordinates."
                    }
                } catch (e: IOException) {
                    Log.e("AddPlacesScreen", "Reverse geocoding IO/Network error", e)
                    addressTextForDisplay = "Failed to get address (Network/Service error)."
                } catch (e: IllegalArgumentException) {
                    Log.e("AddPlacesScreen", "Invalid coordinates for geocoder: $lat, $lon", e)
                    addressTextForDisplay = "Invalid coordinates provided."
                } catch (e: Exception) {
                    Log.e("AddPlacesScreen", "Unexpected error during reverse geocoding", e)
                    addressTextForDisplay = "Error fetching address."
                }
            }
        }
    }

    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentNavBackStackEntry) { // Key with currentNavBackStackEntry
        currentNavBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            if (savedStateHandle.contains("picked_latitude") && savedStateHandle.contains("picked_longitude")) {
                val lat = savedStateHandle.get<Double>("picked_latitude")
                val lon = savedStateHandle.get<Double>("picked_longitude")
                if (lat != null && lon != null) {
                    currentLatitude = lat
                    currentLongitude = lon
                    Log.d("AddPlacesScreen", "Received from MapPicker: Lat $lat, Lon $lon")
                    updateAddressFromCoordinates(lat, lon)
                    savedStateHandle.remove<Double>("picked_latitude")
                    savedStateHandle.remove<Double>("picked_longitude")
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentDeviceLocation(context, fusedLocationClient,
                onSuccess = { lat, lon ->
                    currentLatitude = lat
                    currentLongitude = lon
                    updateAddressFromCoordinates(lat, lon)
                    Toast.makeText(context, "Current location updated", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Log.e("AddPlacesScreen", "Error getting current location", it)
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_LONG).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { contentUri ->
            scope.launch(Dispatchers.IO) { // Perform file saving off the main thread
                val fileName = "happy_place_img_${System.currentTimeMillis()}.jpg"
                val savedPath = saveUriToFile(context, contentUri, fileName)
                withContext(Dispatchers.Main) { // Switch back to main thread for UI updates
                    if (savedPath != null) {
                        placesViewModel.updateDraftImagePath(savedPath)
                        Log.d("AddPlacesScreen", "Image saved to: $savedPath")
                    } else {
                        placesViewModel.updateDraftImagePath(null)
                        Log.e("AddPlacesScreen", "Failed to save image from URI: $contentUri")
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = placeIdToEdit) { // Key with placeIdToEdit only
        if (isEditMode && placeIdToEdit != null) {
            Log.d("AddPlacesScreen", "Edit mode: Loading place ID $placeIdToEdit")
            placesViewModel.getPlaceByIdFlow(placeIdToEdit).collect { place ->
                place?.let { loadedPlace ->
                    Log.d("AddPlacesScreen", "Editing Place: ${loadedPlace.title}")
                    placesViewModel.loadDraftData(loadedPlace) // ViewModel loads all draft data
                    currentLatitude = loadedPlace.latitude
                    currentLongitude = loadedPlace.longitude
                    addressTextForDisplay = if (loadedPlace.latitude != null && loadedPlace.longitude != null) {
                        if (!loadedPlace.address.isNullOrBlank()) {
                            loadedPlace.address
                        } else {
                            // Launch address fetching, will update addressTextForDisplay
                            updateAddressFromCoordinates(loadedPlace.latitude!!, loadedPlace.longitude!!)
                            "fetching address..." // Initial text while fetching
                        }
                    } else {
                        "no location set"
                    }
                }
            }
        } else {
            // In "Add New" mode, clear previous draft data if any
            // except for potentially picked location if user navigated away and came back
            // ViewModel's `clearDraft()` could be called here if needed,
            // or ensure loadDraftData(null) effectively clears it.
            // For now, assume ViewModel handles draft state appropriately.
            Log.d("AddPlacesScreen", "Add mode. Initializing or using existing draft.")
            // If there's a fresh entry into add mode (not from map picker), reset location
            if (currentNavBackStackEntry?.savedStateHandle?.contains("picked_latitude") != true) {
                currentLatitude = null
                currentLongitude = null
                addressTextForDisplay = "no location set"
                // Optionally clear other parts of the draft via ViewModel if needed
                // placesViewModel.clearDraftLocation() // Example
            }
        }
    }
    val scrollState = rememberScrollState() // Hoist scroll state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "edit your happy place" else "add a new happy place") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val currentTitle = title.trim()
                    val currentImagePath = imagePathString

                    if (currentTitle.isNotBlank() && currentImagePath != null) {
                        val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val finalNote = note.trim().ifBlank { null }
                        val finalAddress = addressTextForDisplay?.takeIf {
                            it != "no location set" && it.isNotBlank() && !it.startsWith("fetching") && !it.startsWith("Failed") && !it.startsWith("Error")
                        }?.trim()

                        val placeData = Place(
                            id = if (isEditMode) placeIdToEdit!! else 0, // ID must be non-null for edit
                            title = currentTitle,
                            imageUri = currentImagePath,
                            note = finalNote,
                            date = currentDateString,
                            latitude = currentLatitude,
                            longitude = currentLongitude,
                            address = finalAddress
                        )
                        scope.launch { // Perform DB operations off main thread
                            if (isEditMode) {
                                placesViewModel.updatePlace(placeData)
                            } else {
                                placesViewModel.insertPlace(placeData)
                            }
                            withContext(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        }
                    } else {
                        Toast.makeText(context, "title and image are required.", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(painterResource(id = R.drawable.ic_save), contentDescription = "save place")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding from Scaffold
                .pointerInput(Unit) { // For keyboard dismissal on tap outside
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(scrollState) // Use hoisted scrollState
                .padding(horizontal = 16.dp) // Padding for content within the scrollable area
                .padding(bottom = 80.dp), // Extra padding at the bottom for FAB visibility
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Spacer at the top to give some breathing room
            // Spacer(modifier = Modifier.height(10.dp)) // Use paddingValues from Scaffold instead

            // --- Image Section ---
            Text("photo*", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        imagePickerLauncher.launch("image/*")
                    },
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (imagePathString != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = File(imagePathString!!),
                                placeholder = painterResource(id = R.drawable.ic_no_places_placeholder), // Add a placeholder
                                error = painterResource(id = R.drawable.ic_no_places_placeholder) // Add an error placeholder
                            ),
                            contentDescription = "selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.AddPhotoAlternate,
                                contentDescription = "add photo icon",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "tap to select image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- Details Section ---
            OutlinedTextField(
                value = title,
                onValueChange = { placesViewModel.updateDraftTitle(it) },
                label = { Text("title*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = note,
                onValueChange = { placesViewModel.updateDraftNote(it) },
                label = { Text("notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                shape = MaterialTheme.shapes.medium
            )

            // --- Location Section ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "location",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    LocationInfoText("lat: ${currentLatitude?.format(4) ?: "N/A"}")
                    LocationInfoText("lon: ${currentLongitude?.format(4) ?: "N/A"}")
                    LocationInfoText("address: ${addressTextForDisplay ?: "tap buttons to set"}")

                    FilledTonalButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            locationPermissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "get current location icon")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("use my current location")
                    }

                    FilledTonalButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            val routeToMapPicker = Screen.MapPickerScreenRoute.createRoute(currentLatitude, currentLongitude)
                            navController.navigate(routeToMapPicker)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Map, contentDescription = "choose on map icon")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("choose location on map")
                    }
                }
            }
            // Spacer for FAB is handled by the Column's bottom padding now
            // Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun LocationInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium, // Using bodyMedium for consistency
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}

@SuppressLint("MissingPermission")
fun getCurrentDeviceLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onError: (Exception) -> Unit
) {
    val cancellationTokenSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            onSuccess(location.latitude, location.longitude)
        } else {
            Log.w("AddPlacesScreen", "FusedLocationClient returned null location.")
            onError(Exception("Unable to get current location.")) // Simpler error message
        }
    }.addOnFailureListener { exception ->
        Log.e("AddPlacesScreen", "FusedLocationClient failed to get location.", exception)
        onError(exception)
    }
}
