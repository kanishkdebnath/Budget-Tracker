package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.icons.iconVectorForKey
import com.example.budgettracker.ui.screens.categories.ColorDot

/**
 * Leading visual for a category. When [iconKey] resolves to a registry icon, render it tinted by
 * [color] inside a faint color-washed rounded square; otherwise fall back to the color dot, centered
 * in the same [size] slot so rows stay aligned whether or not a category has an icon.
 */
@Composable
fun CategoryIconChip(
    iconKey: String?,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
) {
    val vector = iconVectorForKey(iconKey)
    if (vector != null) {
        Box(
            modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.3f))
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(vector, contentDescription = null, tint = color, modifier = Modifier.size(size * 0.58f))
        }
    } else {
        Box(modifier.size(size), contentAlignment = Alignment.Center) {
            ColorDot(color, size = size * 0.32f)
        }
    }
}
