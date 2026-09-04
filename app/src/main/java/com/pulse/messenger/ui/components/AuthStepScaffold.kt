package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Shared scaffold for the one-question-per-screen auth steps (PRD §6.2):
 * back arrow, thin segmented step progress, headline title + supporting text,
 * scrollable content and a bottom-pinned primary CTA above the IME.
 */
@Composable
fun AuthStepScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    totalSteps: Int = 1,
    stepIndex: Int = 0,
    onBack: (() -> Unit)? = null,
    /** Set false when the content manages its own scrolling (e.g. LazyColumn). */
    scrollable: Boolean = true,
    cta: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = PulseTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .imePadding(),
    ) {
        // Top bar: back (optional) + segmented progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = PulseSpacing.screen),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.width(PulseSizes.minTouchTarget),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (onBack != null) {
                    AppBackButton(onBack = onBack)
                }
            }
            if (totalSteps > 1) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = PulseSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(totalSteps) { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index <= stepIndex) c.accent else c.surfaceVariant,
                                ),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = PulseSpacing.screen),
        ) {
            Spacer(Modifier.height(PulseSpacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = c.textPrimary,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(PulseSpacing.sm))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                )
            }
            Spacer(Modifier.height(PulseSpacing.xxxl))
            if (scrollable) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    content = content,
                )
                Spacer(Modifier.height(PulseSpacing.xxl))
            } else {
                Column(Modifier.weight(1f), content = content)
            }
        }

        if (cta != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = PulseSpacing.screen)
                    .padding(top = PulseSpacing.sm, bottom = PulseSpacing.lg),
                content = cta,
            )
        }
    }
}
