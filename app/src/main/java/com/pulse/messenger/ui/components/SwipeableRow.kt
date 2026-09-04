package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/** One swipe-revealed action: icon + label on a muted tinted background. */
data class SwipeAction(
    val label: String,
    val icon: ImageVector,
    val background: Color,
    val contentColor: Color,
    val onTrigger: () -> Unit,
)

/**
 * Row with swipe actions (PRD S19/S21): swipe right (start-to-end) reveals the
 * [endAction], swipe left reveals the [startAction]. Both directions snap back
 * after triggering - the repository mutation removes/updates the row. Swipes
 * are disabled while [selectionMode] is active.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableRow(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    startAction: SwipeAction? = null,
    endAction: SwipeAction? = null,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> startAction?.onTrigger()
                SwipeToDismissBoxValue.EndToStart -> endAction?.onTrigger()
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false // snap back; the repository mutation performs the real change
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = enabled && startAction != null,
        enableDismissFromEndToStart = enabled && endAction != null,
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> startAction?.let { action ->
                    SwipeBackground(
                        action = action,
                        alignEnd = false,
                    )
                }
                SwipeToDismissBoxValue.EndToStart -> endAction?.let { action ->
                    SwipeBackground(
                        action = action,
                        alignEnd = true,
                    )
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
    ) {
        content()
    }
}

@Composable
private fun SwipeBackground(action: SwipeAction, alignEnd: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(action.background)
            .padding(horizontal = PulseSpacing.xl),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.default),
                tint = action.contentColor,
            )
            Spacer(Modifier.width(PulseSpacing.sm))
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelLarge,
                color = action.contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
