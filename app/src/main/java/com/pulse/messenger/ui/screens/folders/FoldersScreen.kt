package com.pulse.messenger.ui.screens.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.ChatFolder
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.ui.components.AppBackButton
import com.pulse.messenger.ui.components.AppBottomSheet
import com.pulse.messenger.ui.components.AppButton
import com.pulse.messenger.ui.components.AppChip
import com.pulse.messenger.ui.components.AppDivider
import com.pulse.messenger.ui.components.AppIconButton
import com.pulse.messenger.ui.components.AppSwitch
import com.pulse.messenger.ui.components.AppTextField
import com.pulse.messenger.ui.components.AppTopBar
import com.pulse.messenger.ui.components.ConfirmDialog
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S22 - Chat folders editor: reorder/edit/delete/create folders.
 * Reordering uses stable Up/Down controls (drag-handle is visual only - the
 * project deliberately avoids unstable drag-and-drop dependencies), persisted
 * through the FoldersRepository (DataStore JSON).
 */
@Composable
fun FoldersScreen(
    vm: FoldersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = PulseTheme.colors

    var editorDraft by remember { mutableStateOf<FolderDraft?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background),
    ) {
        AppTopBar(
            title = stringResource(R.string.folders_title),
            navigationIcon = { AppBackButton(onBack = onBack) },
        )

        if (uiState.loading) {
            Box(Modifier.fillMaxSize())
        } else if (uiState.folders.isEmpty()) {
            EmptyState(
                icon = AppIcons.FolderPlus,
                title = stringResource(R.string.folders_empty_title),
                subtitle = stringResource(R.string.folders_empty_subtitle),
                actionLabel = stringResource(R.string.folders_new),
                onAction = { showCreate = true },
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                item(key = "folders-info") {
                    Text(
                        text = stringResource(
                            R.string.folders_include_note,
                            uiState.folders.size,
                            uiState.totalChats,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                        modifier = Modifier.padding(
                            horizontal = PulseSpacing.screen,
                            vertical = PulseSpacing.md,
                        ),
                    )
                }
                itemsIndexed(
                    items = uiState.folders,
                    key = { _, folder -> folder.id },
                ) { index, folder ->
                    FolderRow(
                        folder = folder,
                        chatCount = uiState.includeCounts[folder.id] ?: 0,
                        canMoveUp = index > 0,
                        canMoveDown = index < uiState.folders.lastIndex,
                        onMoveUp = { vm.moveUp(folder.id) },
                        onMoveDown = { vm.moveDown(folder.id) },
                        onEdit = { editorDraft = folder.toDraft() },
                        onDelete = { vm.requestDelete(folder.id) },
                    )
                    AppDivider(insetStart = PulseSpacing.screen)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PulseSpacing.screen)
                .padding(top = PulseSpacing.sm, bottom = PulseSpacing.lg),
            horizontalArrangement = Arrangement.Center,
        ) {
            AppButton(
                text = stringResource(R.string.folders_new),
                leadingIcon = AppIcons.Plus,
                onClick = { showCreate = true },
                fillMaxWidth = false,
            )
        }
    }

    val sheetDraft = if (showCreate) FolderDraft() else editorDraft
    if (sheetDraft != null) {
        FolderEditorSheet(
            draft = sheetDraft,
            onDismiss = {
                showCreate = false
                editorDraft = null
            },
            onSave = { name, kinds, onlyUnread ->
                if (sheetDraft.id == null) {
                    vm.createFolder(name, kinds, onlyUnread)
                } else {
                    vm.updateFolder(
                        ChatFolder(
                            id = sheetDraft.id,
                            name = name,
                            includeKinds = kinds,
                            onlyUnread = onlyUnread,
                        ),
                    )
                }
                showCreate = false
                editorDraft = null
            },
        )
    }

    val pendingDeleteId = uiState.pendingDeleteId
    val pendingFolder = uiState.folders.firstOrNull { it.id == pendingDeleteId }
    if (pendingDeleteId != null && pendingFolder != null) {
        ConfirmDialog(
            title = stringResource(R.string.folders_delete_title),
            text = stringResource(R.string.folders_delete_text, pendingFolder.name),
            confirmLabel = stringResource(R.string.common_delete),
            onConfirm = vm::confirmDelete,
            onDismiss = vm::cancelDelete,
        )
    }
}

@Composable
private fun FolderRow(
    folder: ChatFolder,
    chatCount: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PulseSpacing.md,
                end = PulseSpacing.sm,
                top = PulseSpacing.sm,
                bottom = PulseSpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Drag-handle visual; reorder is intentionally via the stable arrows.
        Icon(
            imageVector = AppIcons.GripVertical,
            contentDescription = null,
            modifier = Modifier
                .size(PulseIconSizes.default)
                .padding(horizontal = PulseSpacing.xs),
            tint = c.textTertiary,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = PulseSpacing.sm),
        ) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    R.string.folders_subtitle,
                    chatCount,
                    folderRulesLabel(folder),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ReorderButton(
            icon = AppIcons.ChevronUp,
            description = stringResource(R.string.folders_move_up),
            enabled = canMoveUp,
            onClick = onMoveUp,
        )
        ReorderButton(
            icon = AppIcons.ChevronDown,
            description = stringResource(R.string.folders_move_down),
            enabled = canMoveDown,
            onClick = onMoveDown,
        )
        AppIconButton(
            icon = AppIcons.Pencil,
            contentDescription = stringResource(R.string.common_edit),
            onClick = onEdit,
            tint = c.textSecondary,
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

@Composable
private fun ReorderButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    AppIconButton(
        icon = icon,
        contentDescription = description,
        onClick = onClick,
        enabled = enabled,
        tint = if (enabled) PulseTheme.colors.textSecondary else PulseTheme.colors.textTertiary,
        iconSize = PulseIconSizes.inline,
    )
}

/** Bottom-sheet form to create or edit a folder (S22). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderEditorSheet(
    draft: FolderDraft,
    onDismiss: () -> Unit,
    onSave: (name: String, kinds: Set<ChatKind>, onlyUnread: Boolean) -> Unit,
) {
    val c = PulseTheme.colors
    var name by remember(draft.id) { mutableStateOf(draft.name) }
    var includeGroups by remember(draft.id) { mutableStateOf(draft.includeGroups) }
    var includePersonal by remember(draft.id) { mutableStateOf(draft.includePersonal) }
    var onlyUnread by remember(draft.id) { mutableStateOf(draft.onlyUnread) }

    val valid = name.isNotBlank() && (includeGroups || includePersonal)

    AppBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PulseSpacing.screen),
        ) {
            Text(
                text = stringResource(if (draft.id == null) R.string.folders_new else R.string.folders_edit),
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(PulseSpacing.lg))
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.folders_name),
                placeholder = stringResource(R.string.folders_name_placeholder),
                singleLine = true,
            )
            Spacer(Modifier.height(PulseSpacing.lg))
            Text(
                text = stringResource(R.string.folders_include),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm)) {
                AppChip(
                    label = stringResource(R.string.folders_kind_groups),
                    selected = includeGroups,
                    onClick = { includeGroups = !includeGroups },
                    leadingIcon = AppIcons.Users,
                    shape = PulseShapes.full,
                )
                AppChip(
                    label = stringResource(R.string.folders_kind_personal),
                    selected = includePersonal,
                    onClick = { includePersonal = !includePersonal },
                    leadingIcon = AppIcons.User,
                    shape = PulseShapes.full,
                )
            }
            Spacer(Modifier.height(PulseSpacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.folders_unread_only),
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textPrimary,
                    )
                    Text(
                        text = stringResource(R.string.folders_unread_only_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textTertiary,
                    )
                }
                AppSwitch(
                    checked = onlyUnread,
                    onCheckedChange = { onlyUnread = it },
                )
            }
            Spacer(Modifier.height(PulseSpacing.xxl))
            AppButton(
                text = stringResource(R.string.common_save),
                onClick = {
                    onSave(
                        name.trim(),
                        buildSet {
                            if (includeGroups) add(ChatKind.Group)
                            if (includePersonal) add(ChatKind.Direct)
                        },
                        onlyUnread,
                    )
                },
                enabled = valid,
                fillMaxWidth = true,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
        }
    }
}

private fun ChatFolder.toDraft(): FolderDraft =
    FolderDraft(
        id = id,
        name = name,
        includeGroups = ChatKind.Group in includeKinds,
        includePersonal = ChatKind.Direct in includeKinds,
        onlyUnread = onlyUnread,
    )

/** Localized one-line summary of a folder's rules (Groups/Personal/Unread). */
@Composable
private fun folderRulesLabel(folder: ChatFolder): String = buildList {
    if (ChatKind.Group in folder.includeKinds) add(stringResource(R.string.folders_kind_groups))
    if (ChatKind.Direct in folder.includeKinds) add(stringResource(R.string.folders_kind_personal))
    if (folder.onlyUnread) add(stringResource(R.string.folders_unread_only))
}.joinToString(" \u00b7 ").ifEmpty { "" }
