package com.pulse.messenger.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.data.mock.SeedData
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppChip
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.Tag
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.screens.auth.UsernameCheck
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S13 - Username with live availability check (mock): spinner -> check / error,
 * suggestions chips derived from the display name.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileUsernameScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val checkState by vm.usernameCheck.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }

    val suggestions = remember(vm.googleFirstName, vm.googleLastName) {
        SeedData.usernameSuggestions(vm.googleFirstName.orEmpty(), vm.googleLastName.orEmpty())
    }

    LaunchedEffect(username) {
        vm.checkUsername(username)
    }

    val available = checkState == UsernameCheck.Available
    val taken = checkState is UsernameCheck.Taken
    val errorText = (checkState as? UsernameCheck.Taken)?.message
    val busy = uiState.busy || checkState == UsernameCheck.Checking

    AuthStepScaffold(
        title = stringResource(R.string.profile_username_title),
        subtitle = stringResource(R.string.profile_username_subtitle),
        totalSteps = 6,
        stepIndex = 1,
        onBack = onBack,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = { vm.saveUsername(username) },
                enabled = available && !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        AppTextField(
            value = username,
            onValueChange = { username = it.replace(" ", "").lowercase() },
            label = stringResource(R.string.profile_username_label),
            placeholder = stringResource(R.string.profile_username_hint),
            leadingIcon = AppIcons.AtSign,
            isError = taken,
            supportingText = when {
                errorText != null -> errorText
                available -> stringResource(R.string.profile_username_available)
                else -> null
            },
            trailingContent = {
                when (checkState) {
                    UsernameCheck.Checking -> CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = c.textTertiary,
                        strokeWidth = 2.dp,
                    )
                    UsernameCheck.Available -> Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = c.success,
                    )
                    is UsernameCheck.Taken -> Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = c.error,
                    )
                    UsernameCheck.Idle -> {}
                }
            },
            autoFocus = true,
            imeAction = ImeAction.Done,
        )

        if (suggestions.isNotEmpty() && username.isEmpty()) {
            Spacer(Modifier.height(PulseSpacing.lg))
            Text(
                text = stringResource(R.string.profile_username_suggestions),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                suggestions.forEach { suggestion ->
                    AppChip(
                        label = "@$suggestion",
                        selected = false,
                        onClick = { username = suggestion },
                    )
                }
            }
        }
    }
}
