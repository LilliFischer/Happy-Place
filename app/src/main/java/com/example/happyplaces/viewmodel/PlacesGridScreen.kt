package com.example.happyplaces.viewmodel

import android.util.Log
import androidx.compose.foundation.Image // Often needed for painterResource
import androidx.compose.foundation.background // For Text background
import androidx.compose.foundation.clickable // For Card click
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // New, for layering image and text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio // New, for square card
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // New, for Text color and background
import androidx.compose.ui.layout.ContentScale // New, for AsyncImage scaling
import androidx.compose.ui.res.painterResource // New, for placeholder/error drawables
import androidx.compose.ui.text.style.TextAlign // New, for Text alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController // Already there, but good to confirm
import coil.compose.AsyncImage // New, for image loading
import com.example.happyplaces.R // New, for accessing R.drawable.*
import com.example.happyplaces.data.Place
import com.example.happyplaces.ui.theme.HappyPlacesTheme
import com.example.happyplaces.viewmodel.PlacesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesGridScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    placesViewModel: PlacesViewModel = viewModel()
) {
    Log.d("PlacesGridScreen", "Using ViewModel instance: ${placesViewModel.getInstanceId()}")

    val placesList by placesViewModel.allPlaces.observeAsState(initial = emptyList<Place>())
    Log.d("PlacesGridScreen", "Observed places list: ${placesList.map { it.title }}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Happy Places") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (placesList.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            PlacesLazyGrid(
                places = placesList,
                modifier = Modifier.padding(innerPadding),
                navController = navController // Pass the navController here
            )
        }
    }
}

@Composable
fun PlacesLazyGrid(
    places: List<Place>,
    modifier: Modifier = Modifier,
    navController: NavHostController // Add NavController as a parameter
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp), // Consider reducing padding slightly for more image space
        verticalArrangement = Arrangement.spacedBy(8.dp), // Consider reducing spacing
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Consider reducing spacing
    ) {
        items(places, key = { place -> place.id }) { place ->
            PlaceGridItem(
                place = place,
                onItemClick = { selectedPlace ->
                    // This is where the click is handled
                    Log.d("PlacesLazyGrid", "Clicked on item: ${selectedPlace.title}, ID: ${selectedPlace.id}")
                    // Later, this will be:
                    // navController.navigate(Screen.PlaceDetailScreen.route.replace("{placeId}", selectedPlace.id.toString()))
                }
            )
        }
    }
}

@Composable
fun PlaceGridItem(
    place: Place,
    modifier: Modifier = Modifier,
    onItemClick: (Place) -> Unit // New: Callback for when item is clicked
) {
    Card(
        modifier = modifier
            .fillMaxWidth() // Each item can take full width in its grid cell
            .aspectRatio(1f) // Makes the Card square. Adjust if you prefer other ratios (e.g., 3/4f for portrait)
            .clickable { onItemClick(place) }, // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart // Aligns the title to the bottom-left/start
        ) {
            // Image takes up the whole Box
            AsyncImage(
                model = place.imageUri,
                contentDescription = place.title, // Good for accessibility
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop, // Scales the image to fill the bounds, cropping if necessary
                // Optional: Add placeholder and error drawables
                // Make sure you have 'ic_placeholder_image.xml' and 'ic_error_image.xml' (or similar)
                // in your res/drawable folder. You can create simple vector drawables.
                placeholder = painterResource(id = R.drawable.ic_launcher_background), // Example placeholder
                error = painterResource(id = R.drawable.ic_launcher_background) // Example error image
            )

            // Title overlaid on the bottom of the image
            Text(
                text = place.title,
                style = MaterialTheme.typography.titleSmall, // Using a smaller style for overlay
                color = Color.White, // Ensure good contrast, adjust as needed
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background for readability
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.BottomCenter) // Align text to the bottom center of the Box
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No happy places yet!", style = MaterialTheme.typography.headlineSmall)
        Text("Add some to see them here.", style = MaterialTheme.typography.bodyLarge)
    }
}


@Preview(showBackground = true)
@Composable
fun PlacesGridScreenPreview() {
    HappyPlacesTheme {
        // For previews, NavController is often not fully functional or needed for UI checks.
        // You can pass a remembered NavController or a fake/mock one if necessary,
        // but for simple visual previews, often just creating one is enough.
        // If your preview doesn't *interact* with navigation, this is fine.
        val navController = rememberNavController() // Import androidx.navigation.compose.rememberNavController
        PlacesGridScreen(navController = navController) // Provide the navController
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceGridItemPreview() {
    HappyPlacesTheme {
        PlaceGridItem(
            place = Place(
                id = 1,
                title = "Preview Lake View",
                imageUri = "", // For preview, an empty string will likely show the error drawable from AsyncImage
                note = "Peaceful morning by the lake.",
                // location = "Lakefront Park", // This field was removed/consolidated into 'address'
                // If your Place data class still has it, you'll get an error here or on 'address'.
                // Ensure your Place data class ONLY has 'address: String?' for the textual location.
                date = "2024-03-15",
                latitude = 47.6062,
                longitude = -122.3321,
                address = "123 Lake Rd, Seattle, WA" // This should be the primary address field
            ),
            onItemClick = {} // Add an empty lambda for the click handler in preview
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    HappyPlacesTheme {
        EmptyState()
    }
}