package com.pulse.messenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTone
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseTheme
import kotlin.math.roundToInt

/** Turns a display name into up-to-two-letter initials, e.g. "Ada Lovelace" -> "AL". */
fun initialsOf(name: String?): String {
    if (name.isNullOrBlank()) return ""
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    val first = parts.first().firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    val result = first + second
    return result.ifEmpty { parts.first().take(2).uppercase() }
}

/**
 * Avatar - photo-ready but initials-first in Phase 1 (photos land with Coil in a
 * later milestone). Supports online dot, group composite and story rings later.
 * (PRD §7 - Avatar: sizes 24-120dp, initials, group composite, online dot.)
 */
@Composable
fun Avatar(
    name: String?,
    modifier: Modifier = Modifier,
    avatarTone: AvatarTone = AvatarTones.Slate,
    size: Dp = 52.dp,
    isOnline: Boolean = false,
    isGroup: Boolean = false,
    groupMemberNames: List<String> = emptyList(),
    onlineRingColor: Color = PulseTheme.colors.background,
) {
    if (isGroup && groupMemberNames.size >= 2) {
        GroupAvatar(
            memberNames = groupMemberNames,
            modifier = modifier,
            size = size,
        )
        return
    }
    val c = PulseTheme.colors
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(avatarTone.background),
            contentAlignment = Alignment.Center,
        ) {
            val nameForInitials = name ?: ""
            if (nameForInitials.isNotBlank()) {
                Text(
                    text = initialsOf(nameForInitials),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = com.pulse.messenger.ui.theme.PulseFontWeights.SemiBold,
                        fontSize = com.pulse.messenger.ui.theme.avatarInitialsFontSize(size.value),
                        color = avatarTone.foreground,
                    ),
                    textAlign = TextAlign.Center,
                )
            } else {
                Icon(
                    imageVector = if (isGroup) AppIcons.Users else AppIcons.User,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.44f),
                    tint = if (isGroup) c.textTertiary else c.textTertiary,
                )
            }
        }
        if (isOnline) {
            val dot = size * 0.26f
            Canvas(Modifier.align(Alignment.BottomEnd)) {
                val ring = dot.toPx() * 0.42f
                val radius = dot.toPx() * 0.32f
                drawCircle(color = onlineRingColor, radius = ring)
                drawCircle(color = c.success, radius = radius)
            }
        }
    }
}

/** Composite group avatar: tinted base + two overlapping member initials tiles. */
@Composable
private fun GroupAvatar(
    memberNames: List<String>,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val c = PulseTheme.colors
    val tile = size * 0.68f
    val names = memberNames.take(2)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(c.surfaceVariant),
    ) {
        // Top-left tile
        val toneA = AvatarTones.bySeed(names.firstOrNull()?.hashCode() ?: 0)
        Avatar(
            name = names.getOrNull(0),
            avatarTone = toneA,
            size = tile,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = size * 0.02f, y = size * 0.04f),
        )
        // Bottom-right tile (overlap)
        val toneB = AvatarTones.bySeed(names.getOrNull(1)?.hashCode() ?: 1)
        Avatar(
            name = names.getOrNull(1),
            avatarTone = toneB,
            size = tile,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = size * 0.06f, y = size * 0.02f),
        )
    }
}

/** Small presence / badge dot that can overlay an avatar corner. */
@Composable
fun StatusDot(
    modifier: Modifier = Modifier,
    color: Color = PulseTheme.colors.success,
    size: Dp = 10.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}
