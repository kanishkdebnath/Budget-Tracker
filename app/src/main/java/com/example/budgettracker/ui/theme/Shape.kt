package com.example.budgettracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale (design §3.3). The signature pill is NOT mapped onto `extraLarge`: M3 components
 * (ModalBottomSheet, DatePickerDialog, large FAB) read `extraLarge`, so a 50% corner there turns
 * those surfaces into ellipses that clip their content. The pill is applied explicitly via
 * [PillShape] / CircleShape where wanted (FAB, CTAs, chips).
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chip-tight
    small = RoundedCornerShape(8.dp),        // input
    medium = RoundedCornerShape(16.dp),      // card (signature)
    large = RoundedCornerShape(20.dp),       // sheet
    extraLarge = RoundedCornerShape(28.dp),  // dialogs / large containers (M3 standard)
)

/** 12dp row radius — not an M3 slot, used directly by list rows. */
val RowShape = RoundedCornerShape(12.dp)

/** Signature pill — FAB, primary CTAs, active-nav indicator, chips (design §3.3). */
val PillShape = RoundedCornerShape(percent = 50)
