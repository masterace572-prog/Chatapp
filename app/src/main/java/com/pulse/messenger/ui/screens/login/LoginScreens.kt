package com.pulse.messenger.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.CountryCode
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppIconButton
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.CountryPickerSheet
import com.pulse.messenger.ui.components.Tag
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

private val DEFAULT_COUNTRY = CountryCode("IN", "India", "+91")

/**
 * S03 - Login with email. One question per screen: continue advances to the
 * password step (S04). "Use phone number instead" reroutes to S03b.
 */
@Composable
fun LoginEmailScreen(
    vm: AuthViewModel,
    onContinue: () -> Unit,
    onUsePhone: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    val emailValid = remember(email) {
        email.trim().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
    }

    fun submit() {
        if (emailValid) {
            vm.onEmailEntered(email)
            onContinue()
        }
    }

    AuthStepScaffold(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_email_subtitle),
        totalSteps = 2,
        stepIndex = 0,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = ::submit,
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
            keyboardActions = KeyboardActions(onDone = { submit() }),
            autoFocus = true,
        )
        Text(
            text = stringResource(R.string.login_use_phone),
            style = MaterialTheme.typography.labelLarge,
            color = c.accent,
            modifier = Modifier
                .clickable(onClick = onUsePhone)
                .padding(vertical = PulseSpacing.md),
        )
    }
}

/**
 * S04 - Password for the email chosen on S03. The chip shows that email with an
 * edit action. Demo rule (PRD §9): password "wrong" fails; anything else
 * >= 8 chars signs in.
 */
@Composable
fun LoginPasswordScreen(
    vm: AuthViewModel,
    onBackToEmail: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    val showError = uiState.error != null
    val valid = password.length >= 8

    AuthStepScaffold(
        title = stringResource(R.string.login_password_title),
        subtitle = stringResource(R.string.login_password_subtitle),
        totalSteps = 2,
        stepIndex = 1,
        onBack = onBackToEmail,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = { vm.loginWithPassword(password) },
                enabled = valid && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(
                text = vm.enteredEmail.ifBlank { "@" },
                containerColor = c.surfaceVariant,
                contentColor = c.textSecondary,
            )
            Spacer(Modifier.width(PulseSpacing.xs))
            AppIconButton(
                icon = AppIcons.Pencil,
                contentDescription = stringResource(R.string.common_edit),
                onClick = onBackToEmail,
                iconSize = 18.dp,
            )
        }
        Spacer(Modifier.height(PulseSpacing.lg))
        AppTextField(
            value = password,
            onValueChange = {
                password = it
                vm.dismissError()
            },
            placeholder = stringResource(R.string.login_password_placeholder),
            label = stringResource(R.string.login_password_label),
            isPassword = true,
            isError = showError,
            supportingText = if (showError) uiState.error else null,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { if (valid) vm.loginWithPassword(password) },
            ),
            autoFocus = true,
        )
        Text(
            text = stringResource(R.string.login_forgot_password),
            style = MaterialTheme.typography.labelLarge,
            color = c.accent,
            modifier = Modifier
                .clickable(onClick = onForgotPassword)
                .padding(top = PulseSpacing.xl, bottom = PulseSpacing.md),
        )
    }
}

/**
 * S03b - Phone login: country code (ISO badge - no emoji flags) + number.
 * Continue sends the mock OTP and routes to S06.
 */
@Composable
fun LoginPhoneScreen(
    vm: AuthViewModel,
    onUseEmail: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var country by remember { mutableStateOf(DEFAULT_COUNTRY) }
    var number by remember { mutableStateOf("") }
    var showCountrySheet by remember { mutableStateOf(false) }
    val numberValid = remember(number) { number.filter(Char::isDigit).length >= 7 }

    AuthStepScaffold(
        title = stringResource(R.string.phone_title),
        subtitle = stringResource(R.string.phone_subtitle),
        totalSteps = 2,
        stepIndex = 0,
        onBack = onUseEmail,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = { vm.loginWithPhone(country, number) },
                enabled = numberValid && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        // Country selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PulseShapes.md)
                .background(c.surface)
                .clickable { showCountrySheet = true }
                .padding(PulseSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 30.dp)
                    .clip(PulseShapes.sm)
                    .background(c.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = country.iso,
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textPrimary,
                )
            }
            Spacer(Modifier.width(PulseSpacing.md))
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge,
                color = c.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = country.prefix,
                style = MaterialTheme.typography.titleMedium,
                color = c.textSecondary,
            )
            Spacer(Modifier.width(PulseSpacing.xs))
            Icon(
                imageVector = AppIcons.ChevronDown,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.textSecondary,
            )
        }
        Spacer(Modifier.height(PulseSpacing.lg))
        AppTextField(
            value = number,
            onValueChange = { number = it },
            placeholder = stringResource(R.string.phone_hint),
            label = stringResource(R.string.phone_label),
            leadingIcon = AppIcons.Smartphone,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { if (numberValid) vm.loginWithPhone(country, number) },
            ),
            autoFocus = true,
        )
    }

    if (showCountrySheet) {
        CountryPickerSheet(
            onDismiss = { showCountrySheet = false },
            onSelect = { country = it },
        )
    }
}
