package com.example.budgettracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A one-shot scale "pop" (1 → 1.06 → 1) each time [selected] becomes true — e.g. tapping a filter
 * chip. Skips the initial composition so the default-selected chip doesn't pop on screen load.
 */
@Composable
fun Modifier.chipPop(selected: Boolean): Modifier {
    val scale = remember { Animatable(1f) }
    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(selected) {
        if (selected && !firstRun) {
            scale.snapTo(1f)
            scale.animateTo(1f, keyframes { durationMillis = 220; 1.06f at 90; 1f at 220 })
        }
        firstRun = false
    }
    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
