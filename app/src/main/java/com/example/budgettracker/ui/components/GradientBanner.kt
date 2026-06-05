package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.theme.BudgetGradients
import com.example.budgettracker.ui.theme.BudgetTheme

enum class BannerTone { INFO, AMBER }

private val BannerShape = RoundedCornerShape(14.dp)

/** Banner on a navy (info) or amber gradient with light text (design §3.6 / banners). */
@Composable
fun GradientBanner(
    text: String,
    tone: BannerTone,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val light = BudgetTheme.isLight
    val brush = when (tone) {
        BannerTone.INFO -> if (light) BudgetGradients.BannerInfoLight else BudgetGradients.BannerInfo
        BannerTone.AMBER -> if (light) BudgetGradients.BannerAmberLight else BudgetGradients.BannerAmber
    }
    val content = when (tone) {
        BannerTone.INFO -> if (light) Color(0xFF0D2736) else Color(0xFFCDE3F2)
        BannerTone.AMBER -> if (light) Color(0xFF6A3B16) else Color(0xFFFBE3CC)
    }
    Box(modifier.fillMaxWidth().clip(BannerShape).background(brush).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp), tint = content)
                Spacer(Modifier.width(10.dp))
            }
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = content)
            trailing?.invoke()
        }
    }
}
