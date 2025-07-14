package com.example.happyplaces.viewmodel // This should ideally be in a 'ui' or 'screens' package

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
// Import for SnapFlingBehavior (Material 3)
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.happyplaces.ConfirmationDialog
import com.example.happyplaces.R
import com.example.happyplaces.Screen
import com.example.happyplaces.data.Place
import com.example.happyplaces.ui.theme.HappyPlacesTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesGridScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    placesViewModel: PlacesViewModel = viewModel()
) {
    val placesList by placesViewModel.allPlaces.observeAsState(initial = emptyList())
    val expandedPlaceId by placesViewModel.expandedPlaceId.collectAsState()
    val longPressedPlaceId by placesViewModel.longPressedPlaceId.collectAsState()
    val showDeleteDialog by placesViewModel.showDeleteConfirmationDialog.collectAsState()
    val placeTargetedForDelete by placesViewModel.placeToDelete.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable( // Global click to clear expanded/long-pressed states
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No visual ripple for this global click
                onClick = {
                    placesViewModel.clearExpandedPlace()
                    placesViewModel.clearLongPressedPlace()
                }
            )
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp), // Let content handle safe areas
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding) // Apply padding from Scaffold (usually for system bars)
                    .fillMaxSize()
                    .safeDrawingPadding() // Ensure content is within safe drawing areas
            ) {
                if (placesList.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PlacesStaggeredGrid(
                        places = placesList,
                        modifier = Modifier.fillMaxSize(),
                        // navController = navController, // Not directly passed if not used by grid item itself
                        expandedPlaceId = expandedPlaceId,
                        onPlaceClicked = placesViewModel::onPlaceClicked,
                        longPressedPlaceId = longPressedPlaceId,
                        onPlaceLongPressed = placesViewModel::onPlaceLongPressed,
                        onEditPlace = { placeId ->
                            placesViewModel.clearLongPressedPlace() // Clear state before navigating
                            navController.navigate(Screen.AddPlacesScreen.createRoute(placeId))
                        },
                        onRequestDelete = { place ->
                            placesViewModel.clearLongPressedPlace() // Clear state before showing dialog
                            placesViewModel.requestDeleteConfirmation(place)
                        }
                    )
                }
            }
        }

        // FAB Row - Placed in the Box to overlay Scaffold content, respects safeDrawingPadding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // Align to bottom of the outer Box
                .safeDrawingPadding() // FABs also respect safe areas
                .padding(horizontal = 16.dp, vertical = 16.dp), // Visual padding from safe edges
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.PlacesMapScreen.route)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = "Open Map"
                )
            }
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddPlacesScreen.createRoute()) // No ID for new place
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Happy Place")
            }
        }

        // Confirmation Dialog - Centered on the screen
        if (showDeleteDialog && placeTargetedForDelete != null) {
            Box( // Overlay the whole screen to center the dialog
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ConfirmationDialog(
                    // showDialog prop might be redundant if visibility is controlled by this `if`
                    // but can be kept if ConfirmationDialog has internal state based on it.
                    showDialog = true,
                    title = "Delete Place",
                    message = "Are you sure you want to delete \"${placeTargetedForDelete?.title ?: ""}\"? This action cannot be undone.",
                    confirmButtonText = "Delete",
                    onConfirm = { placesViewModel.confirmDeletePlace() },
                    onDismiss = { placesViewModel.dismissDeleteConfirmationDialog() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // For rememberSnapFlingBehavior and combinedClickable
@Composable
fun PlacesStaggeredGrid(
    places: List<Place>,
    modifier: Modifier = Modifier,
    expandedPlaceId: Int?,
    onPlaceClicked: (Int) -> Unit,
    longPressedPlaceId: Int?,
    onPlaceLongPressed: (Int) -> Unit,
    onRequestDelete: (Place) -> Unit,
    onEditPlace: (placeId: Int) -> Unit
) {
    val gridState = rememberLazyStaggeredGridState() // Remember the grid state for fling behavior

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        state = gridState, // Pass the state to the grid
        // flingBehavior = rememberSnapFlingBehavior(lazyListState = gridState), // REMOVE THIS LINE
        contentPadding = PaddingValues(8.dp), // Padding around the content of the grid
        verticalItemSpacing = 8.dp, // Spacing between items vertically
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items horizontally
    ) {
        items(places, key = { place -> place.id }) { place ->
            PlaceGridItem(
                place = place,
                isExpanded = (place.id == expandedPlaceId),
                onItemClick = { onPlaceClicked(place.id) },
                isLongPressed = (place.id == longPressedPlaceId),
                onRequestDeleteItem = { onRequestDelete(place) },
                onItemLongClick = { onPlaceLongPressed(place.id) },
                onEditItem = { onEditPlace(place.id) }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class) // For combinedClickable
@Composable
fun PlaceGridItem(
    place: Place,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit,
    isLongPressed: Boolean,
    onItemLongClick: () -> Unit,
    onRequestDeleteItem: () -> Unit,
    onEditItem: () -> Unit
) {
    // Log.d("PlaceGridItem", "Item: ${place.title}, Expanded: $isExpanded, LongPressed: $isLongPressed")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isLongPressed) {
                        // The global click on PlacesGridScreen's Box will handle clearing long press.
                        // No specific action here, or call a clear function if needed.
                    } else {
                        onItemClick()
                    }
                },
                onLongClick = onItemLongClick
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight() // Essential for staggered grid items to size correctly
        ) {
            AsyncImage(
                model = place.imageUri, // Ensure this is a valid path or URI Coil can handle
                contentDescription = place.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium) // Clip the image to the card's shape
                    .then( // Apply blur conditionally
                        if (isExpanded && !isLongPressed) Modifier.blur(radius = 6.dp)
                        else Modifier
                    ),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_no_places_placeholder), // Use your actual placeholder
                error = painterResource(id = R.drawable.ic_no_places_placeholder) // Use your actual error drawable
            )

            // Overlay for Title and Notes (gradient scrim at the bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(top = 16.dp) // Space for content above the text inside the gradient
                    .padding(horizontal = 12.dp) // Horizontal padding for text
                    .padding(bottom = 8.dp) // Bottom padding for text
            ) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White, // Ensure text is visible on scrim
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                    // .padding(bottom = if (isExpanded && !isLongPressed) 4.dp else 0.dp) // Spacing before notes
                )
                if (isExpanded && !isLongPressed) {
                    Spacer(modifier = Modifier.height(4.dp)) // Small space between title and note
                    Text(
                        text = place.note ?: "No notes for this place.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f), // Slightly less prominent than title
                        textAlign = TextAlign.Start,
                        maxLines = 3, // Limit lines for notes in grid view
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Overlay for Action Icons (when long-pressed)
            if (isLongPressed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)) // Darken the item
                    // Optional: Make the scrim itself clickable to dismiss the long-press state
                    // .clickable(
                    //     interactionSource = remember { MutableInteractionSource() },
                    //     indication = null,
                    //     onClick = { /* ViewModel.clearLongPressedPlace() or similar */ }
                    // )
                ) {
                    // Edit Button
                    IconButton(
                        onClick = {
                            Log.d("PlaceGridItem", "Edit icon clicked for ${place.title}")
                            onEditItem()
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart) // Position at top-start
                            .padding(4.dp) // Reduced padding to keep icons closer to corners
                            .size(40.dp) // Explicit size for touch target
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Place",
                            tint = Color.White // Ensure icons are visible
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = {
                            Log.d("PlaceGridItem", "Delete icon clicked for ${place.title}")
                            onRequestDeleteItem()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd) // Position at top-end
                            .padding(4.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Place",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(), // Fill available space
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_places_placeholder), // Use your actual empty state icon
            contentDescription = "No places",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Subdued tint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "no happy places yet.",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "tap the '+' button to add one!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}


// --- Previews ---
// Ensure your previews are updated if you change parameters or structure significantly.
// The provided previews seem okay but double-check them against these changes.

@Preview(showBackground = true, name = "Grid Screen With Items")
@Composable
fun PlacesGridScreenPreview_WithItems_Default() {
    HappyPlacesTheme {
        val navController = rememberNavController()
        // Mock ViewModel for preview
        val mockViewModel: PlacesViewModel = viewModel()
        // Manually populate some data for the preview
        LaunchedEffect(Unit) {
            val samplePlaces = listOf(
                Place(1, "Mountain View", "uri1", "Beautiful hike!", "2023-10-26", 0.0, 0.0, "Address 1"),
                Place(2, "Beach Sunset", "uri2", "So peaceful and warm.", "2023-10-27", 0.0, 0.0, "Address 2"),
                Place(3, "City Lights", "uri3", null, "2023-10-28", 0.0, 0.0, "Address 3")
            )
            // This is a simplified way for preview; in real app, ViewModel handles this.
            // Directly updating LiveData for preview needs care or a mock ViewModel setup.
        }

        PlacesGridScreen(navController = navController, placesViewModel = mockViewModel)
    }
}


@Preview(showBackground = true, name = "Grid Screen - Item 1 LongPressed")
@Composable
fun PlacesGridScreenPreview_WithItems_Item1LongPressed() {
    HappyPlacesTheme {
        // This preview is harder to set up perfectly without a more complex mock ViewModel
        // or directly calling the PlacesStaggeredGrid with a modified longPressedPlaceId.
        // For simplicity, we'll show the grid, but interactive state is tricky in @Preview.
        val navController = rememberNavController()
        val samplePlaces = listOf(
            Place(1, "Mountain View (LongPressed)", "uri1", "Note for P1", "Date 1", 0.0, 0.0, "Address 1"),
            Place(2, "Beach Sunset", "uri2", "Note for P2", "Date 2", 0.0, 0.0, "Address 2")
        )
        // Simulate long press on item 1 by passing its ID
        PlacesStaggeredGrid(
            places = samplePlaces,
            expandedPlaceId = null,
            onPlaceClicked = {},
            longPressedPlaceId = 1, // Simulate item 1 is long-pressed
            onPlaceLongPressed = {},
            onRequestDelete = {},
            onEditPlace = {}
        )
    }
}

@Preview(showBackground = true, name = "Grid Screen - Empty State")
@Composable
fun PlacesGridScreenPreview_Empty() {
    HappyPlacesTheme {
        val navController = rememberNavController()
        val mockViewModel: PlacesViewModel = viewModel() // Use a mock or real ViewModel
        // Ensure ViewModel's placesList is empty for this preview
        PlacesGridScreen(navController = navController, placesViewModel = mockViewModel)
    }
}

@Preview(showBackground = true, name = "Grid Item - Not Expanded")
@Composable
fun PlaceGridItemPreview_NotExpanded() {
    HappyPlacesTheme {
        PlaceGridItem(
            place = Place(id = 1, title = "Preview Lake View", imageUri = "", note = "Peaceful morning.", date = "2024-03-15", 0.0,0.0,"Addr"),
            isExpanded = false, onItemClick = {}, isLongPressed = false, onItemLongClick = {}, onRequestDeleteItem = {}, onEditItem = {}
        )
    }
}

@Preview(showBackground = true, name = "Grid Item - Expanded")
@Composable
fun PlaceGridItemPreview_Expanded() {
    HappyPlacesTheme {
        PlaceGridItem(
            place = Place(id = 1, title = "Preview Lake (Expanded)", imageUri = "", note = "This is the note that should be visible. It might be a bit long to see how it wraps.", date = "2024-03-15",0.0,0.0,"Addr"),
            isExpanded = true, onItemClick = {}, isLongPressed = false, onItemLongClick = {}, onRequestDeleteItem = {}, onEditItem = {}
        )
    }
}

@Preview(showBackground = true, name = "Grid Item - Long Pressed")
@Composable
fun PlaceGridItemPreview_LongPressed() {
    HappyPlacesTheme {
        PlaceGridItem(
            place = Place(id = 1, title = "Item with Actions", imageUri = "", note = "Note should be hidden.", date = "2024-03-15",0.0,0.0,"Addr"),
            isExpanded = false, // Not expanded when long-pressed usually
            onItemClick = {},
            isLongPressed = true,
            onItemLongClick = {},
            onRequestDeleteItem = {},
            onEditItem = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State Component")
@Composable
fun EmptyStatePreview() {
    HappyPlacesTheme {
        EmptyState(modifier = Modifier.fillMaxSize())
    }
}
