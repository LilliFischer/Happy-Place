package com.example.happyplaces

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.happyplaces.AppNavigation
import com.example.happyplaces.ui.theme.HappyPlacesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HappyPlacesTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
