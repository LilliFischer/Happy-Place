package com.example.happyplaces

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.happyplaces.viewmodel.PlacesGridScreen
import com.example.happyplaces.MapPickerScreen
import com.example.happyplaces.viewmodel.PlacesMapScreen


// Sealed class for defining screen routes
sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object HomeScreen : Screen("home_screen")
    object AddPlacesScreen : Screen("add_places_screen?placeId={placeId}") {
        fun createRoute(): String = "add_places_screen?placeId=-1"
        fun createRoute(placeId: Int): String = "add_places_screen?placeId=$placeId"
        const val ARG_PLACE_ID = "placeId"
    }
    object PlacesGridScreen : Screen("places_grid_screen")

    object PlacesMapScreen : Screen("places_map_screen")

    // --- ADD MapPickerScreen to your sealed class for type safety ---
    object MapPickerScreenRoute : Screen("mapPicker?initialLat={initialLat}&initialLon={initialLon}") {
        // Helper to build the route, similar to AddPlacesScreen
        fun createRoute(initialLat: Double? = null, initialLon: Double? = null): String {
            var route = "mapPicker"
            val params = mutableListOf<String>()
            initialLat?.let { params.add("initialLat=${it}") }
            initialLon?.let { params.add("initialLon=${it}") }
            if (params.isNotEmpty()) {
                route += "?" + params.joinToString("&")
            }
            return route
        }
        const val ARG_INITIAL_LAT = "initialLat"
        const val ARG_INITIAL_LON = "initialLon"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.PlacesGridScreen.route
    ) {

       /* composable(Screen.SplashScreen.route) {
            SplashScreen(navController = navController)
        }*/

        composable(Screen.HomeScreen.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.PlacesGridScreen.route) {
            PlacesGridScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.AddPlacesScreen.route,
            arguments = listOf(
                navArgument(Screen.AddPlacesScreen.ARG_PLACE_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt(Screen.AddPlacesScreen.ARG_PLACE_ID)
            AddPlacesScreen(
                navController = navController,
                placeIdToEdit = if (placeId == -1) null else placeId
            )
        }

        composable(Screen.PlacesMapScreen.route) {
            PlacesMapScreen(
                navController = navController
                // placesViewModel will be obtained via viewModel() inside PlacesMapScreen
            )
        }

        // --- ADD THIS COMPOSABLE BLOCK FOR MAP PICKER SCREEN ---
        composable(
            route = Screen.MapPickerScreenRoute.route, // Using the sealed class
            arguments = listOf(
                navArgument(Screen.MapPickerScreenRoute.ARG_INITIAL_LAT) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null // Explicitly can be null, NavController handles this
                },
                navArgument(Screen.MapPickerScreenRoute.ARG_INITIAL_LON) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null // Explicitly can be null
                }
            )
        ) { backStackEntry ->
            val latString = backStackEntry.arguments?.getString(Screen.MapPickerScreenRoute.ARG_INITIAL_LAT)
            val lonString = backStackEntry.arguments?.getString(Screen.MapPickerScreenRoute.ARG_INITIAL_LON)

            // Make sure MapPickerScreen composable is defined and imported
            MapPickerScreen(
                navController = navController,
                initialLat = latString?.toDoubleOrNull(),
                initialLon = lonString?.toDoubleOrNull()
            )
        }
    }
}


