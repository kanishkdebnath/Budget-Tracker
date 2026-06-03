package com.example.budgettracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Shape scale (design §3.3): extraLarge is the signature pill. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chip-tight
    small = RoundedCornerShape(8.dp),        // input
    medium = RoundedCornerShape(16.dp),      // card (signature)
    large = RoundedCornerShape(20.dp),       // sheet
    extraLarge = RoundedCornerShape(percent = 50), // pill / FAB
)

/** 12dp row radius — not an M3 slot, used directly by list rows. */
val RowShape = RoundedCornerShape(12.dp)
