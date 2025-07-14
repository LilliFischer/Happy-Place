package com.example.happyplaces.viewmodel

import android.content.Context // For Context.MODE_PRIVATE
// import android.util.Log // Not explicitly used in the final version of HappyPlaceInfoDialog shown
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable // Not directly used by HappyPlaceInfoDialog's new detail section
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday // For Date Icon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn // For Address Icon
import androidx.compose.material.icons.filled.Place // For Location Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // For Icon type in DetailItemRow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.platform.LocalLifecycleOwner // Not explicitly used
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.happyplaces.R
import com.example.happyplaces.data.Place
import com.example.happyplaces.Screen
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.data.position
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Locale

// Helper extension function (either here or in a shared file)
private fun Double.format(digits: Int): String = "%.${digits}f".format(Locale.US, this)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesMapScreen(
    navController: NavController,
    placesViewModel: PlacesViewModel = viewModel()
) {
    val context = LocalContext.current
    val placesList by placesViewModel.allPlaces.observeAsState(initial = emptyList())
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var selectedPlaceForInfo by remember { mutableStateOf<Place?>(null) }
    var showNotesOnInfoPanel by remember { mutableStateOf(false) }
    var longPressedPlaceOnInfoPanel by remember { mutableStateOf<Place?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "com.example.happyplaces" // Use your app's package name or a unique ID
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("happy places map") }, // Capitalized for convention
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(9.5)
                        controller.setCenter(GeoPoint(50.9335, 6.9618)) // Default to Cologne
                        mapViewInstance = this
                    }
                },
                update = { mapView ->
                    mapViewInstance = mapView
                    mapView.overlays.clear()
                    val happyPinDrawable = ContextCompat.getDrawable(context, R.drawable.map_marker_happy_pin)

                    val markers = mutableListOf<org.osmdroid.views.overlay.Marker>()
                    val validPlaces = placesList.filter { it.latitude != null && it.longitude != null }

                    if (validPlaces.isNotEmpty()) {
                        validPlaces.forEach { place ->
                            val geoPoint = GeoPoint(place.latitude!!, place.longitude!!)
                            val placeMarker = org.osmdroid.views.overlay.Marker(mapView).apply {
                                position = geoPoint
                                title = place.title
                                relatedObject = place

                                if (happyPinDrawable != null) {
                                    icon = happyPinDrawable
                                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                } else {
                                    icon = AppCompatResources.getDrawable(context, R.drawable.ic_map_marker_default)
                                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                }

                                setOnMarkerClickListener { markerLister, _ ->
                                    selectedPlaceForInfo = markerLister.relatedObject as? Place
                                    showNotesOnInfoPanel = false
                                    longPressedPlaceOnInfoPanel = null
                                    mapView.controller.animateTo(markerLister.position)
                                    true
                                }
                            }
                            markers.add(placeMarker)
                        }
                        mapView.overlays.addAll(markers)
                    }
                    mapView.invalidate()
                }
            )
        }
    }

    selectedPlaceForInfo?.let { place ->
        HappyPlaceInfoDialog(
            place = place,
            showNotes = showNotesOnInfoPanel,
            isLongPressed = longPressedPlaceOnInfoPanel == place,
            onDismissRequest = {
                selectedPlaceForInfo = null
                longPressedPlaceOnInfoPanel = null
            },
            onImageClick = {
                showNotesOnInfoPanel = !showNotesOnInfoPanel
                longPressedPlaceOnInfoPanel = null
            },
            onImageLongClick = {
                longPressedPlaceOnInfoPanel = if (longPressedPlaceOnInfoPanel == place) null else place
            },
            onEditClick = {
                navController.navigate(Screen.AddPlacesScreen.createRoute(place.id))
                selectedPlaceForInfo = null // Dismiss dialog after navigation
            },
            onDeleteClick = {
                placesViewModel.requestDeleteConfirmation(place)
                selectedPlaceForInfo = null // Dismiss dialog
            }
        )
    }

    val showDeleteDialog by placesViewModel.showDeleteConfirmationDialog.collectAsState()
    if (showDeleteDialog) {
        placesViewModel.placeToDelete.collectAsState().value?.let { placeForDeletion ->
            AlertDialog(
                onDismissRequest = { placesViewModel.dismissDeleteConfirmationDialog() },
                title = { Text("Delete Place") },
                text = { Text("Are you sure you want to delete '${placeForDeletion.title}'?") },
                confirmButton = {
                    TextButton(onClick = { placesViewModel.confirmDeletePlace() }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { placesViewModel.dismissDeleteConfirmationDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// --- NEW HELPER COMPOSABLE FOR DETAIL ITEMS ---
@Composable
private fun DetailItemRow(
    icon: ImageVector,
    label: String,
    value: String?,
    isLastItem: Boolean = false
) {
    if (value.isNullOrBlank()) return // Don't show the row if there's no value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLastItem) 0.dp else 10.dp), // More space between items
        verticalAlignment = Alignment.Top // Align icon with the top of the label if value is multi-line
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label icon",
            modifier = Modifier
                .size(20.dp) // Adjusted size for better balance
                .padding(end = 10.dp), // Space between icon and text
            tint = MaterialTheme.colorScheme.onSurfaceVariant // Subtler icon color
        )
        Column { // Allows label and value to stack or be styled independently
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium, // Using a specific "label" typography
                fontWeight = FontWeight.SemiBold, // Or FontWeight.Bold
                color = MaterialTheme.colorScheme.onSurfaceVariant // Label color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium, // Value text style
                color = MaterialTheme.colorScheme.onSurface // Value text color
            )
        }
    }
}
// --- END OF NEW HELPER COMPOSABLE ---


@OptIn(ExperimentalMaterial3Api::class) // Keep if Card or other M3 components are used without explicit opt-in
@Composable
fun HappyPlaceInfoDialog(
    place: Place,
    showNotes: Boolean,
    isLongPressed: Boolean,
    onDismissRequest: () -> Unit,
    onImageClick: () -> Unit,
    onImageLongClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onImageClick() },
                                onLongPress = { onImageLongClick() }
                            )
                        }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(place.imageUri)),
                        contentDescription = place.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (showNotes || isLongPressed) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLongPressed) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(onClick = onEditClick) {
                                        Icon(Icons.Filled.Edit, "Edit", tint = Color.White)
                                    }
                                    IconButton(onClick = onDeleteClick) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = Color.White)
                                    }
                                }
                            } else {
                                Text(
                                    text = place.note ?: "No notes for this place.",
                                    color = Color.White,
                                    fontSize = 16.sp, // Or MaterialTheme.typography.bodyLarge.fontSize
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp)) // Increased spacer before details

                // --- REDESIGNED DETAILS SECTION using DetailItemRow ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailItemRow(
                        icon = Icons.Filled.CalendarToday,
                        label = "Date",
                        value = place.date
                    )
                    DetailItemRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Address",
                        value = place.address
                    )
                    DetailItemRow(
                        icon = Icons.Filled.Place,
                        label = "Location",
                        value = if (place.latitude != null && place.longitude != null) {
                            "${place.latitude.format(4)}, ${place.longitude.format(4)}"
                        } else null,
                        isLastItem = true
                    )
                }
                // --- END OF REDESIGNED DETAILS SECTION ---

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close") // Consider consistent casing, e.g., "Close"
                }
            }
        }
    }
}

