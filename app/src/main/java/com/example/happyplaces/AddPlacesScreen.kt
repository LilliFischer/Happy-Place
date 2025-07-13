package com.example.happyplaces

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.happyplaces.R // Assuming you'll add a placeholder image
import com.example.happyplaces.data.Place // Your Place data class
import com.example.happyplaces.viewmodel.PlacesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlacesScreen(
    navController: NavController,
    placesViewModel: PlacesViewModel = viewModel()
) {
    Log.d("AddPlacesScreen", "Using ViewModel instance: ${placesViewModel.getInstanceId()}")

    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var addressText by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Activity result launcher for picking an image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add New Happy Place") })
        },
        // Inside AddPlacesScreen composable, in the Scaffold's floatingActionButton:

        floatingActionButton = {
            FloatingActionButton(onClick = {
                val currentTitle = title.trim()
                val currentNote = note.trim()
                val currentImageUriString = imageUri?.toString()
                val currentAddress = addressText.trim() // Use the renamed state variable

                // Basic validation: Title and Image are essential. Address is also good to have.
                if (currentTitle.isNotBlank() && currentImageUriString != null) { // Address can be optional based on your needs
                    val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val currentLatitude: Double? = null // Still null for now
                    val currentLongitude: Double? = null // Still null for now

                    val newPlace = Place(
                        title = currentTitle,
                        imageUri = currentImageUriString,
                        note = currentNote.ifBlank { null },
                        date = currentDate,
                        latitude = currentLatitude,
                        longitude = currentLongitude,
                        address = currentAddress.ifBlank { null } // Save currentAddress here. Save null if blank.
                        // NO 'location = ...' field here anymore
                    )
                    placesViewModel.insertPlace(newPlace)
                    navController.popBackStack()
                } else {
                    // TODO: Show a more user-friendly error (e.g., Snackbar)
                    Log.e("AddPlacesScreen", "Validation failed: Title or Image URI is blank.")
                    // You might want to inform the user via a Toast or Snackbar
                }
            }) {
                Icon(painterResource(id = R.drawable.ic_save), contentDescription = "Save Place")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Picker
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(1.dp, Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Tap to select image") // Or use a placeholder icon
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title*") }, // Added * to indicate required
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Changed label and state variable for clarity
            OutlinedTextField(
                value = addressText, // Use the renamed state variable
                onValueChange = { addressText = it },
                label = { Text("Address") }, // Clearer label
                modifier = Modifier.fillMaxWidth()
            )


            // TODO: Add a more sophisticated location picker (e.g., Map integration or GPS)
        }
    }
}
