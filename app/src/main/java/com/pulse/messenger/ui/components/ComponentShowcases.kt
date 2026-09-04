package com.pulse.messenger.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/*
 * Audit gap-fill: previews (light AND dark) for components that shipped
 * without a showcase. Interactive modals (CountryPickerSheet, AppBottomSheet)
 * are demonstrated by their non-interactive neighbours where possible.
 */

@Composable
private fun AuditShowcaseHost(content: @Composable () -> Unit) {
    PulseTheme {
        Surface(color = PulseTheme.colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { content() }
        }
    }
}

@Preview(name = "SwipeableRow · Light", showBackground = true, widthDp = 400)
@Preview(name = "SwipeableRow · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun SwipeableRowPreview() {
    val c = PulseTheme.colors
    AuditShowcaseHost {
        Text("Swipe right: archive · Swipe left: pin", color = c.textSecondary)
        SwipeableRow(
            startAction = SwipeAction(
                label = "Archive",
                icon = AppIcons.Archive,
                background = c.surfaceVariant,
                contentColor = c.textSecondary,
                onTrigger = {},
            ),
            endAction = SwipeAction(
                label = "Pin",
                icon = AppIcons.Pin,
                background = c.accentContainer,
                contentColor = c.onAccentContainer,
                onTrigger = {},
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.background)
                    .padding(PulseSpacing.lg),
            ) {
                Text("A chat row inside SwipeableRow", color = c.textPrimary)
            }
        }
    }
}

@Preview(name = "OtpInput · Light", showBackground = true, widthDp = 400)
@Preview(name = "OtpInput · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun OtpInputPreview() {
    AuditShowcaseHost {
        OtpInput(onComplete = {})
    }
}

@Preview(name = "PasswordStrength · Light", showBackground = true, widthDp = 400)
@Preview(name = "PasswordStrength · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun PasswordStrengthPreview() {
    AuditShowcaseHost {
        PasswordStrengthMeter(score = 3)
        PasswordStrengthLabel(score = 3)
        PasswordStrengthMeter(score = 1)
        PasswordStrengthLabel(score = 1)
    }
}

@Preview(name = "PermissionCard · Light", showBackground = true, widthDp = 400)
@Preview(name = "PermissionCard · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun PermissionCardPreview() {
    AuditShowcaseHost {
        PermissionCard(
            icon = AppIcons.Users,
            title = "Contacts",
            reason = "Find people you know on Pulse.",
            granted = false,
            onAllow = {},
        )
        PermissionCard(
            icon = AppIcons.Camera,
            title = "Camera",
            reason = "Snap and share photos in chats.",
            granted = true,
            onAllow = {},
        )
    }
}

@Preview(name = "SuccessScreen · Light", showBackground = true, widthDp = 400)
@Preview(name = "SuccessScreen · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 400)
@Composable
private fun SuccessScreenPreview() {
    SuccessScreen(
        title = "Account created",
        subtitle = "Welcome to Pulse - your chats are waiting.",
        buttonLabel = "Start chatting",
        onButton = {},
    )
}
