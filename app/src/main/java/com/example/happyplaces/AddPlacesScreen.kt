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
import androidx.compose.runtime.saveable.rememberSaveable // Added for rememberSaveable
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

    // New state flag to signal return from map picker
    var justReturnedFromMapPicker by rememberSaveable { mutableStateOf(false) }

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
    LaunchedEffect(currentNavBackStackEntry) {
        Log.d("AddPlacesScreen_LE_NavBack", "Enter NavBackStackEntry effect. Handle: ${currentNavBackStackEntry?.savedStateHandle}")

        currentNavBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            Log.d("AddPlacesScreen_LE_NavBack", "SavedStateHandle exists.")
            if (savedStateHandle.contains("picked_latitude") && savedStateHandle.contains("picked_longitude")) {
                Log.d("AddPlacesScreen_LE_NavBack", "SavedStateHandle CONTAINS lat/lon keys.")
                val lat = savedStateHandle.get<Double>("picked_latitude")
                val lon = savedStateHandle.get<Double>("picked_longitude")

                Log.d("AddPlacesScreen_LE_NavBack", "Retrieved from SavedStateHandle: Lat $lat, Lon $lon")

                if (lat != null && lon != null) {
                    currentLatitude = lat
                    currentLongitude = lon
                    justReturnedFromMapPicker = true // Signal that we've processed map data
                    Log.d("AddPlacesScreen_LE_NavBack", "SUCCESS: currentLatitude SET TO $currentLatitude, currentLongitude SET TO $currentLongitude. justReturnedFromMapPicker = true")
                    updateAddressFromCoordinates(lat, lon)
                    // It's important to remove them so this doesn't re-trigger on config changes
                    // if the NavBackStackEntry is somehow reused by the navigation library.
                    savedStateHandle.remove<Double>("picked_latitude")
                    savedStateHandle.remove<Double>("picked_longitude")
                    Log.d("AddPlacesScreen_LE_NavBack", "Removed lat/lon from SavedStateHandle.")
                } else {
                    Log.d("AddPlacesScreen_LE_NavBack", "Lat/Lon from SavedStateHandle were NULL.")
                }
            } else {
                Log.d("AddPlacesScreen_LE_NavBack", "SavedStateHandle DOES NOT contain lat/lon keys.")
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
            scope.launch(Dispatchers.IO) {
                val fileName = "happy_place_img_${System.currentTimeMillis()}.jpg"
                val savedPath = saveUriToFile(context, contentUri, fileName)
                withContext(Dispatchers.Main) {
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

    // Key this LaunchedEffect with placeIdToEdit AND justReturnedFromMapPicker
    LaunchedEffect(key1 = placeIdToEdit, key2 = justReturnedFromMapPicker) {
        if (isEditMode && placeIdToEdit != null) {
            Log.d("AddPlacesScreen_LE_placeId", "Edit mode: Loading place ID $placeIdToEdit. justReturnedFromMapPicker: $justReturnedFromMapPicker")
            // In edit mode, if we just returned from map picker, the NavBackStack LE
            // would have updated currentLatitude/Longitude and triggered address update.
            // We still need to load other draft data.
            // If the place ID changes, or it's the initial load, collect the place.
            // This 'collect' will re-run if placeIdToEdit changes.
            // The `loadDraftData` should ideally not overwrite newly picked coords if they exist.
            placesViewModel.getPlaceByIdFlow(placeIdToEdit).collect { place ->
                place?.let { loadedPlace ->
                    Log.d("AddPlacesScreen", "Editing Place: ${loadedPlace.title}")
                    placesViewModel.loadDraftData(loadedPlace) // ViewModel loads draft text fields

                    if (justReturnedFromMapPicker) {
                        // Coordinates already updated by NavBack LE, address fetching started.
                        // ViewModel's draft image/title/note are loaded from DB by loadDraftData.
                        // If user changed title/note *then* picked location, loadDraftData might overwrite.
                        // This implies `loadDraftData` should be smart or called strategically.
                        // For now, assume currentLatitude/Longitude are the source of truth for location after map picking.
                        Log.d("AddPlacesScreen_LE_placeId", "Edit Mode: Returned from map picker. Coordinates updated by NavBack. Address being fetched.")
                    } else {
                        // Not returning from map picker (e.g., initial load of edit screen)
                        // Set coordinates from the loaded place
                        currentLatitude = loadedPlace.latitude
                        currentLongitude = loadedPlace.longitude
                        addressTextForDisplay = if (loadedPlace.latitude != null && loadedPlace.longitude != null) {
                            if (!loadedPlace.address.isNullOrBlank()) {
                                loadedPlace.address
                            } else {
                                updateAddressFromCoordinates(loadedPlace.latitude!!, loadedPlace.longitude!!)
                                "fetching address..."
                            }
                        } else {
                            "no location set"
                        }
                    }
                }
            }
            if (justReturnedFromMapPicker) { // Consume the flag after processing in edit mode
                justReturnedFromMapPicker = false
            }
        } else { // Add New Mode
            Log.d("AddPlacesScreen_LE_placeId", "Enter ADD MODE block. justReturnedFromMapPicker: $justReturnedFromMapPicker")

            if (justReturnedFromMapPicker) {
                // If we just returned from map picker, the NavBackStack LE handled coordinates
                // and started address fetching. We don't want to reset addressTextForDisplay.
                Log.d("AddPlacesScreen_LE_placeId", "Add Mode: IS returning from map picker. Coordinates updated by NavBack. Address being fetched.")
                // Consume the flag
                justReturnedFromMapPicker = false
            } else {
                // This is the initial entry to "add mode" OR a recomposition not related to map return.
                Log.d("AddPlacesScreen_LE_placeId", "Add Mode: NOT just returned from map picker. CurrentLat BEFORE reset check: $currentLatitude")
                // Only reset if no location has been set at all (e.g. initial screen load)
                if (currentLatitude == null && currentLongitude == null) {
                    addressTextForDisplay = "no location set"
                    Log.d("AddPlacesScreen_LE_placeId", "Add Mode: Resetting addressTextForDisplay to 'no location set' as no coordinates exist. CurrentLat: $currentLatitude")
                } else {
                    Log.d("AddPlacesScreen_LE_placeId", "Add Mode: NOT resetting addressTextForDisplay as current coordinates exist: $currentLatitude, $currentLongitude")
                }
            }
        }
    }
    val scrollState = rememberScrollState()

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
                        Log.d("AddPlacesScreen_Save", "PREPARING TO SAVE Place. currentLatitude: $currentLatitude, currentLongitude: $currentLongitude, addressTextForDisplay: $addressTextForDisplay")

                        val placeData = Place(
                            id = if (isEditMode) placeIdToEdit!! else 0,
                            title = currentTitle,
                            imageUri = currentImagePath,
                            note = finalNote,
                            date = currentDateString,
                            latitude = currentLatitude,
                            longitude = currentLongitude,
                            address = finalAddress
                        )
                        Log.d("AddPlacesScreen_Save", "Place OBJECT CREATED. Lat: ${placeData.latitude}, Lon: ${placeData.longitude}")

                        scope.launch {
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
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
                                placeholder = painterResource(id = R.drawable.ic_no_places_placeholder),
                                error = painterResource(id = R.drawable.ic_no_places_placeholder)
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
        }
    }
}

@Composable
private fun LocationInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
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
            onError(Exception("Unable to get current location."))
        }
    }.addOnFailureListener { exception ->
        Log.e("AddPlacesScreen", "FusedLocationClient failed to get location.", exception)
        onError(exception)
    }
}

