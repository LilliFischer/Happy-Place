package com.example.happyplaces.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.happyplaces.R // Ensure this R file import is correct

// Define the FontFamily using your variable font "tiktoksans_variable.ttf"
val TikTokSansFamily = FontFamily(
    Font(R.font.tiktoksans_variable, FontWeight.Light),
    Font(R.font.tiktoksans_variable, FontWeight.Normal),
    Font(R.font.tiktoksans_variable, FontWeight.Medium),
    Font(R.font.tiktoksans_variable, FontWeight.SemiBold),
    Font(R.font.tiktoksans_variable, FontWeight.Bold),
    Font(R.font.tiktoksans_variable, FontWeight.ExtraBold)
    // You can add more specific weights if TikTok Sans supports them and you need them,
    // e.g., FontWeight(350) for a weight between Light (300) and Normal (400).
    // For italics, if the font has a 'slnt' axis, advanced FontVariation.Settings might be needed.
)

// Define your AppTypography using the TikTokSansFamily
// These are starting points. You'll likely want to adjust the fontWeight
// for different text styles based on how TikTokSans renders.
val AppTypography = Typography(
    // Display Styles (Largest text on screen)
    displayLarge = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Normal, // Or try FontWeight.Light for a lighter display look
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Normal, // Or FontWeight.Medium
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline Styles (High-emphasis text, typically shorter)
    headlineLarge = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.SemiBold, // Good for prominent headlines
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.SemiBold, // Or FontWeight.Medium
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title Styles (Smaller than headlines, often for screen titles or component titles)
    titleLarge = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Medium, // Or FontWeight.SemiBold for more emphasis
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Medium, // Or FontWeight.Normal for less emphasis
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body Styles (Longer-form text)
    bodyLarge = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp // Or 0.15.sp for a tighter iOS feel
    ),
    bodyMedium = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Normal, // Or FontWeight.Light
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label Styles (For buttons, captions, overline text - typically shorter and utilitarian)
    labelLarge = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Medium, // Or SemiBold for strong button labels
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp // Material default, iOS might use less or more tracking
    ),
    labelSmall = TextStyle(
        fontFamily = TikTokSansFamily,
        fontWeight = FontWeight.Medium, // Or FontWeight.Normal
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
