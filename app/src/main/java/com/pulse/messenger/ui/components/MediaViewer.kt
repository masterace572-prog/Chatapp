@file:OptIn(ExperimentalFoundationApi::class)

package com.pulse.messenger.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.screens.conversation.ConversationRow
import com.pulse.messenger.ui.util.ConversationFormat
import com.pulse.messenger.ui.util.SampleMedia
import kotlinx.coroutines.delay
import java.io.File

/* =====================================================================
 * M4c S33 - Fullscreen media viewer: black immersive surface over the
 * conversation, horizontal pager over that chat's media in chronological
 * order, pinch/double-tap zoom for photos, VideoView playback for videos,
 * top chrome (sender/date), bottom actions (share/forward/delete/info),
 * caption overlay, tap toggles the chrome, back closes. Solid surfaces
 * only - no gradients.
 * ===================================================================== */

/** One flattenable media page (grid messages contribute one entry per image). */
data class ViewerMediaItem(
    val messageId: String,
    val uri: String,
    val isVideo: Boolean,
    val durationSeconds: Int,
    val caption: String,
    val senderName: String,
    val sentAtMillis: Long,
)

/** Builds the chronological media list from the newest-first message rows. */
fun buildViewerMedia(
    rows: List<ConversationRow.MessageItem>,
    userNames: Map<String, String>,
): List<ViewerMediaItem> {
    val flat = rows.flatMap { row ->
        val msg = row.message
        val sender = userNames[msg.senderId] ?: msg.senderId
        when (val content = msg.content) {
            is MessageContent.Image -> content.uris.mapIndexed { index, uri ->
                ViewerMediaItem(
                    messageId = msg.id,
                    uri = uri,
                    isVideo = false,
                    durationSeconds = 0,
                    caption = if (index == 0) content.caption.orEmpty() else "",
                    senderName = sender,
                    sentAtMillis = msg.sentAtMillis,
                )
            }
            is MessageContent.Video -> listOf(
                ViewerMediaItem(
                    messageId = msg.id,
                    uri = content.uri,
                    isVideo = true,
                    durationSeconds = content.durationSeconds,
                    caption = content.caption.orEmpty(),
                    senderName = sender,
                    sentAtMillis = msg.sentAtMillis,
                ),
            )
            else -> emptyList()
        }
    }
    return flat.reversed() // newest-first rows -> chronological pages
}

@Composable
fun MediaViewerOverlay(
    items: List<ViewerMediaItem>,
    initialIndex: Int,
    onClose: () -> Unit,
    onForward: (messageId: String) -> Unit,
    onDelete: (messageId: String) -> Unit,
    onInfo: (messageId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val c = PulseTheme.colors
    val context = LocalContext.current
    val safeIndex = initialIndex.coerceIn(0, items.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeIndex) { items.size }
    var chromeVisible by remember { mutableStateOf(true) }
    val current = items.getOrNull(pagerState.currentPage) ?: return

    // Immersive: hide the system bars for the viewer's lifetime.
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        runCatching { controller?.hide(WindowInsetsCompat.Type.systemBars()) }
        onDispose {
            runCatching { controller?.show(WindowInsetsCompat.Type.systemBars()) }
        }
    }

    BackHandler { onClose() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.viewerBackground)
            .systemBarsPadding(),
    ) {
        // Top chrome: close, sender + date. Hidden with the rest of the chrome.
        if (chromeVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.mediaScrim)
                    .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = AppIcons.ArrowLeft,
                        contentDescription = stringResource(R.string.conversation_viewer_close_cd),
                        tint = c.onMediaScrim,
                    )
                }
                Spacer(Modifier.width(PulseSpacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = current.senderName,
                        style = MaterialTheme.typography.titleSmall,
                        color = c.onMediaScrim,
                        maxLines = 1,
                    )
                    Text(
                        text = "${ConversationFormat.dayLabel(current.sentAtMillis)}, " +
                            ConversationFormat.clock(current.sentAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.onMediaScrim,
                        maxLines = 1,
                    )
                }
                if (items.size > 1) {
                    Text(
                        text = stringResource(
                            R.string.conversation_media_index,
                            pagerState.currentPage + 1,
                            items.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = c.onMediaScrim,
                        modifier = Modifier.padding(end = PulseSpacing.md),
                    )
                }
            }
        }

        // Pages: one pager over all chronological media of the chat.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(c.viewerBackground),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val item = items[page]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.isVideo) {
                        ViewerVideoPage(
                            item = item,
                            active = pagerState.currentPage == page,
                            chromeVisible = chromeVisible,
                            onToggleChrome = { chromeVisible = !chromeVisible },
                        )
                    } else {
                        ViewerPhotoPage(
                            item = item,
                            onToggleChrome = { chromeVisible = !chromeVisible },
                        )
                    }
                }
            }
        }

        // Caption overlay (above the action bar, bottom-aligned).
        if (chromeVisible && current.caption.isNotBlank()) {
            Text(
                text = current.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = c.onMediaScrim,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.mediaScrim)
                    .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
            )
        }

        // Bottom chrome: share / forward / delete / info.
        if (chromeVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.mediaScrim)
                    .navigationBarsPadding()
                    .padding(horizontal = PulseSpacing.xl, vertical = PulseSpacing.xs),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ViewerAction(
                    icon = AppIcons.Share,
                    label = stringResource(R.string.conversation_media_share_cd),
                    tint = c.onMediaScrim,
                    onClick = { shareViewerItem(context, current) },
                )
                ViewerAction(
                    icon = AppIcons.ArrowRight,
                    label = stringResource(R.string.conversation_action_forward),
                    tint = c.onMediaScrim,
                    onClick = { onForward(current.messageId) },
                )
                ViewerAction(
                    icon = AppIcons.Trash,
                    label = stringResource(R.string.conversation_action_delete),
                    tint = c.onMediaScrim,
                    onClick = { onDelete(current.messageId) },
                )
                ViewerAction(
                    icon = AppIcons.Info,
                    label = stringResource(R.string.conversation_action_info),
                    tint = c.onMediaScrim,
                    onClick = { onInfo(current.messageId) },
                )
            }
        }
    }
}

@Composable
private fun ViewerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(PulseSizes.minTouchTarget),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(PulseIconSizes.default),
            tint = tint,
        )
    }
}

/* =====================================================================
 * Photo page: Coil image with pinch-to-zoom (1..5x) and double-tap zoom,
 * single tap toggles the chrome. Solid viewer background behind it.
 * ===================================================================== */

@Composable
private fun ViewerPhotoPage(
    item: ViewerMediaItem,
    onToggleChrome: () -> Unit,
) {
    val c = PulseTheme.colors
    var scale by remember(item.uri) { mutableFloatStateOf(1f) }
    var offsetX by remember(item.uri) { mutableFloatStateOf(0f) }
    var offsetY by remember(item.uri) { mutableFloatStateOf(0f) }
    var bounds by remember(item.uri) { mutableStateOf(IntSize.Zero) }

    fun clampOffsets() {
        val limitX = if (bounds.width > 0) (scale - 1f) * bounds.width / 2f else 0f
        val limitY = if (bounds.height > 0) (scale - 1f) * bounds.height / 2f else 0f
        offsetX = offsetX.coerceIn(-limitX, limitX)
        offsetY = offsetY.coerceIn(-limitY, limitY)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { bounds = it }
            .pointerInput(item.uri) {
                detectTapGestures(
                    onTap = { onToggleChrome() },
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 2.5f
                            clampOffsets()
                        }
                    },
                )
            }
            .pointerInput(item.uri) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val next = (scale * zoom).coerceIn(1f, 5f)
                    if (next != scale) {
                        scale = next
                    } else {
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                    clampOffsets()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = SampleMedia.imageModel(item.uri) ?: item.uri,
            contentDescription = stringResource(R.string.conversation_image_cd),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
        )
    }
}

/* =====================================================================
 * Video page: VideoView with minimal controls - center play/pause,
 * thin progress bar, single tap toggles the chrome. Bundled samples play
 * from res/raw via a resource uri; picked videos play from their
 * persisted content uri.
 * ===================================================================== */

@Composable
private fun ViewerVideoPage(
    item: ViewerMediaItem,
    active: Boolean,
    chromeVisible: Boolean,
    onToggleChrome: () -> Unit,
) {
    val c = PulseTheme.colors
    val context = LocalContext.current
    var playing by remember(item.uri) { mutableStateOf(false) }
    var positionMs by remember(item.uri) { mutableLongStateOf(0L) }
    var durationMs by remember(item.uri) { mutableLongStateOf(0L) }
    var holder by remember(item.uri) { mutableStateOf<VideoView?>(null) }
    val videoUri = remember(item.uri) { videoUriFor(context, item.uri) }

    fun togglePlayback() {
        val vv = holder ?: return
        if (playing) {
            vv.pause()
            playing = false
        } else {
            if (vv.duration <= 0) vv.setVideoURI(videoUri)
            vv.start()
            playing = true
        }
    }

    // Progress ticker while playing.
    LaunchedEffect(playing, item.uri) {
        while (playing) {
            delay(250)
            holder?.let { vv ->
                positionMs = vv.currentPosition.toLong().coerceAtLeast(0L)
                if (durationMs > 0 && positionMs >= durationMs - 150) {
                    vv.pause()
                    playing = false
                }
            }
        }
    }

    // Stop when the page stops being the active page.
    LaunchedEffect(active, item.uri) {
        if (!active) {
            holder?.pause()
            playing = false
        }
    }
    DisposableEffect(item.uri) {
        onDispose {
            holder?.stopPlayback()
            holder = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item.uri) {
                detectTapGestures(onTap = { onToggleChrome() })
            },
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        durationMs = mp.duration.toLong().coerceAtLeast(0L)
                    }
                    holder = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )

        // Center play/pause over a solid translucent scrim.
        if (!playing || chromeVisible) {
            Surface(
                onClick = { togglePlayback() },
                shape = CircleShape,
                color = c.mediaScrim,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(PulseSizes.playerButton),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (playing) AppIcons.Pause else AppIcons.Play,
                        contentDescription = stringResource(
                            if (playing) R.string.conversation_voice_pause_cd
                            else R.string.conversation_video_play_cd,
                        ),
                        modifier = Modifier.size(PulseIconSizes.feature),
                        tint = c.onMediaScrim,
                    )
                }
            }
        }

        // Thin progress track along the bottom edge of the video area.
        val fraction =
            if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(c.mediaScrim)
                .padding(vertical = 3.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(3.dp)
                    .background(c.onMediaScrim),
            )
        }
    }
}

/* =====================================================================
 * Helpers
 * ===================================================================== */

/** Resolves sample video uris to a playable resource uri; content passthrough. */
private fun videoUriFor(context: Context, uri: String): Uri {
    if (uri.startsWith("sample://")) {
        val res = SampleMedia.videoRawRes(uri)
        if (res != null) {
            return Uri.parse("android.resource://${context.packageName}/$res")
        }
    }
    return Uri.parse(uri)
}

/** Shares one viewer item through the system share sheet (no permissions). */
private fun shareViewerItem(context: Context, item: ViewerMediaItem) {
    val uri = when {
        item.isVideo && item.uri.startsWith("sample://") -> {
            val res = SampleMedia.videoRawRes(item.uri)
            copyRawToCache(context, res, "shared", "pulse_video_${item.messageId}.mp4")
        }
        !item.isVideo && item.uri.startsWith("sample://") -> {
            val res = SampleMedia.photoRes(item.uri)
            copyRawToCache(context, res, "shared", "pulse_photo_${item.messageId}.jpg")
        }
        else -> null
    }
    val shareUri = uri ?: Uri.parse(item.uri)
    val mime = when {
        item.isVideo -> "video/mp4"
        item.uri.startsWith("sample://") -> "image/jpeg"
        else -> "image/*"
    }
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}

/** Copies a bundled raw/drawable resource into the FileProvider cache dir. */
private fun copyRawToCache(context: Context, resId: Int?, dir: String, name: String): Uri? {
    if (resId == null) return null
    val target = File(File(context.cacheDir, dir), name)
    return runCatching {
        target.parentFile?.mkdirs()
        context.resources.openRawResource(resId).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.pulsefiles",
            target,
        )
    }.getOrNull()
}
