package com.example.budgettracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import com.example.budgettracker.ui.theme.BudgetGradients

/**
 * App background: the theme's solid background plus a brand-tinted radial glow descending from
 * just above the top edge (design "phone-inner" vignette). The glow is dark-only chrome; in light
 * themes it would muddy the surface, so it's skipped there. Render this once, behind the Scaffold,
 * with a transparent Scaffold/top-bar so the glow shows through every screen.
 */
@Composable
fun BudgetBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(scheme.background)
                if (isDark) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = BudgetGradients.TopGlowColors,
                            center = Offset(size.width / 2f, -size.height * 0.08f),
                            radius = size.width * 0.95f,
                        ),
                    )
                }
            },
    ) {
        content()
    }
}
