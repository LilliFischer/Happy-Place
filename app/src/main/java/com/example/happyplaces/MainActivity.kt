package com.example.happyplaces

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Import for by viewModels()
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope // Import for lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.happyplaces.ui.theme.HappyPlacesTheme
import com.example.happyplaces.AppNavigation
import com.example.happyplaces.viewmodel.PlacesViewModel // Make sure this import is correct
import kotlinx.coroutines.flow.first // Import for collecting the first value
import kotlinx.coroutines.launch    // Import for launch

class MainActivity : ComponentActivity() {

    // Get a reference to your PlacesViewModel
    private val placesViewModel: PlacesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashOnScreen = true // Condition to keep splash screen

        // Set the condition to keep the splash screen visible.
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // Launch a coroutine to wait for the data to load.
        lifecycleScope.launch {
            // Wait until isInitialDataLoaded becomes true
            // Ensure isInitialDataLoaded exists in your PlacesViewModel and works as described previously
            placesViewModel.isInitialDataLoaded.first { it /* it == true */ }
            // Once data is loaded, allow splash to dismiss
            keepSplashOnScreen = false
        }

        enableEdgeToEdge()

        setContent {
            HappyPlacesTheme {
                val navController = rememberNavController() // This is fine if AppNavigation needs it explicitly passed
                AppNavigation(navController)
                // Or if AppNavigation uses rememberNavController() internally, you might not need to pass it
                // AppNavigation()
            }
        }
    }
}


