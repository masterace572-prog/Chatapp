package com.pulse.messenger.ui.screens.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AuthStepScaffold
import com.pulse.messenger.ui.components.PermissionCard
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.auth.AuthViewModel
import com.pulse.messenger.ui.theme.PulseSpacing

/**
 * S16 - Permissions primer: one explainer card per permission with a real
 * Allow action (system dialog). Continue works regardless (PRD S16).
 */
@Composable
fun PermissionsScreen(
    vm: AuthViewModel,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    data class PermissionEntry(
        val permission: String,
        val icon: ImageVector,
        @StringRes val titleRes: Int,
        @StringRes val reasonRes: Int,
    )

    val entries = listOf(
        PermissionEntry(
            Manifest.permission.READ_CONTACTS,
            AppIcons.Users,
            R.string.perm_contacts_title,
            R.string.perm_contacts_reason,
        ),
        PermissionEntry(
            Manifest.permission.POST_NOTIFICATIONS,
            AppIcons.Bell,
            R.string.perm_notifications_title,
            R.string.perm_notifications_reason,
        ),
        PermissionEntry(
            Manifest.permission.RECORD_AUDIO,
            AppIcons.Mic,
            R.string.perm_microphone_title,
            R.string.perm_microphone_reason,
        ),
        PermissionEntry(
            Manifest.permission.CAMERA,
            AppIcons.Camera,
            R.string.perm_camera_title,
            R.string.perm_camera_reason,
        ),
    )

    val granted = remember { mutableStateMapOf<String, Boolean>() }
    var launcherTarget by remember { mutableStateOf<String?>(null) }

    fun isGranted(permission: String): Boolean = when {
        granted[permission] != null -> granted[permission] == true
        permission == Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < 33 -> true
        else -> ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { result ->
        launcherTarget?.let { granted[it] = result }
    }

    AuthStepScaffold(
        title = stringResource(R.string.permissions_title),
        subtitle = stringResource(R.string.permissions_subtitle),
        totalSteps = 6,
        stepIndex = 4,
        onBack = onBack,
        scrollable = false,
        cta = {
            AppButton(
                text = stringResource(R.string.common_continue),
                onClick = onContinue,
                enabled = !uiState.busy,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = PulseSpacing.lg),
        ) {
            entries.forEach { entry ->
                PermissionCard(
                    icon = entry.icon,
                    title = stringResource(entry.titleRes),
                    reason = stringResource(entry.reasonRes),
                    granted = isGranted(entry.permission),
                    onAllow = {
                        launcherTarget = entry.permission
                        launcher.launch(entry.permission)
                    },
                    modifier = Modifier.padding(bottom = PulseSpacing.md),
                )
            }
        }
    }
}
