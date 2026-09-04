package com.pulse.messenger.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppButtonVariant
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S15 - Bio (optional): multiline field with a 0/140 character counter,
 * Skip for now + Continue.
 */
@Composable
fun ProfileBioScreen(
    vm: AuthViewModel,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var bio by remember { mutableStateOf("") }
    val max = 140

    AuthStepScaffold(
        title = stringResource(R.string.profile_bio_title),
        subtitle = stringResource(R.string.profile_bio_subtitle),
        totalSteps = 6,
        stepIndex = 3,
        onBack = onBack,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = { vm.saveBio(bio) },
                enabled = !uiState.busy,
                loading = uiState.busy,
            )
            Text(
                text = stringResource(R.string.profile_bio_skip),
                style = MaterialTheme.typography.labelLarge,
                color = c.textSecondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onSkip)
                    .padding(vertical = PulseSpacing.md),
            )
        },
    ) {
        AppTextField(
            value = bio,
            onValueChange = { bio = it.take(max) },
            placeholder = stringResource(R.string.profile_bio_placeholder),
            singleLine = false,
            maxLines = 4,
            imeAction = ImeAction.Default,
        )
        Spacer(Modifier.height(PulseSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_bio_optional),
                style = MaterialTheme.typography.labelMedium,
                color = c.textTertiary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${bio.length}/$max",
                style = MaterialTheme.typography.labelMedium,
                color = c.textTertiary,
            )
        }
    }
}
