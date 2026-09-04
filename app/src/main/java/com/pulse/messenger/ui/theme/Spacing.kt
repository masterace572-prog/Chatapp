package com.pulse.messenger.ui.theme

import androidx.compose.ui.unit.dp

/*
 * Pulse spacing & sizing tokens (PRD §4.3) on a 4/8dp grid.
 * Screen horizontal padding: 20dp. Min touch target: 48dp. Button height: 52dp.
 */
object PulseSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
    val colossal = 48.dp
    /** Standard screen horizontal padding (PRD §4.3). */
    val screen = 20.dp
}

/** Icon sizes (PRD §4.4): 20 inline, 24 default, 28 feature. */
object PulseIconSizes {
    val inline = 20.dp
    val default = 24.dp
    val feature = 28.dp
}

/** Interaction & layout constants (PRD §4.3). */
object PulseSizes {
    val buttonHeight = 52.dp
    val minTouchTarget = 48.dp
    val avatarChat = 52.dp
    val topBarHeight = 56.dp
    val dividerHairline = 1.dp
    val inputHeight = 56.dp

    /** List divider indent aligning under the 52dp avatar block (S19/S21). */
    val chatRowDividerInset = 84.dp

    /** Bottom list padding so the floating action bar never covers the last row. */
    val fabClearance = 96.dp
}
