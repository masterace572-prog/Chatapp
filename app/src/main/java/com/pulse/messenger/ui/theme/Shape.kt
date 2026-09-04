package com.pulse.messenger.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/*
 * Pulse shape tokens (PRD §4.3).
 * Radius: xs 6 (tags) / sm 8 (inputs) / md 12 (cards, buttons) /
 * lg 16 (sheets) / xl 20 (hero cards) / full (avatars, pills).
 */
object PulseShapes {
    val xs = RoundedCornerShape(6.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(20.dp)
    val full = CircleShape
    /** Bottom sheets & menus: top corners only. */
    val sheetTop = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
