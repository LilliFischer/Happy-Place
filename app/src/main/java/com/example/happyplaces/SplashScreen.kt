
package com.example.happyplaces
/*
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState // <-- Import for LiveData
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
//import androidx.lifecycle.viewmodel.compose.viewModel // <-- Import for viewModel
//import com.example.happyplaces.viewmodel.PlacesViewModel // <-- Import your ViewModel
import kotlinx.coroutines.delay // Use Kotlin's delay


@Composable
fun SplashScreen(
    navController: NavController,
    //placesViewModel: PlacesViewModel = viewModel() // Inject ViewModel
) {
    //val placesList by placesViewModel.allPlaces.observeAsState() // Observe LiveData
    LaunchedEffect(key1 = true) {
        delay(2000) // Your splash delay

        // Always navigate to PlacesGridScreen
        navController.navigate(Screen.PlacesGridScreen.route) {
            popUpTo(Screen.SplashScreen.route) { inclusive = true }
            launchSingleTop = true // Good practice
        }
    }

    // --- Your existing UI for SplashScreen ---
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Happy Places", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
*/
