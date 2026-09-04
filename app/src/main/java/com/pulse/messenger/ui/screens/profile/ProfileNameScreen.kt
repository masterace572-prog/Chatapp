package com.pulse.messenger.ui.screens.profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
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
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseSpacing

/**
 * S12 - Profile setup: first + last name. First name is prefilled from the
 * Google account when the flow arrived through S05.
 */
@Composable
fun ProfileNameScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var firstName by remember { mutableStateOf(vm.googleFirstName.orEmpty()) }
    var lastName by remember { mutableStateOf(vm.googleLastName.orEmpty()) }
    val firstNameValid = firstName.trim().isNotEmpty()

    AuthStepScaffold(
        title = stringResource(R.string.profile_name_title),
        subtitle = stringResource(R.string.profile_name_subtitle),
        totalSteps = 6,
        stepIndex = 0,
        onBack = onBack,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = { vm.saveProfileName(firstName, lastName) },
                enabled = firstNameValid && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        AppTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = stringResource(R.string.profile_name_first),
            placeholder = stringResource(R.string.profile_name_first_placeholder),
            autoFocus = true,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(PulseSpacing.lg))
        AppTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = stringResource(R.string.profile_name_last),
            placeholder = stringResource(R.string.profile_name_last_placeholder),
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { if (firstNameValid) vm.saveProfileName(firstName, lastName) },
            ),
        )
    }
}
