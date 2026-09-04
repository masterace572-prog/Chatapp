package com.pulse.messenger.ui.screens.otp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.OtpChannel
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.OtpInput
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * S06 (phone login) / S11b (sign-up email verification) share this screen.
 * The shared AuthViewModel carries the active channel + identifier.
 * Auto-submits on the 6th digit; wrong codes shake, clear and show an error.
 */
@Composable
fun OtpScreen(
    vm: AuthViewModel,
    onChangeIdentifier: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val emailVerify = vm.otpChannel == OtpChannel.EmailVerification

    val title = stringResource(
        if (emailVerify) R.string.otp_email_title else R.string.otp_phone_title,
    )
    val subtitle = stringResource(
        if (emailVerify) R.string.otp_email_subtitle else R.string.otp_phone_subtitle,
        vm.otpIdentifier.ifBlank { "—" },
    )

    // 30s resend countdown; resendTick restarts it.
    var resendTick by remember { mutableIntStateOf(0) }
    val secondsLeft by produceState(initialValue = 30, key1 = resendTick) {
        while (value > 0) {
            delay(1000)
            value -= 1
        }
    }

    // Clear cells whenever a new error arrives.
    var errorCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) errorCount += 1
    }
    val errorText = uiState.error

    AuthStepScaffold(
        title = title,
        subtitle = subtitle,
        totalSteps = if (emailVerify) 3 else 2,
        stepIndex = 1,
        onBack = onChangeIdentifier,
    ) {
        OtpInput(
            onComplete = { code -> vm.verifyOtp(code) },
            isError = errorText != null,
            enabled = !uiState.busy,
            resetSignal = errorCount,
        )

        if (uiState.busy) {
            Spacer(Modifier.height(PulseSpacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = c.accent,
                    strokeWidth = 2.dp,
                )
            }
        }

        if (errorText != null) {
            Spacer(Modifier.height(PulseSpacing.md))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodyMedium,
                color = c.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(PulseSpacing.xl))
        Text(
            text = stringResource(R.string.otp_demo_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = c.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(PulseSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (secondsLeft > 0) {
                Text(
                    text = stringResource(
                        R.string.otp_resend_in,
                        String.format(Locale.US, "%02d", secondsLeft),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textTertiary,
                )
            } else {
                Text(
                    text = stringResource(R.string.otp_resend),
                    style = MaterialTheme.typography.labelLarge,
                    color = c.accent,
                    modifier = Modifier
                        .clickable {
                            vm.resendOtp()
                            resendTick += 1
                        }
                        .padding(PulseSpacing.sm),
                )
            }
        }

        Spacer(Modifier.height(PulseSpacing.sm))
        Text(
            text = stringResource(
                if (emailVerify) R.string.otp_change_email else R.string.otp_change_phone,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onChangeIdentifier)
                .padding(PulseSpacing.sm),
        )
    }
}
