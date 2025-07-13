package com.example.happyplaces

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Assuming these are your existing screen composables
// import com.example.happyplaces.SplashScreen
// import com.example.happyplaces.HomeScreen
// import com.example.happyplaces.AddPlacesScreen

// Import your new screen
import com.example.happyplaces.viewmodel.PlacesGridScreen // Adjust this import path if needed


// Sealed class for defining screen routes
sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object HomeScreen : Screen("home_screen") // Keep if used, or decide if it becomes PlacesGridScreen
    object AddPlacesScreen : Screen("add_place_screen")
    object PlacesGridScreen : Screen("places_grid_screen") // New screen route
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        // Decide your start destination after splash. For example, PlacesGridScreen:
        startDestination = Screen.SplashScreen.route
    ) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController = navController) // SplashScreen should navigate to PlacesGridScreen or HomeScreen
        }

        // Option A: If HomeScreen is distinct and you navigate to PlacesGridScreen from it or SplashScreen
        composable(Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.PlacesGridScreen.route) { // <-- ADD THIS COMPOSABLE DESTINATION
            PlacesGridScreen(
                navController = navController
            )
        }

        composable(Screen.AddPlacesScreen.route) {
            AddPlacesScreen(navController = navController)
        }

        // TODO: Ensure SplashScreen navigates to the correct screen after it's done.
        // For example, in SplashScreen.kt, after delay:
        // navController.navigate(Screen.PlacesGridScreen.route) {
        //     popUpTo(Screen.SplashScreen.route) { inclusive = true }
        // }
    }
}

