package com.pulse.messenger.ui.screens.success

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.SuccessScreen

/**
 * S10 (reset variant) - shown after S09 sets a new password.
 * Primary action returns the user to the login email step.
 */
@Composable
fun ResetSuccessScreen(
    onBackToLogin: () -> Unit,
) {
    SuccessScreen(
        title = stringResource(R.string.success_reset_title),
        subtitle = stringResource(R.string.success_reset_subtitle),
        buttonLabel = stringResource(R.string.success_reset_button),
        onButton = onBackToLogin,
    )
}

/**
 * S10 (account variant) - shown after S17 completes onboarding.
 * Primary action enters the main shell.
 */
@Composable
fun AccountCreatedScreen(
    onStartChatting: () -> Unit,
) {
    SuccessScreen(
        title = stringResource(R.string.success_account_title),
        subtitle = stringResource(R.string.success_account_subtitle),
        buttonLabel = stringResource(R.string.success_account_button),
        onButton = onStartChatting,
    )
}
