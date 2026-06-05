package com.example.budgettracker.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradient tokens (design §3.6). Gradients are used ONLY on surfaces/CTAs, NEVER on data.
 * 135° gradients run top-left -> bottom-right (Offset.Zero -> Offset.Infinite);
 * 180° gradients use the default vertical linearGradient.
 */
object BudgetGradients {

    private val diagStart = Offset.Zero
    private val diagEnd = Offset.Infinite

    /** Net band hero — 135°. */
    val NetBand = Brush.linearGradient(
        colors = listOf(Color(0xFF1B4561), Color(0xFF143548), Color(0xFF0F2A3D)),
        start = diagStart, end = diagEnd,
    )

    /** FAB — 135°. */
    val Fab = Brush.linearGradient(
        colors = listOf(Color(0xFF2A5E80), Color(0xFF1B4561), Color(0xFF143548)),
        start = diagStart, end = diagEnd,
    )

    /** Tonal button / active bottom-nav pill — 180°. */
    val TonalButton = Brush.verticalGradient(listOf(Color(0xFF1B4561), Color(0xFF143548)))

    /** Filled (primary) button — 180°. */
    val FilledButton = Brush.verticalGradient(listOf(Color(0xFFB0DDED), Color(0xFF9CC8DE)))

    /** Card surfaces (txn / group / recurring / narrative / settings) — 180°, ~3% lift at top. */
    val CardSurface = Brush.verticalGradient(listOf(Color(0xFF131A20), Color(0xFF0F1619)))

    /** Bottom navigation bar — 180°, slightly darker than cards. */
    val BottomNav = Brush.verticalGradient(listOf(Color(0xFF131A20), Color(0xFF0E1419)))

    /** Floating sticky bar (e.g. Plan Save) — 180°, lifts off the surface below it. */
    val StickyBar = Brush.verticalGradient(listOf(Color(0xFF1A2229), Color(0xFF131A20)))

    /**
     * Brand-tinted top vignette behind every screen (design "phone-inner" radial glow):
     * a faint light from above the top edge. Colors only; build the sized [Brush] at draw time.
     */
    val TopGlowColors = listOf(Color(0x591C4B69), Color(0x001C4B69))

    /** Info banner — 135°. */
    val BannerInfo = Brush.linearGradient(
        colors = listOf(Color(0xFF1B4561), Color(0xFF143548)),
        start = diagStart, end = diagEnd,
    )

    /** Amber banner — 135°. */
    val BannerAmber = Brush.linearGradient(
        colors = listOf(Color(0xFF7B4520), Color(0xFF5C3013)),
        start = diagStart, end = diagEnd,
    )

    /** Applied-recurring surface — green-tinted, 180°. */
    val AppliedRecurring = Brush.verticalGradient(
        listOf(Color(0x1A74D9B5), Color(0x0A74D9B5)),
    )

    // ---- Light-theme brand variants (design: fully-light light theme) ----
    /** Net band hero — light blue tint, dark text. */
    val NetBandLight = Brush.linearGradient(
        colors = listOf(Color(0xFFE9F2FA), Color(0xFFDCEBF6), Color(0xFFCFE2F0)),
        start = diagStart, end = diagEnd,
    )

    /** Filled (primary) button — solid navy in light (white text). */
    val FilledButtonLight = Brush.verticalGradient(listOf(Color(0xFF0D2736), Color(0xFF0D2736)))

    /** Tonal button — light navy tint (navy text). */
    val TonalButtonLight = Brush.verticalGradient(listOf(Color(0xFFD7E8F4), Color(0xFFC7DEEE)))

    /** Sticky bar — white card. */
    val StickyBarLight = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF6FAFC)))

    /** Info banner — light blue tint. */
    val BannerInfoLight = Brush.linearGradient(
        colors = listOf(Color(0xFFE2EEF8), Color(0xFFD6E8F4)), start = diagStart, end = diagEnd,
    )

    /** Amber banner — light amber tint. */
    val BannerAmberLight = Brush.linearGradient(
        colors = listOf(Color(0xFFFFEBD2), Color(0xFFFCE2C0)), start = diagStart, end = diagEnd,
    )
}
