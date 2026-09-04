package com.pulse.messenger.ui.screens.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.ui.components.AppChip
import com.pulse.messenger.ui.components.AppDivider
import com.pulse.messenger.ui.components.AppIconButton
import com.pulse.messenger.ui.components.ChatListItem
import com.pulse.messenger.ui.components.ConfirmDialog
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.components.SearchBar
import com.pulse.messenger.ui.components.SwipeAction
import com.pulse.messenger.ui.components.SwipeableRow
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/** Outcomes of the "More" menu; the host decides navigation/snackbars. */
enum class ChatsMoreAction { NewGroup, NewBroadcast, LinkedDevices, StarredMessages, Settings }

/**
 * S19 - Chats tab body (hosted inside the main scaffold; the FAB and its
 * callbacks belong to the S18 shell so the tab stays reusable).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab(
    vm: ChatsViewModel,
    onOpenChat: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenArchived: () -> Unit,
    onOpenFolders: () -> Unit,
    onNewChat: () -> Unit,
    onMoreAction: (ChatsMoreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = PulseTheme.colors

    Column(Modifier.fillMaxSize()) {
        if (uiState.selectionMode) {
        SelectionTopBar(
            count = uiState.selection.size,
            onClose = vm::clearSelection,
            onMarkRead = vm::markSelectedRead,
            onArchive = vm::archiveSelected,
            onPin = { vm.pinSelected(true) },
            onMute = { vm.muteSelected(true) },
            onDelete = vm::requestDeleteSelected,
        )
    } else {
        ChatsHeader(
            uiState = uiState,
            onSearchTap = onOpenSearch,
            onMoreAction = onMoreAction,
            onFilterSelected = vm::selectFilter,
            onFolderSelected = vm::selectFolder,
            onEditFolders = onOpenFolders,
        )
        if (uiState.archivedCount > 0) {
            ArchivedRow(
                count = uiState.archivedCount,
                onClick = onOpenArchived,
            )
            AppDivider(insetStart = PulseSpacing.screen)
        }
    }

    Box(Modifier.weight(1f).fillMaxWidth()) {
        when {
            uiState.loading -> ChatListSkeleton()
            uiState.filteredChats.isEmpty() -> EmptyState(
                icon = AppIcons.MessageCircle,
                title = stringResource(R.string.chats_empty_title),
                subtitle = stringResource(R.string.chats_empty_subtitle),
                actionLabel = stringResource(R.string.chats_empty_cta),
                onAction = onNewChat,
                modifier = Modifier.fillMaxSize(),
            )
            else -> PullToRefreshBox(
                isRefreshing = uiState.refreshing,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = PulseSizes.fabClearance,
                    ),
                ) {
                    items(
                        items = uiState.filteredChats,
                        key = { it.chatId },
                    ) { summary ->
                        SwipeableChatRow(
                            summary = summary,
                            selectionMode = uiState.selectionMode,
                            selected = summary.chatId in uiState.selection,
                            onClick = {
                                if (uiState.selectionMode) vm.toggleSelection(summary.chatId)
                                else onOpenChat(summary.chatId)
                            },
                            onLongClick = { vm.toggleSelection(summary.chatId) },
                            onArchive = { vm.archive(summary.chatId) },
                            onTogglePin = { vm.togglePin(summary.chatId) },
                        )
                        AppDivider(insetStart = PulseSizes.chatRowDividerInset)
                    }
                }
            }
        }
    }
    }

    if (uiState.askDeleteConfirmation) {
        ConfirmDialog(
            title = stringResource(R.string.chats_delete_title),
            text = stringResource(
                R.string.chats_delete_message,
                uiState.selection.size,
            ),
            confirmLabel = stringResource(R.string.common_delete),
            onConfirm = vm::confirmDeleteSelected,
            onDismiss = vm::cancelDelete,
        )
    }
}

/** One row with its swipe actions (archive right, pin left). */
@Composable
private fun SwipeableChatRow(
    summary: ChatSummary,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onArchive: () -> Unit,
    onTogglePin: () -> Unit,
) {
    val c = PulseTheme.colors
    val archiveAction = SwipeAction(
        label = stringResource(R.string.chats_archive),
        icon = AppIcons.Archive,
        background = c.surfaceVariant,
        contentColor = c.textSecondary,
        onTrigger = onArchive,
    )
    val pinAction = SwipeAction(
        label = if (summary.isPinned) {
            stringResource(R.string.chats_unpin)
        } else {
            stringResource(R.string.chats_pin)
        },
        icon = if (summary.isPinned) AppIcons.PinOff else AppIcons.Pin,
        background = c.accentContainer,
        contentColor = c.onAccentContainer,
        onTrigger = onTogglePin,
    )
    SwipeableRow(
        startAction = if (selectionMode) null else archiveAction,
        endAction = if (selectionMode) null else pinAction,
        enabled = !selectionMode,
    ) {
        ChatListItem(
            summary = summary,
            onClick = onClick,
            onLongClick = onLongClick,
            selectionMode = selectionMode,
            selected = selected,
        )
    }
}

/** Header block: title + actions, collapsible search bar, filter chips. */
@Composable
private fun ChatsHeader(
    uiState: ChatsUiState,
    onSearchTap: () -> Unit,
    onMoreAction: (ChatsMoreAction) -> Unit,
    onFilterSelected: (ChatFilterKind) -> Unit,
    onFolderSelected: (String) -> Unit,
    onEditFolders: () -> Unit,
) {
    val c = PulseTheme.colors
    var moreOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = PulseSpacing.screen),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.tab_chats),
                style = MaterialTheme.typography.headlineMedium,
                color = c.textPrimary,
                modifier = Modifier.weight(1f),
            )
            AppIconButton(
                icon = AppIcons.Search,
                contentDescription = stringResource(R.string.common_search),
                onClick = onSearchTap,
            )
            Box {
                AppIconButton(
                    icon = AppIcons.EllipsisVertical,
                    contentDescription = stringResource(R.string.common_more),
                    onClick = { moreOpen = true },
                )
                DropdownMenu(
                    expanded = moreOpen,
                    onDismissRequest = { moreOpen = false },
                    containerColor = c.surface,
                ) {
                    ChatsMoreAction.entries.forEach { action ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (action) {
                                        ChatsMoreAction.NewGroup -> stringResource(R.string.chats_menu_new_group)
                                        ChatsMoreAction.NewBroadcast -> stringResource(R.string.chats_menu_new_broadcast)
                                        ChatsMoreAction.LinkedDevices -> stringResource(R.string.chats_menu_linked_devices)
                                        ChatsMoreAction.StarredMessages -> stringResource(R.string.chats_menu_starred)
                                        ChatsMoreAction.Settings -> stringResource(R.string.chats_menu_settings)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            onClick = {
                                moreOpen = false
                                onMoreAction(action)
                            },
                        )
                    }
                }
            }
        }

        Column(Modifier.padding(horizontal = PulseSpacing.screen)) {
            SearchBar(
                value = "",
                onValueChange = {},
                active = false,
                onActiveChange = { if (it) onSearchTap() },
            )
            Spacer(Modifier.height(PulseSpacing.md))
            FilterChipsRow(
                state = uiState,
                onFilterSelected = onFilterSelected,
                onFolderSelected = onFolderSelected,
                onEditFolders = onEditFolders,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
        }
    }
}

/** Horizontally scrollable All/Unread/Groups/Personal + folders chips. */
@Composable
private fun FilterChipsRow(
    state: ChatsUiState,
    onFilterSelected: (ChatFilterKind) -> Unit,
    onFolderSelected: (String) -> Unit,
    onEditFolders: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatFilterKind.entries.forEach { kind ->
            AppChip(
                label = stringResource(
                    when (kind) {
                        ChatFilterKind.All -> R.string.chats_filter_all
                        ChatFilterKind.Unread -> R.string.chats_filter_unread
                        ChatFilterKind.Groups -> R.string.chats_filter_groups
                        ChatFilterKind.Personal -> R.string.chats_filter_personal
                    },
                ),
                selected = state.activeFolderId == null && state.filterKind == kind,
                onClick = { onFilterSelected(kind) },
            )
        }
        state.folders.forEach { folder ->
            AppChip(
                label = folder.name,
                selected = state.activeFolderId == folder.id,
                onClick = { onFolderSelected(folder.id) },
            )
        }
        AppChip(
            label = stringResource(R.string.chats_filter_edit),
            selected = false,
            onClick = onEditFolders,
            leadingIcon = AppIcons.Pencil,
        )
    }
}

/** Archived chats entry row at the top of the list (S19). */
@Composable
fun ArchivedRow(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.Archive,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.default),
            tint = c.textSecondary,
        )
        Spacer(Modifier.width(PulseSpacing.lg))
        Text(
            text = stringResource(R.string.chats_archived),
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = c.textTertiary,
        )
        Spacer(Modifier.width(PulseSpacing.sm))
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.inline),
            tint = c.textTertiary,
        )
    }
}

/** Multi-select top bar: count, close and bulk actions. */
@Composable
fun SelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit,
    onPin: () -> Unit,
    onMute: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = PulseSpacing.screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIconButton(
            icon = AppIcons.Close,
            contentDescription = stringResource(R.string.common_close),
            onClick = onClose,
            iconSize = PulseIconSizes.default,
        )
        Spacer(Modifier.width(PulseSpacing.sm))
        Text(
            text = stringResource(R.string.chats_selected_count, count),
            style = MaterialTheme.typography.titleMedium,
            color = c.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AppIconButton(
                icon = AppIcons.CheckCheck,
                contentDescription = stringResource(R.string.chats_mark_read),
                onClick = onMarkRead,
                iconSize = PulseIconSizes.inline,
            )
            AppIconButton(
                icon = AppIcons.Pin,
                contentDescription = stringResource(R.string.chats_pin),
                onClick = onPin,
                iconSize = PulseIconSizes.inline,
            )
            AppIconButton(
                icon = AppIcons.BellOff,
                contentDescription = stringResource(R.string.chats_mute),
                onClick = onMute,
                iconSize = PulseIconSizes.inline,
            )
            AppIconButton(
                icon = AppIcons.Archive,
                contentDescription = stringResource(R.string.chats_archive),
                onClick = onArchive,
                iconSize = PulseIconSizes.inline,
            )
            AppIconButton(
                icon = AppIcons.Trash,
                contentDescription = stringResource(R.string.common_delete),
                onClick = onDelete,
                tint = c.error,
                iconSize = PulseIconSizes.inline,
            )
        }
    }
}

/** Skeleton chat list shown during first load. */
@Composable
fun ChatListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        repeat(7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                com.pulse.messenger.ui.components.SkeletonCircle(
                    modifier = Modifier.size(PulseSizes.avatarChat),
                )
                Spacer(Modifier.width(PulseSpacing.lg))
                Column(Modifier.weight(1f)) {
                    com.pulse.messenger.ui.components.SkeletonBox(
                        modifier = Modifier.fillMaxWidth(0.4f).height(14.dp),
                        shape = PulseShapes.full,
                    )
                    Spacer(Modifier.height(6.dp))
                    com.pulse.messenger.ui.components.SkeletonBox(
                        modifier = Modifier.fillMaxWidth(0.75f).height(12.dp),
                        shape = PulseShapes.full,
                    )
                }
            }
        }
    }
}
