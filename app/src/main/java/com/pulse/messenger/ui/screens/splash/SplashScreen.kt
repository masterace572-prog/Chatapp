package com.pulse.messenger.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.ui.components.PulseLogoMark
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S01 - Splash. Full-bleed background, centered brand mark + name,
 * ~1.2s before routing (PRD S01; the Android 12 splash is the OS-level layer).
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onFinished: (target: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.Done) onFinished((uiState as SplashUiState.Done).target)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PulseLogoMark(size = 72.dp)
            Spacer(Modifier.height(PulseSpacing.lg))
            Text(
                text = "Pulse",
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary,
            )
        }
    }
}
