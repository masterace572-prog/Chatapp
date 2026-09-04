package com.pulse.messenger.ui.screens.conversation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.MessageType
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.components.AppBottomSheet
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.components.BubbleQuoteData
import com.pulse.messenger.ui.components.AttachmentTile
import com.pulse.messenger.ui.components.AttachmentTray
import com.pulse.messenger.ui.components.attachmentTileLabel
import com.pulse.messenger.ui.components.CameraSimOverlay
import com.pulse.messenger.ui.components.ChatHeader
import com.pulse.messenger.ui.components.ChatHeaderAction
import com.pulse.messenger.ui.components.ChatHeaderStatus
import com.pulse.messenger.ui.components.ComposerUiState
import com.pulse.messenger.ui.components.ConfirmDialog
import com.pulse.messenger.ui.components.DateSeparator
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.components.EmojiSheetContent
import com.pulse.messenger.ui.components.ForwardSheet
import com.pulse.messenger.ui.components.LocationPickerSheet
import com.pulse.messenger.ui.components.LongPressScrim
import com.pulse.messenger.ui.components.MessageActionItem
import com.pulse.messenger.ui.components.MessageActionSheet
import com.pulse.messenger.ui.components.MessageBubble
import com.pulse.messenger.ui.components.MessageComposer
import com.pulse.messenger.ui.components.MediaDraft
import com.pulse.messenger.ui.components.MediaItemDraft
import com.pulse.messenger.ui.components.MediaViewerOverlay
import com.pulse.messenger.ui.components.MediaSendSheet
import com.pulse.messenger.ui.components.MultiSelectTopBar
import com.pulse.messenger.ui.components.PinnedBanner
import com.pulse.messenger.ui.components.PinnedBannerData
import com.pulse.messenger.ui.components.PollComposerSheet
import com.pulse.messenger.ui.components.QuickReactionBar
import com.pulse.messenger.ui.components.ReactorUi
import com.pulse.messenger.ui.components.RecentMediaItem
import com.pulse.messenger.ui.components.ReactorsSheetContent
import com.pulse.messenger.ui.components.ViewerMediaItem
import com.pulse.messenger.ui.components.buildViewerMedia
import com.pulse.messenger.ui.components.SelectionCheck
import com.pulse.messenger.ui.components.SystemMessageRow
import com.pulse.messenger.ui.components.TypingIndicator
import com.pulse.messenger.ui.components.UnreadDivider
import com.pulse.messenger.ui.icons.AppIcons as Icons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.theme.senderToneTextColor
import com.pulse.messenger.ui.util.ConversationFormat
import com.pulse.messenger.ui.util.MessageLabels
import kotlinx.coroutines.launch

/** User events the screen forwards to the ViewModel. */
/** Open media-viewer session: chronological media + page to start on (M4c S33). */
private data class ViewerSession(val items: List<ViewerMediaItem>, val index: Int)

data class ConversationCallbacks(
    val onBack: () -> Unit = {},
    val onRetry: (String) -> Unit = {},
    val onDraftChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onToggleMuted: () -> Unit = {},
    val onScrolledToBottom: () -> Unit = {},
    val onScrolledUp: () -> Unit = {},
    val onUnreadDividerPassed: () -> Unit = {},
    val onAttachment: () -> Unit = {},
    val onCamera: () -> Unit = {},
    val onMicPress: () -> Unit = {},
    val onRecordCancel: () -> Unit = {},
    val onRecordLock: () -> Unit = {},
    val onRecordFinish: (Long, List<Int>) -> Unit = { _: Long, _: List<Int> -> },
    val onRecordTooShort: () -> Unit = {},
    val onCloseComposerBar: () -> Unit = {},
    val onUnblock: () -> Unit = {},
    val onVoiceCall: () -> Unit = {},
    val onVideoCall: () -> Unit = {},
)

/** S23 conversation screen (M4a core + M4b interactions). */
@Composable
fun ConversationScreen(
    vm: ConversationViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        vm.onScreenOpened()
        onDispose { vm.onScreenClosed() }
    }

    val callbacks = remember {
        ConversationCallbacks(
            onBack = onBack,
            onRetry = vm::retry,
            onDraftChange = vm::onDraftChange,
            onSend = vm::send,
            onToggleMuted = vm::toggleMuted,
            onScrolledToBottom = vm::scrolledToBottom,
            onScrolledUp = vm::scrolledUp,
            onUnreadDividerPassed = vm::unreadDividerPassed,
            onAttachment = { toastComingSoon(context, R.string.conversation_composer_attachment_cd) },
            onCamera = { toastComingSoon(context, R.string.conversation_composer_camera_cd) },
            onMicPress = vm::recordStart,
            onRecordCancel = vm::recordCancel,
            onRecordLock = vm::recordLock,
            onRecordFinish = { ms, samples -> vm.recordFinish(ms, samples) },
            onRecordTooShort = {
                Toast.makeText(
                    context,
                    context.getString(R.string.conversation_voice_too_short),
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onCloseComposerBar = vm::closeComposerBar,
            onUnblock = { vm.setBlocked(false) },
            onVoiceCall = { toastComingSoon(context, R.string.conversation_call_cd) },
            onVideoCall = { toastComingSoon(context, R.string.conversation_video_cd) },
        )
    }

    ConversationContent(
        state = state,
        callbacks = callbacks,
        onVm = VmBridge(
            retry = vm::retry,
            send = vm::send,
            sendImages = vm::sendImages,
            sendVideo = vm::sendVideo,
            sendPoll = vm::sendPoll,
            votePoll = vm::votePoll,
            retractVote = vm::retractVote,
            sendLocation = vm::sendLocation,
            toggleReaction = vm::toggleReaction,
            toggleStar = vm::toggleStar,
            pin = vm::pinMessage,
            unpin = vm::unpinMessage,
            deleteForMe = vm::deleteForMe,
            deleteForEveryone = vm::deleteForEveryone,
            beginReply = vm::beginReply,
            beginEdit = vm::beginEdit,
            openActions = vm::openActions,
            dismissActions = vm::dismissActions,
            enterSelection = vm::enterSelection,
            exitSelection = vm::exitSelection,
            toggleSelect = vm::toggleSelect,
            starSelected = vm::starSelected,
            forwardMessages = vm::forwardMessages,
            setBlocked = vm::setBlocked,
            jumpToMessage = vm::jumpToMessage,
            pinnedBannerTap = vm::pinnedBannerTap,
            consumeJump = vm::consumeJump,
        ),
        modifier = modifier,
    )
}

/** Narrow VM surface used by the stateless body. */
internal class VmBridge(
    val retry: (String) -> Unit,
    val send: () -> Unit,
    val sendImages: (List<String>, String?) -> Unit,
    val sendVideo: (String, Int, String?) -> Unit,
    val sendPoll: (String, List<String>, Boolean, Boolean, Boolean, Int?) -> Unit,
    val votePoll: (String, List<Int>) -> Unit,
    val retractVote: (String) -> Unit,
    val sendLocation: (Double, Double, String, Boolean, Long?) -> Unit,
    val toggleReaction: (String, String) -> Unit,
    val toggleStar: (String) -> Unit,
    val pin: (String) -> Unit,
    val unpin: (String) -> Unit,
    val deleteForMe: (List<String>) -> Unit,
    val deleteForEveryone: (List<String>) -> Unit,
    val beginReply: (String) -> Unit,
    val beginEdit: (String) -> Unit,
    val openActions: (String) -> Unit,
    val dismissActions: () -> Unit,
    val enterSelection: (String) -> Unit,
    val exitSelection: () -> Unit,
    val toggleSelect: (String) -> Unit,
    val starSelected: () -> Unit,
    val forwardMessages: (List<String>, List<String>, String) -> Unit,
    val setBlocked: (Boolean) -> Unit,
    val jumpToMessage: (String) -> Unit,
    val pinnedBannerTap: () -> Unit,
    val consumeJump: () -> Unit,
)

/**
 * Stateless conversation body (M4a core + M4b): header or multi-select top
 * bar, pinned banner, reverse message list with the long-press overlay,
 * forward/reactor sheets and the full composer surface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConversationContent(
    state: ConversationUiState,
    callbacks: ConversationCallbacks = ConversationCallbacks(),
    onVm: VmBridge? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Ephemeral UI state (dialogs/sheets not owned by the VM).
    var deleteTargets by remember { mutableStateOf<List<String>?>(null) }
    var confirmUnpinMessage by remember { mutableStateOf<String?>(null) }
    var confirmBlockChat by remember { mutableStateOf(false) }
    var forwardIds by remember { mutableStateOf<List<String>?>(null) }
    var reactorsTarget by remember { mutableStateOf<Pair<String, String>?>(null) } // (messageId, emoji)
    var moreEmojiFor by remember { mutableStateOf<String?>(null) }
    var listTopPx by remember { mutableStateOf(0f) }
    var actionAnchorPx by remember { mutableStateOf<Float?>(null) }

    var voicePlayingId by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current

    // ---- M4c attachment flows (S24-S26): ephemeral UI state. ----
    var showTray by remember { mutableStateOf(false) }
    var cameraOpen by remember { mutableStateOf(false) }
    var mediaDraft by remember { mutableStateOf<MediaDraft?>(null) }
    var pendingAdd by remember { mutableStateOf(false) }
    var recentMedia by remember { mutableStateOf<List<RecentMediaItem>>(emptyList()) }
    var viewerSession by remember { mutableStateOf<ViewerSession?>(null) }
    var pollComposerOpen by remember { mutableStateOf(false) }
    var locationPickerOpen by remember { mutableStateOf(false) }

    val chat = state.chat
    val overlayMessage = state.actionMessageId?.let { id ->
        state.rows.filterIsInstance<ConversationRow.MessageItem>()
            .firstOrNull { it.message.id == id }?.message
    }
    val isGroup = chat?.isGroup == true

    fun snack(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    fun copyMessages(ids: List<String>) {
        val texts = ids.mapNotNull { rowMessageOf(state, it) }
            .filter { !it.isDeleted }
            .map { copyableText(it) }
            .filter { it.isNotBlank() }
        if (texts.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(null, texts.joinToString("\n")))
        snack(context.getString(R.string.conversation_copied))
    }

    // ---- M4c media flows (S24-S26) ----
    fun addRecent(item: RecentMediaItem) {
        recentMedia = (listOf(item) + recentMedia.filterNot { it.uri == item.uri }).take(12)
    }

    fun videoDurationSeconds(uri: Uri): Int = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Video.VideoColumns.DURATION),
            null,
            null,
            null,
        )?.use { c ->
            if (c.moveToFirst()) (c.getLong(0) / 1000L).toInt() else 0
        } ?: 0
    }.getOrDefault(0)

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        // Photo Picker grants last for the process; keep them for the send.
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }
        }
        val picked = uris.map { uri ->
            val mime = runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
            val video = mime.startsWith("video/")
            MediaItemDraft(
                uri = uri.toString(),
                isVideo = video,
                durationSeconds = if (video) videoDurationSeconds(uri) else 0,
            )
        }
        val base = if (pendingAdd) mediaDraft?.items.orEmpty() else emptyList()
        val merged = base + picked
        if (merged.isEmpty()) return@rememberLauncherForActivityResult
        pendingAdd = false
        if (merged.size > 10) {
            Toast.makeText(
                context,
                context.getString(R.string.conversation_media_too_many),
                Toast.LENGTH_SHORT,
            ).show()
        }
        val kept = merged.take(10)
        mediaDraft = MediaDraft(kept)
        showTray = false
        recentMedia = (
            kept.map { RecentMediaItem(it.uri, it.isVideo, it.durationSeconds) } + recentMedia
            ).distinctBy { it.uri }.take(12)
    }

    fun launchGalleryPicker() {
        pickLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
        )
    }

    fun openCamera() {
        cameraOpen = true
        showTray = false
    }

    fun submitDraft(draft: MediaDraft, caption: String) {
        val bridge = onVm
        if (bridge != null) {
            val cap = caption.takeIf { it.isNotBlank() }
            val images = draft.items.filterNot { it.isVideo }.map { it.uri }
            val videos = draft.items.filter { it.isVideo }
            if (images.isNotEmpty()) {
                bridge.sendImages(images, cap)
                videos.forEach { v -> bridge.sendVideo(v.uri, v.durationSeconds, null) }
            } else {
                videos.forEachIndexed { i, v ->
                    bridge.sendVideo(v.uri, v.durationSeconds, if (i == 0) cap else null)
                }
            }
        }
        mediaDraft = null
    }

    fun onAttachmentTile(tile: AttachmentTile) {
        when (tile) {
            AttachmentTile.Camera -> openCamera()
            AttachmentTile.Gallery -> launchGalleryPicker()
            AttachmentTile.Poll -> {
                showTray = false
                pollComposerOpen = true
            }
            AttachmentTile.Location -> {
                showTray = false
                locationPickerOpen = true
            }
            else -> toastComingSoon(context, attachmentTileLabel(tile))
        }
    }

    fun onRecentTap(item: RecentMediaItem) {
        mediaDraft = MediaDraft(
            listOf(MediaItemDraft(item.uri, item.isVideo, item.durationSeconds)),
        )
        showTray = false
    }

    fun openMediaViewer(messageId: String, videoOnly: Boolean) {
        val items = buildViewerMedia(
            state.rows.filterIsInstance<ConversationRow.MessageItem>(),
            state.users.mapValues { it.value.displayName },
        )
        val index = if (videoOnly) {
            items.indexOfFirst { it.messageId == messageId && it.isVideo }
        } else {
            items.indexOfFirst { it.messageId == messageId && !it.isVideo }
        }
        if (index >= 0) viewerSession = ViewerSession(items, index)
    }

    fun doForward(ids: List<String>) {
        forwardIds = ids
    }

    fun sendForward(targetIds: List<String>, comment: String) {
        val ids = forwardIds.orEmpty()
        forwardIds = null
        if (ids.isEmpty()) return
        onVm?.forwardMessages?.invoke(ids, targetIds, comment)
        snack(context.getString(R.string.conversation_forwarded))
        if (state.selectionMode) onVm?.exitSelection?.invoke()
    }

    fun doDelete(ids: List<String>) {
        deleteTargets = ids
    }

    // Back handling: overlay -> dismiss; selection -> exit; sheets close first.
    BackHandler(enabled = overlayMessage != null) {
        onVm?.dismissActions?.invoke()
    }
    BackHandler(enabled = state.selectionMode && overlayMessage == null) {
        onVm?.exitSelection?.invoke()
    }
    BackHandler(enabled = showTray) {
        showTray = false
    }
    BackHandler(enabled = mediaDraft != null) {
        mediaDraft = null
    }
    BackHandler(enabled = cameraOpen) {
        cameraOpen = false
    }
    BackHandler(enabled = pollComposerOpen) {
        pollComposerOpen = false
    }
    BackHandler(enabled = locationPickerOpen) {
        locationPickerOpen = false
    }

    // Scroll-to target (reply quote / pinned banner taps).
    LaunchedEffect(state.jumpTargetId) {
        val target = state.jumpTargetId ?: return@LaunchedEffect
        val index = state.rows.indexOfFirst {
            it is ConversationRow.MessageItem && it.message.id == target
        }
        if (index >= 0) listState.animateScrollToItem(index)
        onVm?.consumeJump?.invoke()
    }

    // Unread divider dismissal when scrolled past it.
    LaunchedEffect(
        listState.firstVisibleItemIndex,
        state.rows.filterIsInstance<ConversationRow.Unread>().firstOrNull()?.key,
    ) {
        val unreadIndex = state.rows.indexOfFirst { it is ConversationRow.Unread }
        if (unreadIndex >= 0 && listState.firstVisibleItemIndex > unreadIndex) {
            callbacks.onUnreadDividerPassed()
        }
    }

    // Anchor of the long-pressed row (freeze happens in the list).
    LaunchedEffect(state.actionMessageId, listTopPx) {
        val id = state.actionMessageId ?: run { actionAnchorPx = null; return@LaunchedEffect }
        val index = state.rows.indexOfFirst {
            it is ConversationRow.MessageItem && it.message.id == id
        }
        val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
        actionAnchorPx = if (info != null) {
            listTopPx + info.offset.toFloat()
        } else {
            null
        }
    }

    val blocked = chat?.isBlocked == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PulseTheme.colors.background)
            .imePadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            if (state.selectionMode) {
                val selectedIds = state.selectedMessageIds
                val selectedRows = state.rows.filterIsInstance<ConversationRow.MessageItem>()
                    .filter { it.message.id in selectedIds }
                val canCopy = selectedRows.any { copyableText(it.message).isNotBlank() }
                MultiSelectTopBar(
                    count = selectedIds.size,
                    onClose = { onVm?.exitSelection?.invoke() },
                    canCopy = canCopy,
                    onCopy = { copyMessages(selectedIds.toList()) },
                    onForward = { doForward(selectedIds.toList()) },
                    onStar = { onVm?.starSelected?.invoke() },
                    onDelete = { doDelete(selectedIds.toList()) },
                )
            } else {
                state.chat?.let { c ->
                    ChatHeader(
                        title = headerTitle(c, state.users),
                        statusText = headerStatusText(c, state),
                        status = headerStatus(c, state),
                        avatarSeed = headerAvatarSeed(c, state.users),
                        avatarName = headerAvatarName(c, state.users),
                        isGroup = c.isGroup,
                        memberNames = headerMemberNames(c, state.users),
                        otherUnread = state.otherUnread,
                        muted = c.isMuted,
                        blocked = blocked,
                        onBack = callbacks.onBack,
                        onVoiceCall = callbacks.onVoiceCall,
                        onVideoCall = callbacks.onVideoCall,
                        onMenuAction = { action ->
                            when (action) {
                                ChatHeaderAction.ToggleMute -> callbacks.onToggleMuted()
                                ChatHeaderAction.Block -> {
                                    if (c.isGroup) {
                                        toastComingSoon(context, menuLabelRes(action))
                                    } else if (blocked) {
                                        onVm?.setBlocked?.invoke(false)
                                    } else {
                                        confirmBlockChat = true
                                    }
                                }
                                else -> toastComingSoon(context, menuLabelRes(action))
                            }
                        },
                    )
                }
            }

            // Pinned banner (below header).
            val pinnedData = state.validPinnedIds.mapNotNull { id ->
                rowMessageOf(state, id)?.let { msg ->
                    PinnedBannerData(id, bannerExcerpt(msg))
                }
            }
            if (pinnedData.isNotEmpty() && !state.selectionMode) {
                PinnedBanner(
                    items = pinnedData,
                    displayIndex = state.pinnedDisplayIndex % pinnedData.size,
                    onTap = { onVm?.pinnedBannerTap?.invoke() },
                    onClose = {
                        val current = state.validPinnedIds
                            .getOrNull(state.pinnedDisplayIndex % pinnedData.size)
                        confirmUnpinMessage = current
                    },
                )
            }

            // Message area (contains the overlay layer).
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { listTopPx = it.positionInRoot().y },
            ) {
                ConversationList(
                    state = state,
                    isGroup = isGroup,
                    listState = listState,
                    freeze = overlayMessage != null,
                    voicePlayingId = voicePlayingId,
                    onVoiceToggle = { id ->
                        voicePlayingId = if (voicePlayingId == id) null else id
                    },
                    callbacks = callbacks,
                    onVm = onVm,
                    onLongPress = { id -> onVm?.openActions?.invoke(id) },
                    onTap = { id ->
                        if (state.selectionMode) onVm?.toggleSelect?.invoke(id)
                    },
                    onReactionLongPress = { id, emoji -> reactorsTarget = id to emoji },
                    onImageTap = { id, _ -> openMediaViewer(id, videoOnly = false) },
                    onVideoTap = { id -> openMediaViewer(id, videoOnly = true) },
                    onPollVote = { id, indexes -> onVm?.votePoll?.invoke(id, indexes) },
                    onPollRetract = { id -> onVm?.retractVote?.invoke(id) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Composer (full M4b surface) - hidden while overlay/selection.
            if (overlayMessage == null && !state.selectionMode) {
                Column {
                    if (showTray) {
                        AttachmentTray(
                            recent = recentMedia,
                            onTile = { tile -> onAttachmentTile(tile) },
                            onRecent = { item -> onRecentTap(item) },
                        )
                    }
                    MessageComposer(
                    state = state.composer,
                    value = state.draftText,
                    onValueChange = callbacks.onDraftChange,
                    mentionMembers = state.mentionMembers,
                    onSend = callbacks.onSend,
                    onAttachment = {
                        if (onVm != null) {
                            showTray = !showTray
                        } else {
                            callbacks.onAttachment()
                        }
                    },
                    onCamera = {
                        if (onVm != null) {
                            openCamera()
                        } else {
                            callbacks.onCamera()
                        }
                    },
                    onMicPress = callbacks.onMicPress,
                    onRecordCancel = callbacks.onRecordCancel,
                    onRecordLock = callbacks.onRecordLock,
                    onRecordFinish = callbacks.onRecordFinish,
                    onRecordTooShort = callbacks.onRecordTooShort,
                    onCloseBar = callbacks.onCloseComposerBar,
                    onMentionSelected = {},
                    onUnblock = callbacks.onUnblock,
                    modifier = Modifier.navigationBarsPadding(),
                )
                }
            }
        }

        // Themed snackbar (bottom, above the composer).
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
        ) { data ->
            val c = PulseTheme.colors
            Snackbar(
                snackbarData = data,
                shape = PulseShapes.md,
                containerColor = c.surface,
                contentColor = c.textPrimary,
            )
        }

        // ---- Long-press overlay: scrim + lifted bubble + quick reactions +
        // ---- action sheet (bottom).
        if (overlayMessage != null) {
            val anchor = actionAnchorPx
            Box(Modifier.fillMaxSize()) {
                LongPressScrim(onDismiss = { onVm?.dismissActions?.invoke() })
                if (anchor != null) {
                    val row = rowItemOf(state, overlayMessage.id)
                    if (row != null) {
                        val rowMessage = row.message
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset {
                                    IntOffset(0, anchor.toInt() - 56.dp.roundToPx())
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            QuickReactionBar(
                                onReact = { emoji ->
                                    onVm?.toggleReaction?.invoke(overlayMessage.id, emoji)
                                    onVm?.dismissActions?.invoke()
                                },
                                onMore = { moreEmojiFor = overlayMessage.id },
                            )
                            Spacer(Modifier.height(PulseSpacing.sm))
                            // The lifted bubble: same row content on a scrim.
                            MessageBubbleReplica(
                                message = rowMessage,
                                isFirstInRun = row.isFirstInRun,
                                isLastInRun = row.isLastInRun,
                                senderName = if (isGroup && !rowMessage.isOutgoing && row.isFirstInRun) {
                                    row.senderName
                                } else {
                                    null
                                },
                                senderNameColor = row.senderSeed?.let {
                                    senderToneTextColor(AvatarTones.bySeed(it), PulseTheme.colors.isDark)
                                },
                                avatarSeed = if (isGroup && !rowMessage.isOutgoing) row.senderSeed else null,
                                quote = quoteDataFor(state, rowMessage.replyToMessageId),
                                onQuoteTap = {
                                    onVm?.dismissActions?.invoke()
                                    rowMessage.replyToMessageId?.let { onVm?.jumpToMessage?.invoke(it) }
                                },
                                onRetry = if (rowMessage.isOutgoing &&
                                    rowMessage.status == MessageStatus.Failed
                                ) {
                                    { onVm?.retry?.invoke(rowMessage.id) }
                                } else {
                                    null
                                },
                                voicePlaying = voicePlayingId == rowMessage.id,
                                onVoiceToggle = {
                                    voicePlayingId = if (voicePlayingId == rowMessage.id) null
                                    else rowMessage.id
                                },
                                onPollVote = { indexes -> onVm?.votePoll?.invoke(rowMessage.id, indexes) },
                                onPollRetract = { onVm?.retractVote?.invoke(rowMessage.id) },
                                scale = 1.02f,
                            )
                        }
                    }
                }
                // Action sheet at the bottom.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                ) {
                    MessageActionSheet(
                        items = actionItems(
                            context = context,
                            message = overlayMessage,
                            isPinnedInChat = overlayMessage.id in (chat?.pinnedMessageIds ?: emptyList()),
                            onReply = {
                                onVm?.dismissActions?.invoke()
                                onVm?.beginReply?.invoke(overlayMessage.id)
                            },
                            onForward = { onVm?.dismissActions?.invoke(); doForward(listOf(overlayMessage.id)) },
                            onCopy = { onVm?.dismissActions?.invoke(); copyMessages(listOf(overlayMessage.id)) },
                            onPin = {
                                onVm?.dismissActions?.invoke()
                                onVm?.pin?.invoke(overlayMessage.id)
                            },
                            onStar = {
                                onVm?.dismissActions?.invoke()
                                onVm?.toggleStar?.invoke(overlayMessage.id)
                            },
                            onEdit = {
                                onVm?.dismissActions?.invoke()
                                onVm?.beginEdit?.invoke(overlayMessage.id)
                            },
                            onInfo = {
                                onVm?.dismissActions?.invoke()
                                toastComingSoon(context, R.string.conversation_action_info)
                            },
                            onDelete = { onVm?.dismissActions?.invoke(); doDelete(listOf(overlayMessage.id)) },
                            onSelect = { onVm?.enterSelection?.invoke(overlayMessage.id) },
                            onRetractVote = {
                                onVm?.dismissActions?.invoke()
                                onVm?.retractVote?.invoke(overlayMessage.id)
                            },
                        ),
                    )
                }
            }
        }

        // Forward sheet.
        forwardIds?.let { ids ->
            if (ids.isNotEmpty()) {
                val preview = ids.mapNotNull { rowMessageOf(state, it) }
                AppBottomSheet(
                    onDismissRequest = { forwardIds = null },
                ) {
                    ForwardSheet(
                        chats = state.selectableChats,
                        messages = preview,
                        onSend = ::sendForward,
                    )
                }
            }
        }

        // Reactor list sheet.
        reactorsTarget?.let { (messageId, emoji) ->
            val message = rowMessageOf(state, messageId)
            val reactors = message?.reactions?.firstOrNull { it.emoji == emoji }
            val users = reactors?.userIds?.mapNotNull { uid ->
                if (uid == "me") {
                    ReactorUi(context.getString(R.string.conversation_you), 3)
                } else {
                    state.users[uid]?.let { ReactorUi(it.displayName, it.avatarSeed) }
                }
            }.orEmpty()
            AppBottomSheet(onDismissRequest = { reactorsTarget = null }) {
                ReactorsSheetContent(emoji = emoji, reactors = users)
            }
        }

        // Emoji picker sheet (from the quick bar "+").
        moreEmojiFor?.let { messageId ->
            AppBottomSheet(onDismissRequest = { moreEmojiFor = null }) {
                EmojiSheetContent(
                    onEmoji = { emoji ->
                        onVm?.toggleReaction?.invoke(messageId, emoji)
                        moreEmojiFor = null
                        onVm?.dismissActions?.invoke()
                    },
                )
            }
        }

        // Delete dialog (single: for-me / for-everyone; multi: for-me).
        deleteTargets?.let { ids ->
            val single = ids.size == 1
            val msg = rowMessageOf(state, ids.first())
            val own = single && msg?.isOutgoing == true
            AlertDialog(
                onDismissRequest = { deleteTargets = null },
                shape = PulseShapes.lg,
                containerColor = PulseTheme.colors.surface,
                title = {
                    Text(
                        text = if (single) {
                            stringResource(R.string.conversation_delete_title)
                        } else {
                            stringResource(R.string.conversation_delete_many_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = PulseTheme.colors.textPrimary,
                    )
                },
                text = {
                    Text(
                        text = if (single) {
                            stringResource(
                                if (own) R.string.conversation_delete_single_text
                                else R.string.conversation_delete_others_text,
                            )
                        } else {
                            stringResource(R.string.conversation_delete_many_text, ids.size)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = PulseTheme.colors.textSecondary,
                    )
                },
                confirmButton = {
                    Column {
                        if (own) {
                            TextButton(onClick = {
                                deleteTargets = null
                                onVm?.deleteForEveryone?.invoke(ids)
                            }) {
                                Text(
                                    text = stringResource(R.string.conversation_delete_everyone),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PulseTheme.colors.error,
                                )
                            }
                        }
                        TextButton(onClick = {
                            deleteTargets = null
                            if (state.selectionMode) onVm?.deleteForMe?.invoke(ids) else {
                                onVm?.deleteForMe?.invoke(ids)
                                if (!state.selectionMode) onVm?.exitSelection?.invoke()
                            }
                        }) {
                            Text(
                                text = stringResource(R.string.conversation_delete_me),
                                style = MaterialTheme.typography.labelLarge,
                                color = PulseTheme.colors.textPrimary,
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTargets = null }) {
                        Text(
                            text = stringResource(android.R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                            color = PulseTheme.colors.textSecondary,
                        )
                    }
                },
            )
        }

        // Unpin confirm (banner close).
        confirmUnpinMessage?.let { id ->
            ConfirmDialog(
                title = stringResource(R.string.conversation_unpin_title),
                text = stringResource(R.string.conversation_unpin_text),
                confirmLabel = stringResource(R.string.conversation_unpin_confirm),
                onConfirm = {
                    confirmUnpinMessage = null
                    onVm?.unpin?.invoke(id)
                },
                onDismiss = { confirmUnpinMessage = null },
            )
        }

        // Block confirm (header menu).
        if (confirmBlockChat) {
            val name = chat?.let { headerTitle(it, state.users) } ?: ""
            ConfirmDialog(
                title = stringResource(R.string.conversation_block_title, name),
                text = stringResource(R.string.conversation_block_text),
                confirmLabel = stringResource(R.string.conversation_block_confirm),
                onConfirm = {
                    confirmBlockChat = false
                    onVm?.setBlocked?.invoke(true)
                },
                onDismiss = { confirmBlockChat = false },
            )
        }

        // ---- M4c full-screen media surfaces (S24-S26) ----
        mediaDraft?.let { draft ->
            val reply = state.composer as? ComposerUiState.Reply
            MediaSendSheet(
                draft = draft,
                onDismiss = { mediaDraft = null },
                onSend = { caption -> submitDraft(draft, caption) },
                onAddMore = {
                    pendingAdd = true
                    launchGalleryPicker()
                },
                onRemove = { index ->
                    val rest = draft.items.toMutableList()
                    rest.removeAt(index)
                    mediaDraft = if (rest.isEmpty()) null else MediaDraft(rest)
                },
                replyTitle = reply?.senderName,
                replyExcerpt = reply?.excerpt,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (cameraOpen) {
            CameraSimOverlay(
                onClose = { cameraOpen = false },
                onPhoto = { uri ->
                    cameraOpen = false
                    addRecent(RecentMediaItem(uri = uri, isVideo = false))
                    mediaDraft = MediaDraft(
                        listOf(MediaItemDraft(uri = uri, isVideo = false)),
                    )
                },
                onVideo = { uri, seconds ->
                    cameraOpen = false
                    addRecent(RecentMediaItem(uri = uri, isVideo = true, durationSeconds = seconds))
                    mediaDraft = MediaDraft(
                        listOf(MediaItemDraft(uri = uri, isVideo = true, durationSeconds = seconds)),
                    )
                },
                onVideoTooShort = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.conversation_camera_video_too_short),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }

        // ---- M4c S39: create-poll composer. ----
        if (pollComposerOpen) {
            PollComposerSheet(
                onDismiss = { pollComposerOpen = false },
                onSend = { question, options, multiple, anonymous, quiz, correct ->
                    pollComposerOpen = false
                    onVm?.sendPoll?.invoke(question, options, multiple, anonymous, quiz, correct)
                    snack(context.getString(R.string.conversation_poll_created))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ---- M4c S40: location picker + live-location durations. ----
        if (locationPickerOpen) {
            LocationPickerSheet(
                onDismiss = { locationPickerOpen = false },
                onSendLocation = { lat, lng, address, isLive, durationMs ->
                    locationPickerOpen = false
                    onVm?.sendLocation?.invoke(lat, lng, address, isLive, durationMs)
                    snack(
                        context.getString(
                            if (isLive) R.string.conversation_location_live_sent
                            else R.string.conversation_location_sent,
                        ),
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ---- M4c S33: fullscreen media viewer (images/videos of this chat). ----
        viewerSession?.let { session ->
            MediaViewerOverlay(
                items = session.items,
                initialIndex = session.index,
                onClose = { viewerSession = null },
                onForward = { id ->
                    viewerSession = null
                    doForward(listOf(id))
                },
                onDelete = { id ->
                    viewerSession = null
                    doDelete(listOf(id))
                },
                onInfo = {
                    viewerSession = null
                    toastComingSoon(context, R.string.conversation_action_info)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Message row bubble reused by the overlay (same content, slight lift). */
@Composable
private fun MessageBubbleReplica(
    message: Message,
    isFirstInRun: Boolean,
    isLastInRun: Boolean,
    senderName: String?,
    senderNameColor: Color?,
    avatarSeed: Int?,
    quote: BubbleQuoteData?,
    onQuoteTap: (() -> Unit)?,
    onRetry: (() -> Unit)?,
    voicePlaying: Boolean,
    onVoiceToggle: () -> Unit,
    scale: Float,
    onPollVote: ((List<Int>) -> Unit)? = null,
    onPollRetract: (() -> Unit)? = null,
) {
    val lift = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(Unit) {
        lift.animateTo(scale, animationSpec = spring(stiffness = Spring.StiffnessMedium))
    }
    MessageBubble(
        message = message,
        modifier = Modifier.graphicsLayer {
            scaleX = lift.value
            scaleY = lift.value
        },
        isFirstInRun = isFirstInRun,
        isLastInRun = isLastInRun,
        senderName = senderName,
        senderNameColor = senderNameColor,
        avatarSeed = avatarSeed,
        onRetry = onRetry,
        quote = quote,
        onQuoteTap = onQuoteTap,
        voicePlaying = voicePlaying,
        onVoiceToggle = onVoiceToggle,
        onPollVote = onPollVote,
        onPollRetract = onPollRetract,
    )
}

/* ============ Action-item construction ============ */

private fun actionItems(
    context: Context,
    message: Message,
    isPinnedInChat: Boolean,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onPin: () -> Unit,
    onStar: () -> Unit,
    onEdit: () -> Unit,
    onInfo: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onRetractVote: () -> Unit = {},
): List<MessageActionItem> {
    val own = message.isOutgoing
    val canEdit = own && !message.isDeleted &&
        message.content is MessageContent.Text &&
        System.currentTimeMillis() - message.sentAtMillis <= 15 * 60_000L
    val canCopy = !message.isDeleted && copyableText(message).isNotBlank()
    val items = ArrayList<MessageActionItem>()
    if (!message.isDeleted) {
        items.add(MessageActionItem(Icons.CornerUpLeft, R.string.conversation_action_reply, onClick = onReply))
    }
    items.add(MessageActionItem(Icons.Share, R.string.conversation_action_forward, onClick = onForward))
    val myPollVote = !own && (message.content as? MessageContent.Poll)
        ?.votes?.values?.any { users -> "me" in users } == true
    if (myPollVote) {
        items.add(MessageActionItem(Icons.RotateCw, R.string.conversation_poll_remove_vote, onClick = onRetractVote))
    }
    if (canCopy) {
        items.add(MessageActionItem(Icons.Copy, R.string.conversation_action_copy, onClick = onCopy))
    }
    if (!message.isDeleted) {
        items.add(
            MessageActionItem(
                if (isPinnedInChat) Icons.PinOff else Icons.Pin,
                if (isPinnedInChat) R.string.conversation_action_unpin else R.string.conversation_action_pin,
                onClick = onPin,
            ),
        )
        items.add(
            MessageActionItem(
                Icons.Star,
                if (message.isStarred) R.string.conversation_action_unstar else R.string.conversation_action_star,
                onClick = onStar,
            ),
        )
        if (canEdit) {
            items.add(MessageActionItem(Icons.Pencil, R.string.conversation_action_edit, onClick = onEdit))
        }
        items.add(MessageActionItem(Icons.Info, R.string.conversation_action_info, onClick = onInfo))
    }
    items.add(
        MessageActionItem(Icons.Trash, R.string.conversation_action_delete, destructive = true, onClick = onDelete),
    )
    items.add(MessageActionItem(Icons.Check, R.string.conversation_action_select, onClick = onSelect))
    return items
}

/* ============ Presentation helpers ============ */

private fun copyableText(message: Message): String = when (val content = message.content) {
    is MessageContent.Text -> content.text
    is MessageContent.System -> ""
    else -> content.text
}

private fun rowMessageOf(state: ConversationUiState, id: String): Message? =
    state.rows.filterIsInstance<ConversationRow.MessageItem>()
        .firstOrNull { it.message.id == id }?.message

private fun rowItemOf(state: ConversationUiState, id: String): ConversationRow.MessageItem? =
    state.rows.filterIsInstance<ConversationRow.MessageItem>()
        .firstOrNull { it.message.id == id }

private fun bannerExcerpt(message: Message): String = when (val content = message.content) {
    is MessageContent.Text -> content.text
    else -> content.text.ifBlank { MessageLabels.typeLabel(message.type).orEmpty() }
}

@Composable
internal fun quoteDataFor(state: ConversationUiState, replyToMessageId: String?): BubbleQuoteData? {
    if (replyToMessageId == null) return null
    val target = rowMessageOf(state, replyToMessageId) ?: return null
    if (target.isSystem || target.isDeleted) return null
    val name = if (target.senderId == "me") {
        stringResource(R.string.conversation_you)
    } else {
        state.users[target.senderId]?.displayName ?: "Message"
    }
    val content = target.content
    return if (content is MessageContent.Text) {
        BubbleQuoteData(senderName = name, text = content.text, isText = true, type = MessageType.Text)
    } else {
        BubbleQuoteData(
            senderName = name,
            text = content.text,
            isText = false,
            type = target.type,
        )
    }
}

@Composable
private fun quoteDataForComposable(state: ConversationUiState, messageId: String): BubbleQuoteData? {
    val message = rowMessageOf(state, messageId) ?: return null
    return quoteDataFor(state, message.replyToMessageId)
}

/* ============ Header resolution (M4a, unchanged) ============ */

private fun headerTitle(chat: Chat, users: Map<String, User>): String {
    if (chat.isGroup) return chat.title ?: "Group"
    val peer = users[chat.participantIds.firstOrNull { it != "me" }]
    return peer?.displayName ?: "Chat"
}

private fun headerAvatarName(chat: Chat, users: Map<String, User>): String? =
    if (chat.isGroup) chat.title else headerTitle(chat, users)

private fun headerAvatarSeed(chat: Chat, users: Map<String, User>): Int {
    if (chat.isGroup) return chat.avatarSeed
    return users[chat.participantIds.firstOrNull { it != "me" }]?.avatarSeed ?: chat.avatarSeed
}

private fun headerMemberNames(chat: Chat, users: Map<String, User>): List<String> =
    if (chat.isGroup) {
        chat.participantIds.filter { it != "me" }.mapNotNull { users[it]?.firstName }
    } else {
        emptyList()
    }

@Composable
private fun headerStatus(chat: Chat, state: ConversationUiState): ChatHeaderStatus {
    if (chat.isGroup) return ChatHeaderStatus.Group
    if (state.rows.any { it is ConversationRow.Typing }) return ChatHeaderStatus.Typing
    val peer = state.users[chat.participantIds.firstOrNull { it != "me" }]
    return if (peer?.isOnline == true) ChatHeaderStatus.Online else ChatHeaderStatus.Neutral
}

@Composable
private fun headerStatusText(chat: Chat, state: ConversationUiState): String {
    val typing = state.rows.any { it is ConversationRow.Typing }
    val peer = state.users[chat.participantIds.firstOrNull { it != "me" }]
    return when {
        chat.isGroup -> stringResource(R.string.conversation_members, chat.participantIds.size)
        typing -> stringResource(R.string.conversation_typing)
        peer?.isOnline == true -> stringResource(R.string.conversation_online)
        else -> ConversationFormat.lastSeen(peer?.lastSeenAtMillis)
    }
}

private fun menuLabelRes(action: ChatHeaderAction): Int = when (action) {
    ChatHeaderAction.ViewProfile -> R.string.conversation_menu_view_profile
    ChatHeaderAction.InChatSearch -> R.string.conversation_menu_search
    ChatHeaderAction.Wallpaper -> R.string.conversation_menu_wallpaper
    ChatHeaderAction.ClearChat -> R.string.conversation_menu_clear_chat
    ChatHeaderAction.Block -> R.string.conversation_menu_block
    ChatHeaderAction.Report -> R.string.conversation_menu_report
    ChatHeaderAction.ToggleMute -> R.string.conversation_menu_mute
}

/* ============ Message list ============ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationList(
    state: ConversationUiState,
    isGroup: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    freeze: Boolean,
    voicePlayingId: String?,
    onVoiceToggle: (String) -> Unit,
    callbacks: ConversationCallbacks,
    onVm: VmBridge?,
    onLongPress: (String) -> Unit,
    onTap: (String) -> Unit,
    onReactionLongPress: (String, String) -> Unit = { _: String, _: String -> },
    onImageTap: (String, Int) -> Unit = { _: String, _: Int -> },
    onVideoTap: (String) -> Unit = {},
    onPollVote: (String, List<Int>) -> Unit = { _: String, _: List<Int> -> },
    onPollRetract: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    // Pinned-to-newest detection (M4a scroll contract).
    val atBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    LaunchedEffect(atBottom) {
        if (atBottom) callbacks.onScrolledToBottom() else callbacks.onScrolledUp()
    }

    // Newest-first list: keep the view anchored to the newest row while the
    // user is pinned to the bottom and new rows arrive.
    val bottomKey = state.rows.firstOrNull()?.key
    LaunchedEffect(bottomKey) {
        if (atBottom && state.rows.isNotEmpty()) listState.scrollToItem(0)
    }


    if (state.loading || state.rows.isEmpty()) {
        EmptyState(
            icon = Icons.MessageCircle,
            title = if (state.loading) "" else stringResource(R.string.conversation_empty_title),
            modifier = modifier,
        )
        return
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            userScrollEnabled = !freeze,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(PulseSpacing.tight),
            contentPadding = PaddingValues(vertical = PulseSpacing.sm),
        ) {
            items(
                count = state.rows.size,
                key = { index -> state.rows[index].key },
            ) { index ->
                val row = state.rows[index]
                when (row) {
                    is ConversationRow.Day -> DateSeparator(
                        label = ConversationFormat.dayLabel(row.dateMillis),
                        modifier = Modifier.padding(vertical = PulseSpacing.xs),
                    )
                    is ConversationRow.Unread -> UnreadDivider(
                        label = pluralStringResource(R.plurals.conversation_unread_messages, row.count, row.count),
                        modifier = Modifier.padding(vertical = PulseSpacing.xs),
                    )
                    is ConversationRow.Typing -> {
                        TypingIndicator(
                            senderName = row.senderName,
                            senderNameColor = row.senderSeed?.let {
                                senderToneTextColor(AvatarTones.bySeed(it), c.isDark)
                            } ?: c.textSecondary,
                            modifier = Modifier.animateItem(fadeInSpec = tween(220)),
                        )
                    }
                    is ConversationRow.MessageItem -> {
                        val message = row.message
                        if (message.content is MessageContent.System || message.type == MessageType.System) {
                            SystemMessageRow(
                                text = message.text,
                                modifier = Modifier
                                    .padding(vertical = PulseSpacing.xs)
                                    .animateItem(fadeInSpec = tween(220)),
                            )
                        } else {
                            // Outgoing send animation: fade + 8dp slide-up.
                            val animateIn = message.isOutgoing &&
                                message.status == MessageStatus.Sending
                            val offsetY = remember { androidx.compose.animation.core.Animatable(1f) }
                            LaunchedEffect(message.id) {
                                if (message.status == MessageStatus.Sending) {
                                    offsetY.snapTo(1f)
                                    offsetY.animateTo(0f, animationSpec = tween(220))
                                } else {
                                    offsetY.snapTo(0f)
                                }
                            }
                            val selected = message.id in state.selectedMessageIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (state.selectionMode && selected) {
                                            Modifier.background(c.accentContainerMuted)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .graphicsLayer {
                                        translationY = if (animateIn) 8.dp.toPx() * offsetY.value else 0f
                                        alpha = if (animateIn) offsetY.value else 1f
                                    }
                                    .animateItem(
                                        fadeInSpec = tween(220),
                                        placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val msgColumn: @Composable () -> Unit = {
                                    MessageBubble(
                                        message = message,
                                        isFirstInRun = row.isFirstInRun,
                                        isLastInRun = row.isLastInRun,
                                        senderName = if (isGroup && !message.isOutgoing && row.isFirstInRun) {
                                            row.senderName
                                        } else {
                                            null
                                        },
                                        senderNameColor = row.senderSeed?.let {
                                            senderToneTextColor(AvatarTones.bySeed(it), c.isDark)
                                        },
                                        avatarSeed = if (isGroup && !message.isOutgoing) row.senderSeed else null,
                                        quote = quoteDataForComposable(state, message.id),
                                        onQuoteTap = {
                                            message.replyToMessageId?.let { onVm?.jumpToMessage?.invoke(it) }
                                        },
                                        flashSignal = if (state.flashMessageId == message.id) {
                                            state.flashTick
                                        } else {
                                            0
                                        },
                                        onLongPress = {
                                            if (!state.selectionMode) onLongPress(message.id)
                                        },
                                        onTap = {
                                            if (state.selectionMode) onTap(message.id)
                                        },
                                        onToggleReaction = { emoji ->
                                            if (!state.selectionMode) {
                                                onVm?.toggleReaction?.invoke(message.id, emoji)
                                            }
                                        },
                                        onReactionLongPress = { emoji ->
                                            onReactionLongPress(message.id, emoji)
                                        },
                                        voicePlaying = voicePlayingId == message.id &&
                                            message.content is MessageContent.Voice,
                                        onVoiceToggle = { onVoiceToggle(message.id) },
                                        onRetry = if (message.isOutgoing &&
                                            message.status == MessageStatus.Failed
                                        ) {
                                            { onVm?.retry?.invoke(message.id) }
                                        } else {
                                            null
                                        },
                                        onImageTap = { index -> onImageTap(message.id, index) },
                                        onVideoTap = { onVideoTap(message.id) },
                                        onPollVote = { idx -> onPollVote(message.id, idx) },
                                        onPollRetract = { onPollRetract(message.id) },
                                    )
                                }
                                if (state.selectionMode) {
                                    if (message.isOutgoing) {
                                        Spacer(Modifier.width(PulseSpacing.md))
                                        msgColumn()
                                        Spacer(Modifier.width(PulseSpacing.xs))
                                        SelectionCheck(selected = selected)
                                    } else {
                                        Spacer(Modifier.width(PulseSpacing.sm))
                                        SelectionCheck(selected = selected)
                                        Spacer(Modifier.width(PulseSpacing.md))
                                        msgColumn()
                                    }
                                } else {
                                    msgColumn()
                                }
                            }
                        }
                    }
                }
            }
        }
    
        // Jump-to-newest FAB (hidden while the overlay / selection is open).
        val showFab by remember { derivedStateOf { !atBottom && !freeze && !state.selectionMode } }
        if (showFab) {
            val scope = rememberCoroutineScope()
            Surface(
                onClick = {
                    scope.launch { listState.animateScrollToItem(0) }
                },
                shape = CircleShape,
                color = c.surface,
                border = BorderStroke(1.dp, c.border),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(PulseSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = AppIcons.ArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(PulseSpacing.xl),
                        tint = c.textSecondary,
                    )
                    if (state.unseenCount > 0) {
                        Spacer(Modifier.width(PulseSpacing.xs))
                        Text(
                            text = state.unseenCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textSecondary,
                        )
                    }
                }
            }
        }

}
}

private fun toastComingSoon(context: Context, labelRes: Int) {
    val label = context.getString(labelRes)
    Toast.makeText(context, context.getString(R.string.conversation_coming_soon, label), Toast.LENGTH_SHORT).show()
}
