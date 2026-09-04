package com.pulse.messenger.ui.screens.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.data.mock.SeedData
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.Avatar
import com.pulse.messenger.ui.components.Tag
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthSnackbar
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S17 - Find friends: contacts already on Pulse (Add) and an invite section.
 * Continue finishes onboarding (mock profile) and shows the account success.
 */
@Composable
fun FindFriendsScreen(
    vm: AuthViewModel,
    onBack: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val addedIds = remember { mutableStateOf(setOf<String>()) }
    val onPulse = SeedData.contacts.take(6)

    // Mock phone contacts not on Pulse yet.
    val invitees = remember {
        listOf(
            User("p-1", "Sara", "Iyer", "", avatarSeed = 2),
            User("p-2", "Ravi", "Menon", "", avatarSeed = 4),
            User("p-3", "Lena", "Fischer", "", avatarSeed = 6),
        )
    }

    fun addContact(user: User) {
        addedIds.value = addedIds.value + user.id
        vm.notice(AuthSnackbar.AddedToContacts)
    }

    AuthStepScaffold(
        title = stringResource(R.string.friends_title),
        subtitle = stringResource(R.string.friends_subtitle),
        totalSteps = 6,
        stepIndex = 5,
        onBack = onBack,
        scrollable = false,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = { vm.completeOnboarding() },
                enabled = !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            item {
                SectionLabel(stringResource(R.string.friends_section_on_pulse))
            }
            items(onPulse, key = { it.id }) { user ->
                ContactRow(
                    user = user,
                    actionLabel = if (addedIds.value.contains(user.id)) null else "Add",
                    actionEnabled = !addedIds.value.contains(user.id),
                    onAction = { addContact(user) },
                )
            }
            item {
                Spacer(Modifier.height(PulseSpacing.sm))
                SectionLabel(stringResource(R.string.friends_section_invite))
            }
            items(invitees, key = { it.id }) { user ->
                ContactRow(
                    user = user,
                    actionLabel = stringResource(R.string.friends_invite),
                    actionEnabled = true,
                    onAction = { vm.notice(AuthSnackbar.InviteSent) },
                )
            }
            item { Spacer(Modifier.height(PulseSpacing.lg)) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val c = PulseTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = c.textSecondary,
        modifier = Modifier.padding(vertical = PulseSpacing.sm),
    )
}

@Composable
private fun ContactRow(
    user: User,
    actionLabel: String?,
    actionEnabled: Boolean,
    onAction: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            name = user.displayName,
            avatarTone = AvatarTones.bySeed(user.avatarSeed),
            size = 48.dp,
        )
        Spacer(Modifier.width(PulseSpacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = c.textPrimary,
            )
            if (user.username.isNotBlank()) {
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                )
            }
        }
        when {
            actionLabel == null -> Tag(
                text = stringResource(R.string.friends_added),
                containerColor = c.success.copy(alpha = 0.14f),
                contentColor = c.success,
            )
            actionEnabled -> Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = c.accent,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
            )
        }
    }
}
