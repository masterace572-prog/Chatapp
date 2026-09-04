package com.pulse.messenger.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Calm empty state (PRD §7 / S19): icon in a tinted circle, title, optional
 * subtitle and action.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = PulseTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.xxxl, vertical = PulseSpacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(c.accentContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = c.accent,
            )
        }
        Spacer(Modifier.height(PulseSpacing.xxl))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(PulseSpacing.sm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(PulseSpacing.xxl))
            AppButton(
                text = actionLabel,
                onClick = onAction,
                fillMaxWidth = false,
            )
        }
    }
}

/** Error state with retry (PRD §7 - ErrorState, S73). */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
) {
    val c = PulseTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.xxxl, vertical = PulseSpacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(c.error.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.AlertCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = c.error,
            )
        }
        Spacer(Modifier.height(PulseSpacing.xxl))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PulseSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PulseSpacing.xxl))
        AppButton(
            text = "Try again",
            onClick = onRetry,
            variant = AppButtonVariant.Secondary,
            leadingIcon = AppIcons.RotateCw,
            fillMaxWidth = false,
        )
    }
}

/** Base skeleton block with a soft pulse animation (PRD §7 - SkeletonLoader). */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = PulseShapes.md,
    color: Color = PulseTheme.colors.surfaceVariant,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = alpha)),
    )
}

/** Skeleton circle (avatars etc). */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    color: Color = PulseTheme.colors.surfaceVariant,
) {
    SkeletonBox(modifier = modifier, shape = CircleShape, color = color)
}

/**
 * Generic list skeleton - rows of [avatar, two text bars] like chats/contacts.
 */
@Composable
fun SkeletonList(
    rows: Int = 6,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 52.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonCircle(
                    modifier = Modifier.size(avatarSize),
                )
                Spacer(Modifier.width(PulseSpacing.lg))
                Column(Modifier.weight(1f)) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(16.dp),
                        shape = PulseShapes.full,
                    )
                    Spacer(Modifier.height(PulseSpacing.sm))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(12.dp),
                        shape = PulseShapes.full,
                    )
                }
            }
        }
    }
}

/** Horizontal scaffold of skeleton bars (future detail screens). */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
) {
    SkeletonBox(modifier = modifier.fillMaxWidth().height(height))
}
