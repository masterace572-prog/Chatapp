package com.pulse.messenger.ui.screens.signup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S11a - Sign-up email. Continue sends the mock verification code and routes
 * to the shared OTP screen (S11b). "Already have an account?" leads to login.
 */
@Composable
fun SignupEmailScreen(
    vm: AuthViewModel,
    onLoginInstead: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    val emailValid = remember(email) {
        email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
    }
    val showError = uiState.error != null

    AuthStepScaffold(
        title = stringResource(R.string.signup_email_title),
        subtitle = stringResource(R.string.signup_email_subtitle),
        totalSteps = 3,
        stepIndex = 0,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = {
                    vm.onSignupEmailEntered(email)
                    vm.signUpWithEmail()
                },
                enabled = emailValid && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        AppTextField(
            value = email,
            onValueChange = {
                email = it
                vm.dismissError()
            },
            placeholder = stringResource(R.string.login_email_placeholder),
            label = stringResource(R.string.login_email_label),
            leadingIcon = AppIcons.Mail,
            keyboardType = KeyboardType.Email,
            isError = showError,
            supportingText = if (showError) uiState.error else null,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (emailValid) {
                        vm.onSignupEmailEntered(email)
                        vm.signUpWithEmail()
                    }
                },
            ),
            autoFocus = true,
        )
        Text(
            text = stringResource(R.string.signup_already_account),
            style = MaterialTheme.typography.labelLarge,
            color = c.accent,
            modifier = Modifier
                .clickable(onClick = onLoginInstead)
                .padding(top = PulseSpacing.xl, bottom = PulseSpacing.md),
        )
    }
}
