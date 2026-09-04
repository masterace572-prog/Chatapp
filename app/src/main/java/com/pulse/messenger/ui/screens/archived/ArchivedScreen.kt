package com.pulse.messenger.ui.screens.archived

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppBackButton
import com.pulse.messenger.ui.components.AppDivider
import com.pulse.messenger.ui.components.AppTopBar
import com.pulse.messenger.ui.components.ChatListItem
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.components.SwipeAction
import com.pulse.messenger.ui.components.SwipeableRow
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S21 - Archived chats: reusable ChatListItem rows, swipe right to unarchive,
 * empty state when the archive is empty.
 */
@Composable
fun ArchivedScreen(
    vm: ArchivedViewModel,
    onOpenChat: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = PulseTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background),
    ) {
        AppTopBar(
            title = stringResource(R.string.chats_archived),
            navigationIcon = { AppBackButton(onBack = onBack) },
        )

        when {
            uiState.loading -> Box(Modifier.fillMaxSize())
            uiState.chats.isEmpty() -> EmptyState(
                icon = AppIcons.Archive,
                title = stringResource(R.string.archived_empty_title),
                subtitle = stringResource(R.string.archived_empty_subtitle),
                modifier = Modifier.weight(1f),
            )
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(uiState.chats, key = { it.chatId }) { summary ->
                    val action = SwipeAction(
                        label = stringResource(R.string.archived_unarchive),
                        icon = AppIcons.ArchiveRestore,
                        background = c.surfaceVariant,
                        contentColor = c.textSecondary,
                        onTrigger = { vm.unarchive(summary.chatId) },
                    )
                    SwipeableRow(
                        startAction = action,
                        enabled = true,
                    ) {
                        ChatListItem(
                            summary = summary,
                            onClick = { onOpenChat(summary.chatId) },
                        )
                    }
                    AppDivider(insetStart = PulseSizes.chatRowDividerInset)
                }
            }
        }
    }
}
