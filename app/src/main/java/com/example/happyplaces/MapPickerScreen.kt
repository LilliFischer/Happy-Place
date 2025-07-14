package com.example.happyplaces

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // For back button
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.MapEventsOverlay // Try this one
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.util.Log // For logging
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController,
    initialLat: Double?,
    initialLon: Double?
) {
    val context = LocalContext.current
    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    val mapView = remember { MapView(context) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) } // Keep track of the current marker

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Log.d("MapPickerScreen", "OSMDroid configuration loading.")
        // Get SharedPreferences directly from context or use androidx.preference if you have it as a dependency
        val prefs = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, prefs)

        Log.d("MapPickerScreen", "OSMDroid configuration loaded.")
    }

    // Set initial selected point if arguments are provided (e.g., when editing)
    LaunchedEffect(initialLat, initialLon) {
        if (initialLat != null && initialLon != null) {
            val initialPoint = GeoPoint(initialLat, initialLon)
            selectedGeoPoint = initialPoint
            Log.d("MapPickerScreen", "Initial location set from arguments: $initialPoint")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("select location on map") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // CHANGE HERE
                            contentDescription = "back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (selectedGeoPoint != null) {
                FloatingActionButton(onClick = {
                    selectedGeoPoint?.let { gp ->
                        Log.d("MapPickerScreen", "Confirming location: Lat ${gp.latitude}, Lon ${gp.longitude}")
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("picked_latitude", gp.latitude)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("picked_longitude", gp.longitude)
                        navController.popBackStack()
                    }
                }) {
                    Icon(Icons.Filled.Check, contentDescription = "confirm location")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) { // Use Box for potential overlays later
            AndroidView(
                factory = { ctx ->
                    Log.d("MapPickerScreen", "AndroidView factory called.")
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        val startZoom = if (selectedGeoPoint != null) 15.0 else 6.0 // Zoom closer if editing
                        val startPoint = selectedGeoPoint ?: GeoPoint(51.5074, 0.1278) // Default (London) or initial

                        Log.d("MapPickerScreen", "Initial map center: $startPoint, Zoom: $startZoom")
                        controller.setZoom(startZoom)
                        controller.setCenter(startPoint)

                        // Add initial marker if a location is already selected (e.g., from args)
                        selectedGeoPoint?.let { initialPoint ->
                            val marker = Marker(this)
                            marker.position = initialPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            this.overlays.add(marker)
                            currentMarker = marker
                            Log.d("MapPickerScreen", "Initial marker added at $initialPoint")
                        }

                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let { tappedPoint ->
                                    Log.d("MapPickerScreen", "Map tapped at: $tappedPoint")
                                    selectedGeoPoint = tappedPoint

                                    // Remove previous marker if it exists
                                    currentMarker?.let { overlays.remove(it) }

                                    // Add new marker
                                    val newMarker = Marker(this@apply) // Use this@apply to refer to MapView
                                    newMarker.position = tappedPoint
                                    newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    overlays.add(newMarker)
                                    currentMarker = newMarker // Update current marker reference
                                    invalidate() // Redraw map
                                    Log.d("MapPickerScreen", "New marker added at $tappedPoint")
                                }
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                return false // Not used for now
                            }
                        }
                        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                        // Add map events overlay at the beginning so it's under other overlays like markers
                        if (!this.overlays.contains(mapEventsOverlay)) {
                            this.overlays.add(0, mapEventsOverlay)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // This block is called when the composable recomposes.
                    // We can use it to update the map if needed, for example,
                    // if the initialLat/Lon changed due to recomposition from outside.
                    // For now, let's ensure the center and zoom are set if initial point is available.
                    Log.d("MapPickerScreen", "AndroidView update called. Selected: $selectedGeoPoint")
                    selectedGeoPoint?.let {
                        if (view.mapCenter != it || view.zoomLevelDouble != 15.0) { // Avoid unnecessary updates
                            // view.controller.animateTo(it, 15.0, null) // Animate for smoother transition
                        }
                    }
                    view.invalidate() // Ensure map redraws with current overlays
                }
            )
        }
    }
}
