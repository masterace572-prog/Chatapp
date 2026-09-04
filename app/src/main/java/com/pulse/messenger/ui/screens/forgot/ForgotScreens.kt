package com.pulse.messenger.ui.screens.forgot

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppButtonVariant
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthSnackbar
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S07 - Forgot password: email step.
 */
@Composable
fun ForgotEmailScreen(
    vm: AuthViewModel,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf(vm.enteredEmail) }
    val emailValid = remember(email) {
        email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
    }

    AuthStepScaffold(
        title = stringResource(R.string.forgot_title),
        subtitle = stringResource(R.string.forgot_subtitle),
        totalSteps = 3,
        stepIndex = 0,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = {
                    vm.onForgotEmailEntered(email)
                    vm.requestPasswordReset()
                },
                enabled = emailValid && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        AppTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.login_email_placeholder),
            label = stringResource(R.string.login_email_label),
            leadingIcon = AppIcons.Mail,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (emailValid) {
                        vm.onForgotEmailEntered(email)
                        vm.requestPasswordReset()
                    }
                },
            ),
            autoFocus = true,
        )
    }
}

/**
 * S08 - "Check your inbox": mail icon, explanation with the email, primary
 * "Open email app" plus resend and back-to-login actions.
 */
@Composable
fun ForgotInboxScreen(
    vm: AuthViewModel,
    onBackToLogin: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AuthStepScaffold(
        title = stringResource(R.string.forgot_inbox_title),
        subtitle = null,
        totalSteps = 3,
        stepIndex = 1,
        cta = {
            AppButton(
                text = stringResource(R.string.forgot_open_email_app),
                onClick = {
                    val email = vm.enteredEmail
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        android.net.Uri.parse("mailto:$email"),
                    )
                    runCatching {
                        startActivity(context, intent, null)
                    }.onFailure {
                        vm.notice(AuthSnackbar.NoEmailApp)
                    }
                },
            )
            Spacer(Modifier.height(PulseSpacing.sm))
            AppButton(
                text = stringResource(R.string.forgot_resend),
                onClick = { vm.resendResetLink() },
                variant = AppButtonVariant.Secondary,
                loading = uiState.busy,
            )
            Spacer(Modifier.height(PulseSpacing.xs))
            AppButton(
                text = stringResource(R.string.forgot_back_to_login),
                onClick = onBackToLogin,
                variant = AppButtonVariant.Tertiary,
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(c.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(PulseIconSizes.feature),
                    tint = c.accent,
                )
            }
            Spacer(Modifier.height(PulseSpacing.xxl))
            Text(
                text = stringResource(R.string.forgot_inbox_desc, vm.enteredEmail),
                style = MaterialTheme.typography.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
            Text(
                text = stringResource(R.string.forgot_inbox_tip),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textTertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
