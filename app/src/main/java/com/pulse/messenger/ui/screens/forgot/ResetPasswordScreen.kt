package com.pulse.messenger.ui.screens.forgot

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.PasswordChecks
import com.pulse.messenger.ui.components.PasswordRequirements
import com.pulse.messenger.ui.components.PasswordStrengthLabel
import com.pulse.messenger.ui.components.PasswordStrengthMeter
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S09 - Reset password: new + confirm with live strength meter and the
 * requirements checklist (PRD S09). Shared layout reused by S11c.
 */
@Composable
fun ResetPasswordScreen(
    vm: AuthViewModel,
    isSignUp: Boolean = false,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = PulseTheme.colors
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val checks = remember(password) { PasswordChecks.evaluate(password) }
    val confirmMismatch = remember(password, confirm) { confirm.isNotEmpty() && password != confirm }
    val allValid = checks.score == 4 && !confirmMismatch

    val title = stringResource(
        if (isSignUp) R.string.signup_password_title else R.string.reset_title,
    )
    val subtitle = stringResource(
        if (isSignUp) R.string.signup_password_subtitle else R.string.reset_subtitle,
    )

    AuthStepScaffold(
        title = title,
        subtitle = subtitle,
        totalSteps = if (isSignUp) 3 else 3,
        stepIndex = if (isSignUp) 2 else 2,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = {
                    if (isSignUp) vm.saveSignupPassword(password) else vm.resetPassword(password)
                },
                enabled = allValid && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.reset_password_label),
            placeholder = stringResource(R.string.reset_password_placeholder),
            isPassword = true,
            autoFocus = true,
            imeAction = ImeAction.Next,
            isError = false,
        )
        Spacer(Modifier.height(PulseSpacing.lg))
        AppTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = stringResource(R.string.reset_confirm_label),
            placeholder = stringResource(R.string.reset_confirm_placeholder),
            isPassword = true,
            isError = confirmMismatch,
            supportingText = if (confirmMismatch) {
                stringResource(R.string.reset_confirm_mismatch)
            } else {
                null
            },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (allValid) {
                        if (isSignUp) vm.saveSignupPassword(password) else vm.resetPassword(password)
                    }
                },
            ),
        )
        if (password.isNotEmpty()) {
            Spacer(Modifier.height(PulseSpacing.xxl))
            PasswordStrengthMeter(score = checks.score)
            Spacer(Modifier.height(PulseSpacing.sm))
            PasswordStrengthLabel(score = checks.score)
            Spacer(Modifier.height(PulseSpacing.lg))
            PasswordRequirements(result = checks)
        }
        Spacer(Modifier.height(PulseSpacing.lg))
        Text(
            text = stringResource(R.string.reset_demo_hint),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = c.textTertiary,
        )
    }
}
