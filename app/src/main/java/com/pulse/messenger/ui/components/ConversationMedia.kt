@file:OptIn(ExperimentalFoundationApi::class)

package com.pulse.messenger.ui.components

import android.os.SystemClock
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.ConversationFormat
import com.pulse.messenger.ui.util.SampleMedia
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/* =====================================================================
 * M4c attachment flows (S24 tray, S25 picker/caption sheet, S26 simulated
 * camera). UI-only state; the conversation screen owns the instances and
 * funnels sends through the ViewModel. Solid surfaces only - no gradients.
 * ===================================================================== */

/** One picked/captured media item ready to be sent. */
data class MediaItemDraft(
    val uri: String,
    val isVideo: Boolean,
    val durationSeconds: Int = 0,
)

/** Ordered set of media prepared in the review/caption sheet. */
data class MediaDraft(val items: List<MediaItemDraft>)

/** Recently sent/picked item shown in the attachment tray (in-session). */
data class RecentMediaItem(
    val uri: String,
    val isVideo: Boolean,
    val durationSeconds: Int = 0,
)

/** Attachment-sheet tiles (S24). */
enum class AttachmentTile {
    Camera,
    Gallery,
    Document,
    Audio,
    Poll,
    Location,
    Contact,
}

internal fun attachmentTileIcon(tile: AttachmentTile): ImageVector = when (tile) {
    AttachmentTile.Camera -> AppIcons.Camera
    AttachmentTile.Gallery -> AppIcons.Images
    AttachmentTile.Document -> AppIcons.FileText
    AttachmentTile.Audio -> AppIcons.FileAudio
    AttachmentTile.Poll -> AppIcons.BarChart
    AttachmentTile.Location -> AppIcons.MapPin
    AttachmentTile.Contact -> AppIcons.UserPlus
}

internal fun attachmentTileLabel(tile: AttachmentTile): Int = when (tile) {
    AttachmentTile.Camera -> R.string.conversation_attach_camera
    AttachmentTile.Gallery -> R.string.conversation_attach_gallery
    AttachmentTile.Document -> R.string.conversation_attach_document
    AttachmentTile.Audio -> R.string.conversation_attach_audio
    AttachmentTile.Poll -> R.string.conversation_attach_poll
    AttachmentTile.Location -> R.string.conversation_attach_location
    AttachmentTile.Contact -> R.string.conversation_attach_contact
}

/** Coil model for one media uri (bundled sample or real content uri). */
private fun coilModel(uri: String, isVideo: Boolean): Any {
    if (!isVideo) return SampleMedia.imageModel(uri) ?: uri
    return SampleMedia.videoThumbRes(uri) ?: uri
}

/* =====================================================================
 * S24 - Attachment tray: quick tiles + an in-session "recent" strip of
 * media picked or captured during this conversation.
 * ===================================================================== */

@Composable
fun AttachmentTray(
    recent: List<RecentMediaItem>,
    onTile: (AttachmentTile) -> Unit,
    onRecent: (RecentMediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface)
            .padding(top = PulseSpacing.md, bottom = PulseSpacing.sm),
    ) {
        if (recent.isNotEmpty()) {
            Text(
                text = stringResource(R.string.conversation_attach_recent),
                style = MaterialTheme.typography.labelMedium,
                color = c.textTertiary,
                modifier = Modifier.padding(horizontal = PulseSpacing.md),
            )
            Spacer(Modifier.height(PulseSpacing.xs))
            LazyRow(
                contentPadding = PaddingValues(horizontal = PulseSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
            ) {
                itemsIndexed(recent) { _, item ->
                    RecentMediaThumb(item = item, onClick = { onRecent(item) })
                }
            }
            Spacer(Modifier.height(PulseSpacing.sm))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = PulseSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.md),
        ) {
            AttachmentTile.entries.forEach { tile ->
                AttachmentTileButton(tile = tile, onClick = { onTile(tile) })
            }
        }
    }
}

@Composable
private fun RecentMediaThumb(item: RecentMediaItem, onClick: () -> Unit) {
    val c = PulseTheme.colors
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(PulseShapes.md)
            .background(c.surfaceVariant)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        AsyncImage(
            model = coilModel(item.uri, item.isVideo),
            contentDescription = stringResource(
                if (item.isVideo) R.string.conversation_video else R.string.conversation_image_cd,
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .clip(PulseShapes.sm)
                    .background(c.mediaScrim)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(
                    text = ConversationFormat.durationLabel(item.durationSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onMediaScrim,
                )
            }
        }
    }
}

@Composable
private fun AttachmentTileButton(tile: AttachmentTile, onClick: () -> Unit) {
    val c = PulseTheme.colors
    val label = stringResource(attachmentTileLabel(tile))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(PulseShapes.sm),
    ) {
        Box(
            modifier = Modifier
                .size(PulseSizes.attachmentTile)
                .clip(CircleShape)
                .background(c.surfaceVariant)
                .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = attachmentTileIcon(tile),
                contentDescription = label,
                modifier = Modifier.size(PulseIconSizes.feature),
                tint = c.textPrimary,
            )
        }
        Spacer(Modifier.height(PulseSpacing.tight))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.width(64.dp),
        )
    }
}

/* =====================================================================
 * S25 - Media review/caption sheet: full-screen surface over the chat.
 * No crop/draw/text tools (documented out of scope). Reply context is
 * preserved (chip) and the VM re-applies it when the send lands.
 * ===================================================================== */

@Composable
fun MediaSendSheet(
    draft: MediaDraft,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    onAddMore: () -> Unit = {},
    onRemove: (Int) -> Unit = {},
    replyTitle: String? = null,
    replyExcerpt: String? = null,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val items = draft.items
    var caption by remember(items) { mutableStateOf("") }
    val pagerState = rememberPagerState(initialPage = 0) { items.size }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val hasMultiple = items.size > 1

    fun send() {
        onSend(caption.trim())
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            color = c.surface,
            modifier = modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            Column(Modifier.fillMaxSize()) {
                // Top bar: close, title, count, send.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIconButton(
                        icon = AppIcons.Close,
                        contentDescription = stringResource(R.string.conversation_attach_close_cd),
                        onClick = onDismiss,
                        tint = c.textPrimary,
                    )
                    Spacer(Modifier.width(PulseSpacing.xs))
                    Text(
                        text = stringResource(R.string.conversation_media_review_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (hasMultiple) {
                        Text(
                            text = stringResource(R.string.conversation_media_index, 1, items.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textTertiary,
                            modifier = Modifier.padding(end = PulseSpacing.md),
                        )
                    }
                    TextButton(onClick = ::send) {
                        Text(
                            text = stringResource(R.string.conversation_media_send),
                            style = MaterialTheme.typography.labelLarge,
                            color = c.accent,
                        )
                    }
                }

                // Media pages (weight fills between bars).
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = PulseSpacing.xs),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        MediaPreviewPage(items[page])
                    }
                }

                // Thumbnail strip with remove / add-more (multi-item only).
                if (hasMultiple) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = PulseSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
                        modifier = Modifier.padding(bottom = PulseSpacing.sm),
                    ) {
                        itemsIndexed(items) { index, item ->
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(PulseSizes.attachmentStrip)
                                        .clip(PulseShapes.md)
                                        .background(c.surfaceVariant)
                                        .clickable(
                                            role = Role.Button,
                                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                        ),
                                ) {
                                    AsyncImage(
                                        model = coilModel(item.uri, item.isVideo),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(c.mediaScrim)
                                        .clickable(
                                            role = Role.Button,
                                            onClickLabel = stringResource(R.string.conversation_media_remove_cd),
                                            onClick = { onRemove(index) },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Close,
                                        contentDescription = stringResource(
                                            R.string.conversation_media_remove_cd,
                                        ),
                                        modifier = Modifier.size(12.dp),
                                        tint = c.onMediaScrim,
                                    )
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(PulseSizes.attachmentStrip)
                                    .clip(PulseShapes.md)
                                    .background(c.surfaceVariant)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = stringResource(R.string.conversation_media_add_more_cd),
                                        onClick = onAddMore,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = AppIcons.Plus,
                                    contentDescription = stringResource(
                                        R.string.conversation_media_add_more_cd,
                                    ),
                                    modifier = Modifier.size(PulseIconSizes.inline),
                                    tint = c.textSecondary,
                                )
                            }
                        }
                    }
                }

                // Reply chip (context preserved from the composer).
                if (replyTitle != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.tight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .background(c.accent, RoundedCornerShape(2.dp)),
                        )
                        Spacer(Modifier.width(PulseSpacing.sm))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.conversation_reply_to, replyTitle),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.accent,
                                maxLines = 1,
                            )
                            if (!replyExcerpt.isNullOrBlank()) {
                                Text(
                                    text = replyExcerpt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                // Caption row + circular send.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PulseSpacing.md)
                        .navigationBarsPadding()
                        .padding(bottom = PulseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PulseShapes.full)
                            .background(c.surfaceVariant)
                            .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
                    ) {
                        BasicTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
                            cursorBrush = SolidColor(c.accent),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { send() }),
                            decorationBox = { inner ->
                                Box {
                                    if (caption.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.conversation_media_caption_hint),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.textTertiary,
                                        )
                                    }
                                    inner()
                                }
                            },
                        )
                    }
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Surface(
                        onClick = ::send,
                        shape = CircleShape,
                        color = c.accent,
                        modifier = Modifier.size(PulseSizes.sendCircle),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = AppIcons.Send,
                                contentDescription = stringResource(R.string.conversation_media_send_cd),
                                modifier = Modifier.size(PulseIconSizes.inline),
                                tint = c.onAccent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaPreviewPage(item: MediaItemDraft) {
    val c = PulseTheme.colors
    if (item.isVideo) {
        VideoPreviewBox(uri = item.uri, durationSeconds = item.durationSeconds)
    } else {
        AsyncImage(
            model = coilModel(item.uri, isVideo = false),
            contentDescription = stringResource(R.string.conversation_image_cd),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PulseSpacing.md),
        )
    }
}

/** 16:9 preview surface for a draft video (bundled thumb or icon). */
@Composable
private fun VideoPreviewBox(uri: String, durationSeconds: Int) {
    val c = PulseTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.md)
            .clip(PulseShapes.lg)
            .background(c.surfaceVariant),
    ) {
        AsyncImage(
            model = coilModel(uri, isVideo = true),
            contentDescription = stringResource(R.string.conversation_media_video_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(PulseSizes.videoPreviewHeight),
        )
        // Play affordance on a solid translucent scrim.
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(c.mediaScrim)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Play,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.feature),
                tint = c.onMediaScrim,
            )
        }
        if (durationSeconds > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(PulseSpacing.sm)
                    .clip(PulseShapes.sm)
                    .background(c.mediaScrim)
                    .padding(horizontal = PulseSpacing.sm, vertical = 2.dp),
            ) {
                Text(
                    text = ConversationFormat.durationLabel(durationSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onMediaScrim,
                )
            }
        }
    }
}

/* =====================================================================
 * S26 - Simulated camera (full-screen overlay). The "feed" is a rotating
 * bundled sample photo - no CameraX/preview and no CAMERA permission.
 * Tap the shutter = capture that photo; hold >= ~450 ms = record a video
 * (auto caps at 10 s) delivered as one bundled sample video. Captures
 * open the same review/caption sheet as picked media.
 * ===================================================================== */

internal val cameraSamplePhotoUris: List<String> = listOf(
    "sample://photos/photo_hills.jpg",
    "sample://photos/photo_city.jpg",
    "sample://photos/photo_park.jpg",
    "sample://photos/photo_tea.jpg",
    "sample://photos/photo_books.jpg",
    "sample://photos/photo_beach.jpg",
    "sample://photos/photo_route.jpg",
    "sample://photos/photo_palette.jpg",
)

internal val cameraSampleVideoUris: List<String> = listOf(
    "sample://videos/sample_video_1.mp4",
    "sample://videos/sample_video_2.mp4",
)

private const val VIDEO_HOLD_MS = 450L
private const val MAX_RECORD_MS = 10_000L
private const val MIN_VIDEO_MS = 1_000L
private const val CLOCK_TICK_MS = 100L

@Composable
fun CameraSimOverlay(
    onClose: () -> Unit,
    onPhoto: (uri: String) -> Unit,
    onVideo: (uri: String, durationSeconds: Int) -> Unit,
    onVideoTooShort: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors

    var photoIndex by remember { mutableIntStateOf(0) }
    var videoIndex by remember { mutableIntStateOf(0) }
    var recording by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var flashVisible by remember { mutableStateOf(false) }

    // Latest callbacks for the gesture/clock coroutines.
    val currentOnPhoto by rememberUpdatedState(onPhoto)
    val currentOnVideo by rememberUpdatedState(onVideo)
    val currentOnTooShort by rememberUpdatedState(onVideoTooShort)

    fun takePhoto() {
        if (recording) return
        val uri = cameraSamplePhotoUris[photoIndex % cameraSamplePhotoUris.size]
        photoIndex = (photoIndex + 1) % cameraSamplePhotoUris.size
        flashVisible = true
        currentOnPhoto(uri)
    }

    fun finishVideo() {
        if (elapsedMs < MIN_VIDEO_MS) {
            currentOnTooShort()
        } else {
            val seconds = ((elapsedMs + 500) / 1000).coerceIn(1, 10).toInt()
            val uri = cameraSampleVideoUris[videoIndex % cameraSampleVideoUris.size]
            videoIndex = (videoIndex + 1) % cameraSampleVideoUris.size
            currentOnVideo(uri, seconds)
        }
        elapsedMs = 0L
    }

    // Recording clock with a 10 s auto stop.
    LaunchedEffect(recording) {
        if (!recording) return@LaunchedEffect
        while (elapsedMs < MAX_RECORD_MS) {
            delay(CLOCK_TICK_MS)
            elapsedMs += CLOCK_TICK_MS
        }
        if (recording) {
            recording = false
            val seconds = ((elapsedMs + 500) / 1000).coerceIn(1, 10).toInt()
            val uri = cameraSampleVideoUris[videoIndex % cameraSampleVideoUris.size]
            videoIndex = (videoIndex + 1) % cameraSampleVideoUris.size
            currentOnVideo(uri, seconds)
        }
    }

    // Feed: current sample photo, full-bleed.
    AsyncImage(
        model = SampleMedia.imageModel(cameraSamplePhotoUris[photoIndex % cameraSamplePhotoUris.size]),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top chrome: close + recording status / title.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.mediaScrim)
                .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconButton(
                icon = AppIcons.Close,
                contentDescription = stringResource(R.string.conversation_camera_close_cd),
                onClick = onClose,
                tint = c.onMediaScrim,
            )
            Spacer(Modifier.weight(1f))
            if (recording) {
                Row(
                    modifier = Modifier
                        .clip(PulseShapes.full)
                        .background(c.mediaScrim)
                        .padding(horizontal = PulseSpacing.md, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(c.live),
                    )
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Text(
                        text = stringResource(R.string.conversation_camera_recording),
                        style = MaterialTheme.typography.labelMedium,
                        color = c.onMediaScrim,
                    )
                    Spacer(Modifier.width(PulseSpacing.md))
                    Text(
                        text = stringResource(
                            R.string.conversation_camera_timer,
                            (elapsedMs / 1000) / 60,
                            (elapsedMs / 1000) % 60,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onMediaScrim,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.conversation_attach_camera),
                    style = MaterialTheme.typography.titleMedium,
                    color = c.onMediaScrim,
                    modifier = Modifier.padding(end = 48.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))

        // Hint pill (solid translucent surface).
        Text(
            text = stringResource(
                if (recording) R.string.conversation_camera_hold_video
                else R.string.conversation_camera_tap_capture,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = c.onMediaScrim,
            modifier = Modifier
                .clip(PulseShapes.full)
                .background(c.mediaScrim)
                .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
        )
        Spacer(Modifier.height(PulseSpacing.lg))

        // Shutter: quick press = photo, hold = video record.
        Box(
            modifier = Modifier
                .size(PulseSizes.cameraShutter)
                .border(3.dp, c.onMediaScrim, CircleShape)
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(if (recording) c.live else c.onMediaScrim)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val start = SystemClock.uptimeMillis()
                            var longStarted = false
                            while (true) {
                                val held = SystemClock.uptimeMillis() - start
                                if (!longStarted && held >= VIDEO_HOLD_MS) {
                                    longStarted = true
                                    recording = true
                                    elapsedMs = 0L
                                }
                                val up = withTimeoutOrNull(50) {
                                    awaitPointerEvent().changes.any { it.changedToUp() }
                                } ?: false
                                if (up) {
                                    if (longStarted) {
                                        if (recording) {
                                            recording = false
                                            finishVideo()
                                        }
                                    } else {
                                        takePhoto()
                                    }
                                    break
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (recording) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(c.mediaScrim),
                    )
                }
            }
        }
        Spacer(Modifier.height(48.dp))
    }

    // Brief whiteout on photo capture (solid surface).
    if (flashVisible) {
        LaunchedEffect(flashVisible) {
            delay(110)
            flashVisible = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.onMediaScrim.copy(alpha = 0.85f)),
        )
    }
}
