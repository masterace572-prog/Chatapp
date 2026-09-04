package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Reusable success screen body (PRD S10): check icon in a success-tinted
 * circle, title, subtitle and a single primary button. Used by the password
 * reset flow and account-creation completion.
 */
@Composable
fun SuccessScreen(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onButton: () -> Unit,
    modifier: Modifier = Modifier,
    buttonLoading: Boolean = false,
    icon: ImageVector = AppIcons.Check,
) {
    val c = PulseTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .padding(horizontal = PulseSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(c.success.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = c.success,
            )
        }
        Spacer(Modifier.height(PulseSpacing.xxl))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PulseSpacing.sm))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(PulseSpacing.xxxl))
        AppButton(
            text = buttonLabel,
            onClick = onButton,
            loading = buttonLoading,
        )
    }
}
