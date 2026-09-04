package com.pulse.messenger.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Password strength evaluation (PRD S09): 4 muted segments coloured
 * error -> warning -> success, plus a requirements checklist with check icons.
 */
object PasswordChecks {
    const val MIN_LENGTH = 8

    data class Result(
        val hasLength: Boolean,
        val hasUppercase: Boolean,
        val hasDigit: Boolean,
        val hasSymbol: Boolean,
    ) {
        val score: Int
            get() = listOf(hasLength, hasUppercase, hasDigit, hasSymbol).count { it }
    }

    fun evaluate(password: String): Result = Result(
        hasLength = password.length >= MIN_LENGTH,
        hasUppercase = password.any { it.isUpperCase() },
        hasDigit = password.any { it.isDigit() },
        hasSymbol = password.any { !it.isLetterOrDigit() },
    )
}

/** 4-segment meter. Filled segments count = score, tinted by strength. */
@Composable
fun PasswordStrengthMeter(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val fillColor = when {
        score >= 4 -> c.success
        score == 3 -> c.warning
        else -> c.error
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (index < score) fillColor else c.surfaceVariant),
            )
        }
    }
}

/** Strength caption under the meter: Weak / Fair / Good / Strong. */
@Composable
fun PasswordStrengthLabel(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val (label, color) = when {
        score >= 4 -> "Strong" to c.success
        score == 3 -> "Good" to c.warning
        score == 2 -> "Fair" to c.warning
        else -> "Weak" to c.error
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier,
    )
}

/** One requirement line with a check icon when satisfied (PRD S09 checklist). */
@Composable
fun PasswordRequirementRow(
    label: String,
    met: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(com.pulse.messenger.ui.theme.PulseIconSizes.inline)
                .clip(CircleShape)
                .background(if (met) c.success.copy(alpha = 0.14f) else c.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (met) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = c.success,
                )
            }
        }
        Spacer(Modifier.width(PulseSpacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (met) c.textPrimary else c.textSecondary,
        )
    }
}

/** Convenience: full checklist used by S09 / S11c. */
@Composable
fun PasswordRequirements(
    result: PasswordChecks.Result,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PasswordRequirementRow("At least 8 characters", result.hasLength)
        PasswordRequirementRow("One uppercase letter", result.hasUppercase)
        PasswordRequirementRow("One number", result.hasDigit)
        PasswordRequirementRow("One symbol", result.hasSymbol)
    }
}
