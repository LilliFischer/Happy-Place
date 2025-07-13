package com.example.happyplaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.happyplaces.data.Place
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController // <-- Import NavController

@Composable
fun HomeScreen(navController: NavController) { // <-- MODIFIED: Added navController parameter
    val places = remember { mutableStateListOf<Place>() } // This will later come from a ViewModel

    Box(modifier = Modifier.fillMaxSize()) {
        if (places.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Füge deinen ersten Happy Place hinzu")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(places) { place ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.placeholder), // Ersetze später durch URI
                                contentDescription = place.title, // Good practice to add content description
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                            Text(
                                text = place.title,
                                style = MaterialTheme.typography.titleMedium, // Consider using titleMedium for card titles
                                modifier = Modifier.padding(8.dp)
                            )
                            // You might want to display the note or location here too later
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate(Screen.AddPlacesScreen.route) // <-- MODIFIED: Navigate on click
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Neuen Happy Place hinzufügen") // More descriptive
        }
    }
}
