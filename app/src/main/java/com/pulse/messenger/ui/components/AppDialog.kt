package com.pulse.messenger.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Pulse alert dialog (PRD §7 - AppDialog): surface container, md-lg radius,
 * accent confirm / destructive confirm actions.
 */
@Composable
fun AppDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Cancel",
    destructive: Boolean = false,
    icon: ImageVector? = null,
) {
    val c = PulseTheme.colors
    val confirmColor = if (destructive) c.error else c.accent
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = PulseShapes.lg,
        containerColor = c.surface,
        icon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (destructive) c.error else c.accent,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            null
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary,
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = confirmColor,
                )
            }
        },
        dismissButton = if (dismissLabel.isNotEmpty()) {
            {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = c.textSecondary,
                    )
                }
            }
        } else {
            null
        },
    )
}

/**
 * Confirm-destructive-action dialog (PRD §7 - ConfirmDialog): warning icon,
 * destructive confirm button.
 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDialog(
        title = title,
        text = text,
        confirmLabel = confirmLabel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
        destructive = true,
        icon = AppIcons.AlertTriangle,
    )
}
