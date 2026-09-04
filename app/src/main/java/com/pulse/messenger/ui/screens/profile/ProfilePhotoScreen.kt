package com.pulse.messenger.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppButtonVariant
import com.pulse.messenger.ui.components.AppBottomSheet
import com.pulse.messenger.ui.components.AppIconButton
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.Avatar
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthSnackbar
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S14 - Profile photo: options sheet with Take photo / Choose from gallery /
 * Choose an avatar (grid of initial-based muted tones) / Remove. Camera and
 * gallery open with the media milestone; the avatar palette is fully usable.
 */
@Composable
fun ProfilePhotoScreen(
    vm: AuthViewModel,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showOptions by remember { mutableStateOf(false) }
    var chosenSeed by remember { mutableStateOf<Int?>(vm.googleAvatarSeed) }

    val firstName = vm.googleFirstName ?: "You"
    val avatarSeed = chosenSeed ?: firstName.hashCode()
    val avatarTone = AvatarTones.bySeed(avatarSeed)

    AuthStepScaffold(
        title = stringResource(R.string.profile_photo_title),
        subtitle = stringResource(R.string.profile_photo_subtitle),
        totalSteps = 6,
        stepIndex = 2,
        onBack = onBack,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = {
                    if (chosenSeed != null && chosenSeed != vm.googleAvatarSeed) {
                        vm.saveAvatarSeed(chosenSeed!!, advance = true)
                    } else {
                        onSkip()
                    }
                },
                enabled = !uiState.busy,
                loading = uiState.busy,
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Avatar(
                    name = firstName,
                    avatarTone = avatarTone,
                    size = 120.dp,
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(c.surface)
                        .clickable { showOptions = true }
                        .padding(0.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.Camera,
                        contentDescription = stringResource(R.string.profile_photo_edit),
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = c.accent,
                    )
                }
            }
            Spacer(Modifier.height(PulseSpacing.xxl))
            AppButton(
                text = stringResource(R.string.profile_photo_choose_avatar),
                onClick = { showOptions = true },
                variant = AppButtonVariant.Secondary,
                fillMaxWidth = false,
            )
            Spacer(Modifier.height(PulseSpacing.xs))
            AppButton(
                text = stringResource(R.string.profile_photo_skip),
                onClick = onSkip,
                variant = AppButtonVariant.Tertiary,
                fillMaxWidth = false,
            )
        }
    }

    if (showOptions) {
        PhotoOptionsSheet(
            firstName = firstName,
            selectedSeed = avatarSeed,
            onPickAvatar = { seed ->
                chosenSeed = seed
                showOptions = false
            },
            onCamera = {
                showOptions = false
                vm.notice(AuthSnackbar.ComingLater)
            },
            onGallery = {
                showOptions = false
                vm.notice(AuthSnackbar.ComingLater)
            },
            onRemove = {
                chosenSeed = null
                showOptions = false
            },
            onDismiss = { showOptions = false },
        )
    }
}

/** S14 options bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoOptionsSheet(
    firstName: String,
    selectedSeed: Int,
    onPickAvatar: (Int) -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = PulseTheme.colors
    AppBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.profile_photo_title),
            style = MaterialTheme.typography.titleLarge,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(PulseSpacing.md))
        Text(
            text = stringResource(R.string.profile_photo_avatar_picker_title),
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
        )
        Spacer(Modifier.height(PulseSpacing.md))
        // Avatar palette grid
        Column(verticalArrangement = Arrangement.spacedBy(PulseSpacing.md)) {
            AvatarTones.All.chunked(4).forEach { rowTones ->
                Row(horizontalArrangement = Arrangement.spacedBy(PulseSpacing.md)) {
                    rowTones.forEach { tone ->
                        val seed = AvatarTones.All.indexOf(tone)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .then(
                                    if (selectedSeed == seed) {
                                        Modifier.background(c.border)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { onPickAvatar(seed) }
                                .padding(3.dp),
                        ) {
                            Avatar(
                                name = firstName,
                                avatarTone = tone,
                                size = 56.dp,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(PulseSpacing.lg))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconButton(
                icon = AppIcons.Camera,
                contentDescription = stringResource(R.string.profile_photo_take),
                onClick = onCamera,
                containerColor = c.accentContainer,
                tint = c.accent,
            )
            Spacer(Modifier.width(PulseSpacing.md))
            AppIconButton(
                icon = AppIcons.Image,
                contentDescription = stringResource(R.string.profile_photo_gallery),
                onClick = onGallery,
                containerColor = c.accentContainer,
                tint = c.accent,
            )
            Spacer(Modifier.width(PulseSpacing.md))
            Text(
                text = stringResource(R.string.profile_photo_remove),
                style = MaterialTheme.typography.labelLarge,
                color = c.error,
                modifier = Modifier
                    .clickable(onClick = onRemove)
                    .padding(PulseSpacing.md),
            )
        }
    }
}
