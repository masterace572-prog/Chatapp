package com.pulse.messenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import kotlin.math.min

/**
 * Pulse brand mark - accent rounded tile with the pulse line (S01/S02).
 * The identical geometry lives in res/drawable/ic_splash_logo.xml.
 */
@Composable
fun PulseLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    backgroundColor: Color = PulseTheme.colors.accent,
    lineColor: Color = Color.White,
) {
    Canvas(modifier = modifier.size(size)) {
        val tile = this.size
        val radiusPx = tile.width * 0.22f
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(radiusPx, radiusPx),
        )
        val stroke = tile.width * 0.085f
        val path = Path().apply {
            val y0 = tile.height * 0.56f
            moveTo(tile.width * 0.14f, y0)
            lineTo(tile.width * 0.36f, y0)
            lineTo(tile.width * 0.48f, tile.height * 0.34f)
            lineTo(tile.width * 0.60f, tile.height * 0.70f)
            lineTo(tile.width * 0.70f, y0)
            lineTo(tile.width * 0.86f, y0)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/**
 * Lockup: mark + wordmark, used by Splash (S01) and Welcome (S02).
 */
@Composable
fun PulseLogoLockup(
    modifier: Modifier = Modifier,
    markSize: Dp = 64.dp,
    showWordmark: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PulseLogoMark(size = markSize)
        if (showWordmark) {
            Spacer(Modifier.height(PulseSpacing.lg))
            Text(
                text = "Pulse",
                style = MaterialTheme.typography.titleLarge,
                color = PulseTheme.colors.textPrimary,
            )
        }
    }
}
