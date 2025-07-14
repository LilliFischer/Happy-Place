package com.example.happyplaces.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp), // For small elements like chips, text fields
    medium = RoundedCornerShape(12.dp), // Good for cards, dialogs (iOS often uses 8-16dp)
    large = RoundedCornerShape(16.dp) // For larger elements like bottom sheets
    // You can also define extraSmall and extraLarge
)
