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

    /* Conversation chrome (S23) */
    val bubbleRadius = 18.dp
    val bubbleTightRadius = 4.dp
    val composerRadius = 24.dp
}

/**
 * Corner radii for one message bubble inside a sender run (S23):
 *  - single message: fully rounded (18dp);
 *  - first of run: 18dp top, 4dp bottom (connects to the next bubble);
 *  - middle of run: 4dp everywhere;
 *  - last of run: 4dp top, 18dp on the far bottom corner, 4dp "tail" corner
 *    on the avatar side (bottomStart for incoming, bottomEnd for outgoing).
 */
fun PulseBubbleShape(
    isOutgoing: Boolean,
    isFirstInRun: Boolean,
    isLastInRun: Boolean,
): RoundedCornerShape {
    val r = PulseShapes.bubbleRadius
    val t = PulseShapes.bubbleTightRadius
    if (isFirstInRun && isLastInRun) return RoundedCornerShape(r)
    val top = if (isFirstInRun) r else t
    return when {
        // Middle of a run: flat top & bottom, stack reads as one shape.
        !isFirstInRun && !isLastInRun -> RoundedCornerShape(t)
        // Last of a run: tail corner hugs the avatar side.
        isLastInRun -> if (isOutgoing) {
            RoundedCornerShape(topStart = top, topEnd = top, bottomStart = r, bottomEnd = t)
        } else {
            RoundedCornerShape(topStart = top, topEnd = top, bottomStart = t, bottomEnd = r)
        }
        // First of a run: rounded top, flat bottom (connects to the run).
        else -> RoundedCornerShape(topStart = r, topEnd = r, bottomStart = t, bottomEnd = t)
    }
}
