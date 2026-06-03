package com.example.budgettracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Letter-spacing is expressed in sp (design §3.2 gives em; converted at each size). It must NOT be
// in `em`: Material 3 lerps these styles against its own sp-based styles (e.g. the OutlinedTextField
// floating label), and Compose cannot interpolate between Em and Sp units (crashes at runtime).

/** Material 3 type scale in Inter (design §3.2). */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.79).sp, // -0.022em
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.29).sp, // -0.012em
    ),
    titleLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.48.sp, // +0.04em
    ),
)

/**
 * Money style (design §3.2): 18sp, medium, tabular numerals.
 * MANDATORY anywhere a monetary amount is rendered. Access via MaterialTheme.typography.money.
 */
val MoneyTextStyle = TextStyle(
    fontFamily = InterFamily, fontWeight = FontWeight.Medium,
    fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.18).sp, // -0.01em
    fontFeatureSettings = "tnum",
)

val Typography.money: TextStyle get() = MoneyTextStyle
