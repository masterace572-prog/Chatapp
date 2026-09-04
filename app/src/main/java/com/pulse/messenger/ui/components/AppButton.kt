package com.pulse.messenger.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

enum class AppButtonVariant { Primary, Secondary, Tertiary, Destructive }

/**
 * Primary CTA of the Pulse design system (PRD §4.3: 52dp height, md 12dp radius).
 * Press feedback: ripple + 0.98 scale (PRD §4.5). Loading and disabled states included.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
) {
    val c = PulseTheme.colors
    val (container, content, border) = when (variant) {
        AppButtonVariant.Primary -> Triple(c.accent, c.onAccent, null)
        AppButtonVariant.Secondary -> Triple(c.surface, c.textPrimary, c.border)
        AppButtonVariant.Tertiary -> Triple(Color.Transparent, c.accent, null)
        AppButtonVariant.Destructive -> Triple(c.error, c.onError, null)
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.98f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "buttonScale",
    )
    val effectiveEnabled = enabled && !loading

    val contentModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .semantics { if (contentDescription != null) this.contentDescription = text }
        .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)

    Surface(
        onClick = onClick,
        modifier = contentModifier,
        enabled = effectiveEnabled,
        shape = PulseShapes.md,
        color = container,
        contentColor = if (effectiveEnabled) content else content.copy(alpha = 0.45f),
        border = border?.let { BorderStroke(1.dp, it) },
        interactionSource = interaction,
    ) {
        Row(
            modifier = Modifier
                .height(PulseSizes.buttonHeight)
                .padding(horizontal = PulseSpacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(PulseIconSizes.inline),
                        color = content,
                        strokeWidth = 2.dp,
                    )
                }
                leadingIcon != null -> {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = content,
                    )
                    Spacer(Modifier.width(PulseSpacing.sm))
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Icon button with a 48dp minimum touch target (PRD §4.3).
 * `containerColor = null` renders a bare icon (top bars); a color renders a
 * tinted circle (action rows, sheets).
 */
@Composable
fun AppIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = PulseTheme.colors.textSecondary,
    containerColor: Color? = null,
    iconSize: Dp = PulseIconSizes.default,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(PulseSizes.minTouchTarget)
            .then(
                if (containerColor != null) {
                    Modifier.background(color = containerColor, shape = CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
        )
    }
}
