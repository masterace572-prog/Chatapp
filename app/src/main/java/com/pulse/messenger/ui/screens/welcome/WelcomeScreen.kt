package com.pulse.messenger.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.data.mock.SeedData
import com.pulse.messenger.domain.model.GoogleAccount
import com.pulse.messenger.ui.components.AppBottomSheet
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppButtonVariant
import com.pulse.messenger.ui.components.Avatar
import com.pulse.messenger.ui.components.PulseLogoMark
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.icons.GoogleIcon
import com.pulse.messenger.ui.screens.auth.AuthSnackbar
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S02 - Welcome. Hero on the upper area, three stacked actions and a legal
 * footer. "Continue with Google" opens the mock account picker (S05).
 */
@Composable
fun WelcomeScreen(
    vm: AuthViewModel,
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    var showAccountSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PulseSpacing.screen)
                .padding(top = PulseSpacing.huge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero (roughly the upper half)
            Spacer(Modifier.height(PulseSpacing.huge))
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(c.accentContainer),
                contentAlignment = Alignment.Center,
            ) {
                PulseLogoMark(size = 84.dp)
            }
            Spacer(Modifier.height(PulseSpacing.xxxl))
            Text(
                text = stringResource(R.string.welcome_headline),
                style = MaterialTheme.typography.displayLarge,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(PulseSpacing.md))
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = c.textSecondary,
            )
            Spacer(Modifier.height(PulseSpacing.colossal))

            // Actions
            AppButton(
                text = stringResource(R.string.welcome_google),
                onClick = { showAccountSheet = true },
                variant = AppButtonVariant.Secondary,
                loading = uiState.busy,
                leadingContent = {
                    Icon(
                        imageVector = GoogleIcon.G,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
            Spacer(Modifier.height(PulseSpacing.md))
            AppButton(
                text = stringResource(R.string.welcome_login),
                onClick = onLogin,
            )
            Spacer(Modifier.height(PulseSpacing.xs))
            AppButton(
                text = stringResource(R.string.welcome_create),
                onClick = onCreateAccount,
                variant = AppButtonVariant.Tertiary,
            )

            if (uiState.error != null) {
                Spacer(Modifier.height(PulseSpacing.md))
                Text(
                    text = uiState.error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.error,
                )
            }

            Spacer(Modifier.height(PulseSpacing.huge))

            // Legal footer
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.welcome_terms_prefix))
                    withStyle(SpanStyle(color = c.accent)) {
                        pushStringAnnotation("link", "terms")
                        append(stringResource(R.string.welcome_terms))
                    }
                    append(stringResource(R.string.welcome_terms_and))
                    withStyle(SpanStyle(color = c.accent)) {
                        pushStringAnnotation("link", "privacy")
                        append(stringResource(R.string.welcome_privacy))
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = c.textTertiary,
                modifier = Modifier
                    .clickable {
                        // Tap target is the whole footer for accessibility.
                        vm.notice(AuthSnackbar.ComingLater)
                    }
                    .padding(PulseSpacing.sm),
            )
            Spacer(Modifier.height(PulseSpacing.lg))
        }
    }

    if (showAccountSheet) {
        GoogleAccountSheet(
            onDismiss = { showAccountSheet = false },
            onUseAnother = {
                showAccountSheet = false
                vm.notice(AuthSnackbar.ComingLater)
            },
            onSelect = { account ->
                showAccountSheet = false
                vm.signInWithGoogle(account)
            },
        )
    }
}

/**
 * S05 - Mock Google account chooser: two accounts + "Use another account".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleAccountSheet(
    onDismiss: () -> Unit,
    onSelect: (GoogleAccount) -> Unit,
    onUseAnother: () -> Unit = onDismiss,
) {
    val c = PulseTheme.colors
    AppBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.welcome_account_picker_title),
            style = MaterialTheme.typography.titleLarge,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(PulseSpacing.md))
        SeedData.googleAccounts.forEach { account ->
            Surface(
                onClick = { onSelect(account) },
                color = Color.Transparent,
                shape = PulseShapes.md,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = PulseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(
                        name = account.name,
                        avatarTone = AvatarTones.bySeed(account.avatarSeed),
                        size = 44.dp,
                    )
                    Spacer(Modifier.width(PulseSpacing.lg))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textPrimary,
                        )
                        Text(
                            text = account.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(PulseSpacing.xs))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PulseShapes.md)
                .clickable { onUseAnother() }
                .padding(vertical = PulseSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(c.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(PulseIconSizes.default),
                    tint = c.textSecondary,
                )
            }
            Spacer(Modifier.width(PulseSpacing.lg))
            Text(
                text = stringResource(R.string.welcome_other_account),
                style = MaterialTheme.typography.bodyLarge,
                color = c.textSecondary,
            )
        }
    }
}
