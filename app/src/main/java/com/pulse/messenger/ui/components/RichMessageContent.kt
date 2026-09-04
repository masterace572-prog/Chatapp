@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.pulse.messenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.LinkPreview
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.ConversationFormat
import com.pulse.messenger.ui.util.SampleMedia

/**
 * M4c rich message bodies: real rendering for IMAGE (single + 2/3/4+ grids
 * with "+N" scrim), VIDEO (thumb, play control, duration chip), FILE (tile
 * tinted by extension family), LOCATION (static map placeholder + live
 * shares), CONTACT, POLL (multi/anonymous/quiz with reveal), STICKER and the
 * LINK-PREVIEW card drawn under text bubbles.
 *
 * All composables are stateless; colors come from PulseTheme.colors only.
 * MessageBubble owns corners/tails, taps, flash, reactions and meta rows.
 */

/* ============================ IMAGE ============================ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImageMessageBody(
    message: Message,
    isOutgoing: Boolean,
    maxBubbleWidth: Dp,
    onImageTap: ((Int) -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    val image = message.content as MessageContent.Image
    val caption = image.caption.trim()
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    val portrait = image.uris.size == 1 && image.widthPx > 0 && image.heightPx > 0 &&
        image.widthPx < image.heightPx

    val widthMod = if (portrait) {
        Modifier.width((maxBubbleWidth * 0.68f).coerceIn(160.dp, 260.dp))
    } else {
        Modifier.fillMaxWidth()
    }

    Column(widthMod) {
        Box(Modifier.fillMaxWidth()) {
            when {
                image.uris.size == 1 -> SingleImageTile(
                    uri = image.uris[0],
                    aspect = imageAspect(image),
                    onTap = { onImageTap?.invoke(0) },
                    onLongPress = onLongPress,
                )

                image.uris.size == 2 -> TwoImageTiles(uris = image.uris, onTap = onImageTap, onLongPress = onLongPress)
                image.uris.size == 3 -> ThreeImageTiles(uris = image.uris, onTap = onImageTap, onLongPress = onLongPress)
                else -> FourPlusImageTiles(uris = image.uris, onTap = onImageTap, onLongPress = onLongPress)
            }
            if (caption.isEmpty()) {
                MetaPill(message = message, isOutgoing = isOutgoing)
            }
            UploadStrip(upload = message.uploadProgress)
        }

        if (caption.isNotEmpty() && !message.isDeleted) {
            Column(
                modifier = widthMod.padding(start = PulseSpacing.md, end = PulseSpacing.md, top = PulseSpacing.sm),
            ) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                )
                BubbleMetaRow(message = message, isOutgoing = isOutgoing, onSurface = false)
            }
        }
    }
}

private fun imageAspect(image: MessageContent.Image): Float {
    if (image.widthPx <= 0 || image.heightPx <= 0) return 1f
    val raw = image.widthPx.toFloat() / image.heightPx
    // Portrait keeps its true ratio (cap ~1/0.68 width lane); landscape is
    // capped so a single row never exceeds ~320dp of height.
    return if (raw < 1f) raw.coerceAtLeast(0.62f) else raw.coerceAtMost(1.8f)
}

@Composable
private fun BoxScope.SingleImageTile(
    uri: String,
    aspect: Float,
    onTap: (() -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    val c = PulseTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .background(c.surfaceVariant)
            .then(
                if (onTap != null || onLongPress != null) {
                    Modifier.combinedClickable(onClick = { onTap?.invoke() }, onLongClick = onLongPress)
                } else Modifier,
            ),
    ) {
        BubbleImage(uri = uri)
    }
}

@Composable
private fun BoxScope.BubbleImage(uri: String) {
    val c = PulseTheme.colors
    val model = SampleMedia.imageModel(uri)
    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(AppIcons.Image, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BoxScope.TwoImageTiles(
    uris: List<String>,
    onTap: ((Int) -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    Row(Modifier.fillMaxWidth()) {
        uris.forEachIndexed { i, uri ->
            GridTile(uri = uri, modifier = Modifier.weight(1f).aspectRatio(1f), index = i, onTap = onTap, onLongPress = onLongPress)
            if (i == 0) Spacer(Modifier.width(2.dp))
        }
    }
}

@Composable
private fun BoxScope.ThreeImageTiles(
    uris: List<String>,
    onTap: ((Int) -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    Row(Modifier.fillMaxWidth()) {
        GridTile(
            uri = uris[0],
            modifier = Modifier.weight(1.15f).aspectRatio(0.75f),
            index = 0,
            onTap = onTap,
            onLongPress = onLongPress,
        )
        Spacer(Modifier.width(2.dp))
        Column(Modifier.weight(1f).fillMaxHeight()) {
            GridTile(uri = uris[1], modifier = Modifier.weight(1f).fillMaxWidth(), index = 1, onTap = onTap, onLongPress = onLongPress)
            Spacer(Modifier.height(2.dp))
            GridTile(uri = uris[2], modifier = Modifier.weight(1f).fillMaxWidth(), index = 2, onTap = onTap, onLongPress = onLongPress)
        }
    }
}

@Composable
private fun BoxScope.FourPlusImageTiles(
    uris: List<String>,
    onTap: ((Int) -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    val extra = uris.size - 4
    val shown = uris.take(4)
    val rows = shown.chunked(2)
    Column(Modifier.fillMaxWidth()) {
        rows.forEachIndexed { rIndex, row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEachIndexed { colIndex, uri ->
                    val index = rIndex * 2 + colIndex
                    val isLast = index == 3
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        GridTile(
                            uri = uri,
                            modifier = Modifier.fillMaxSize(),
                            index = index,
                            onTap = onTap,
                            onLongPress = onLongPress,
                        )
                        if (isLast && extra > 0) {
                            // "+N" translucent overlay on the final visible tile.
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(PulseTheme.colors.mediaScrim),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "+$extra",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PulseTheme.colors.onMediaScrim,
                                )
                            }
                        }
                    }
                }
            }
            if (rIndex < rows.lastIndex) Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun GridTile(
    uri: String,
    modifier: Modifier,
    index: Int,
    onTap: ((Int) -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    val c = PulseTheme.colors
    Box(
        modifier = modifier
            .background(c.surfaceVariant)
            .then(
                if (onTap != null || onLongPress != null) {
                    Modifier.combinedClickable(onClick = { onTap?.invoke(index) }, onLongClick = onLongPress)
                } else Modifier,
            ),
    ) {
        BubbleImage(uri = uri)
    }
}

/** Thin determinate upload strip along the media's bottom edge. */
@Composable
private fun BoxScope.UploadStrip(upload: Float?) {
    if (upload == null) return
    val c = PulseTheme.colors
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(3.dp)
            .background(c.mediaScrim),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(upload.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(c.onMediaScrim.copy(alpha = 0.9f)),
        )
    }
}

/* ============================ VIDEO ============================ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun VideoMessageBody(
    message: Message,
    isOutgoing: Boolean,
    onVideoTap: (() -> Unit)?,
    onLongPress: (() -> Unit)?,
) {
    val video = message.content as MessageContent.Video
    val caption = video.caption.trim()
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    val aspect = if (video.widthPx > 0 && video.heightPx > 0) {
        (video.widthPx.toFloat() / video.heightPx).coerceIn(0.8f, 1.8f)
    } else {
        4f / 3f
    }

    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .background(c.surfaceVariant)
                .then(
                    if (onVideoTap != null || onLongPress != null) {
                        Modifier.combinedClickable(onClick = { onVideoTap?.invoke() }, onLongClick = onLongPress)
                    } else Modifier,
                ),
        ) {
            val thumb = SampleMedia.videoThumbRes(video.uri)
            if (thumb != null) {
                AsyncImage(
                    model = thumb,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(AppIcons.Video, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(32.dp))
                }
            }
            // Centered translucent play control (solid surface, no gradient).
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(c.mediaScrim),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.Play,
                    contentDescription = stringResource(R.string.conversation_video_play_cd),
                    tint = c.onMediaScrim,
                    modifier = Modifier.size(22.dp),
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.mediaScrim)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ConversationFormat.durationLabel(video.durationSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onMediaScrim,
                )
            }
            if (caption.isEmpty()) {
                MetaPill(message = message, isOutgoing = isOutgoing)
            }
            UploadStrip(upload = message.uploadProgress)
        }
        if (caption.isNotEmpty() && !message.isDeleted) {
            Column(Modifier.padding(start = PulseSpacing.md, end = PulseSpacing.md, top = PulseSpacing.sm)) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                )
                BubbleMetaRow(message = message, isOutgoing = isOutgoing, onSurface = false)
            }
        }
    }
}

/* ============================ STICKER ============================ */

@Composable
internal fun StickerMessageBody(message: Message, isOutgoing: Boolean) {
    val sticker = message.content as MessageContent.Sticker
    val res = SampleMedia.stickerRes(sticker.assetKey)
    val c = PulseTheme.colors
    if (res == null) {
        // Truly unknown key: graceful icon tile (no unsupported placeholders).
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(c.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(AppIcons.Smile, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(28.dp))
                Text(
                    text = stringResource(R.string.conversation_sticker_cd),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textTertiary,
                    modifier = Modifier.padding(top = PulseSpacing.xs),
                )
            }
        }
        return
    }
    Box(modifier = Modifier.width(160.dp).height(160.dp)) {
        AsyncImage(model = res, contentDescription = null, modifier = Modifier.fillMaxSize())
        if (!message.isDeleted) {
            MetaPill(message = message, isOutgoing = isOutgoing)
        }
    }
}

/* ============================ FILE ============================ */

/** File tile inside a surfaced bubble; tile tint follows the extension family. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileMessageBody(message: Message, isOutgoing: Boolean, onFileTap: (() -> Unit)?) {
    val file = message.content as MessageContent.File
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    val subColor = if (isOutgoing) c.onAccent.copy(alpha = 0.8f) else c.textTertiary

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onFileTap != null) Modifier.combinedClickable(onClick = onFileTap) else Modifier)
                .padding(vertical = PulseSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fileTileColor(file.mimeType, file.name)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.File,
                    contentDescription = stringResource(R.string.conversation_file_open_cd),
                    tint = c.onFile,
                    modifier = Modifier.size(PulseIconSizes.inline),
                )
            }
            Spacer(Modifier.width(PulseSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = fileKindLabel(file.mimeType) + " · " + ConversationFormat.bytesLabel(file.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        val upload = message.uploadProgress
        if (upload != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(c.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(upload.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(if (isOutgoing) c.onAccent else c.accent),
                )
            }
        }
    }
}

@Composable
private fun fileKindLabel(mimeType: String): String = when {
    mimeType.contains("pdf") -> stringResource(R.string.conversation_file_type_pdf)
    mimeType.contains("word") || mimeType.contains("document") || mimeType.contains("officedocument.wordprocessingml") ->
        stringResource(R.string.conversation_file_type_doc)
    mimeType.contains("sheet") || mimeType.contains("excel") || mimeType.contains("spreadsheetml") ||
        mimeType.contains("csv") -> stringResource(R.string.conversation_file_type_sheet)
    mimeType.contains("zip") || mimeType.contains("rar") || mimeType.contains("7z") || mimeType.contains("tar") ||
        mimeType.contains("gzip") -> stringResource(R.string.conversation_file_type_archive)
    mimeType.startsWith("audio/") -> stringResource(R.string.conversation_file_type_audio)
    else -> stringResource(R.string.conversation_file_type_file)
}

@Composable
private fun fileTileColor(mimeType: String, name: String): Color {
    val c = PulseTheme.colors
    val lower = name.lowercase()
    return when {
        mimeType.contains("pdf") || lower.endsWith(".pdf") -> c.filePdf
        mimeType.contains("word") || lower.endsWith(".docx") || lower.endsWith(".doc") -> c.fileDoc
        mimeType.contains("sheet") || mimeType.contains("excel") || lower.endsWith(".xlsx") ||
            lower.endsWith(".xls") || lower.endsWith(".csv") -> c.fileSheet
        mimeType.contains("zip") || mimeType.contains("rar") || lower.endsWith(".zip") ||
            mimeType.contains("gzip") || mimeType.contains("tar") -> c.fileArchive
        mimeType.startsWith("audio/") || lower.endsWith(".mp3") || lower.endsWith(".wav") ||
            lower.endsWith(".m4a") || lower.endsWith(".ogg") -> c.fileAudio
        else -> c.fileGeneric
    }
}

/* ============================ LOCATION ============================ */

@Composable
internal fun LocationMessageBody(message: Message, isOutgoing: Boolean) {
    val location = message.content as MessageContent.Location
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    val address = location.address.trim()

    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(c.surfaceVariant),
        ) {
            MapPlaceholderGrid()
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(c.mediaScrim),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.MapPin, contentDescription = null, tint = c.onMediaScrim, modifier = Modifier.size(26.dp))
            }
            LiveChip(message = message)
            if (address.isEmpty()) {
                MetaPill(message = message, isOutgoing = isOutgoing)
            }
        }
        if (address.isNotEmpty() && !message.isDeleted) {
            Column(Modifier.padding(start = PulseSpacing.md, end = PulseSpacing.md, top = PulseSpacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = AppIcons.MapPin,
                        contentDescription = null,
                        tint = if (isOutgoing) c.onAccent else c.accent,
                        modifier = Modifier.size(PulseIconSizes.inline),
                    )
                    Spacer(Modifier.width(PulseSpacing.xs))
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
                BubbleMetaRow(message = message, isOutgoing = isOutgoing, onSurface = false)
            }
        }
    }
}

/** Static map placeholder: center pin over a subtle grid (solid colors only). */
@Composable
private fun BoxScope.MapPlaceholderGrid() {
    val c = PulseTheme.colors
    Canvas(Modifier.fillMaxSize()) {
        val step = 28.dp.toPx()
        val line = c.border
        var x = 0f
        while (x < size.width) {
            drawLine(line, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

/** Live-location chip: countdown while active, "ended" once expired. */
@Composable
private fun BoxScope.LiveChip(message: Message) {
    val location = message.content as MessageContent.Location
    if (!location.isLive) return
    val c = PulseTheme.colors
    val endsAt = message.sentAtMillis + (location.liveDurationSeconds ?: 0) * 1000L
    val active = endsAt > System.currentTimeMillis()
    val label = if (active) {
        stringResource(R.string.conversation_live_label) + " · " + ConversationFormat.remainingLabel(endsAt - System.currentTimeMillis())
    } else {
        stringResource(R.string.conversation_live_ended)
    }
    Row(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(c.mediaScrim)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (active) c.live else c.textTertiary),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (active) c.onMediaScrim else c.onMediaScrim.copy(alpha = 0.7f),
        )
    }
}

/* ============================ CONTACT ============================ */

@Composable
internal fun ContactMessageBody(message: Message, isOutgoing: Boolean) {
    val contact = message.content as MessageContent.Contact
    val c = PulseTheme.colors
    val ink = if (isOutgoing) c.onAccent else c.textPrimary
    val subColor = if (isOutgoing) c.onAccent.copy(alpha = 0.85f) else c.textSecondary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            name = contact.displayName,
            avatarTone = AvatarTones.bySeed(contact.userId.hashCode()),
            size = 44.dp,
        )
        Spacer(Modifier.width(PulseSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val sub = when {
                !contact.phone.isNullOrBlank() -> contact.phone
                !contact.username.isNullOrBlank() -> "@" + contact.username
                else -> stringResource(R.string.conversation_file_type_file)
            }
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = subColor,
                maxLines = 1,
            )
        }
        if (!contact.phone.isNullOrBlank()) {
            Icon(
                imageVector = AppIcons.Phone,
                contentDescription = null,
                tint = subColor,
                modifier = Modifier.size(PulseIconSizes.small),
            )
        }
    }
}

/* ============================ POLL ============================ */

/**
 * Poll card. Totals are revealed once "me" has voted (or immediately for my
 * own outgoing polls). Quiz mode tints the correct option with the success
 * token after answering; anonymous polls only show counts, never names.
 */
@Composable
internal fun PollMessageBody(
    message: Message,
    onVote: (List<Int>) -> Unit,
    onRetract: () -> Unit,
) {
    val poll = message.content as MessageContent.Poll
    val c = PulseTheme.colors
    val myOptions = poll.votes.entries.filter { it.value.contains("me") }.map { it.key }.toSet()
    val revealed = myOptions.isNotEmpty() || message.isOutgoing
    val totalVotes = poll.votes.values.sumOf { it.size }

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.surface).border(1.dp, c.border, RoundedCornerShape(12.dp))) {
        Column(Modifier.padding(horizontal = PulseSpacing.md)) {
            Text(
                text = poll.question,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = c.textPrimary,
            )
            Row(Modifier.padding(top = PulseSpacing.tight), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pluralStringResource(R.plurals.conversation_poll_votes, totalVotes, totalVotes),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textTertiary,
                )
                if (poll.isQuiz && revealed) {
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Text(
                        text = stringResource(R.string.conversation_poll_quiz),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = c.warning,
                    )
                }
                if (poll.isAnonymous && revealed) {
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Text(
                        text = stringResource(R.string.conversation_poll_anonymous),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                    )
                }
            }
        }
        Column(Modifier.padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.sm)) {
            poll.options.forEachIndexed { index, option ->
                val count = poll.votes[index].orEmpty().size
                val mine = index in myOptions
                val correct = poll.isQuiz && revealed && poll.correctOptionIndex == index
                val rowShape = RoundedCornerShape(10.dp)
                val rowColor = when {
                    correct -> c.successContainer
                    mine -> c.accentContainer
                    else -> c.surfaceVariant
                }
                val voteAction: (() -> Unit)? = if (!revealed) {
                    {
                        if (poll.multipleAnswers) {
                            val next = myOptions.toMutableSet()
                            if (!next.add(index)) next.remove(index)
                            if (next.isEmpty()) onRetract() else onVote(next.toList())
                        } else {
                            onVote(listOf(index))
                        }
                    }
                } else {
                    null
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(rowShape)
                        .background(rowColor)
                        .then(if (voteAction != null) Modifier.clickable(onClick = voteAction) else Modifier)
                        .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val markerBg = when {
                        correct -> c.success
                        mine -> c.accent
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(markerBg)
                            .border(1.dp, if (markerBg == Color.Transparent) c.border else Color.Transparent, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (markerBg != Color.Transparent) {
                            Icon(AppIcons.Check, contentDescription = null, tint = c.onFile, modifier = Modifier.size(13.dp))
                        }
                    }
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (revealed) {
                        Spacer(Modifier.width(PulseSpacing.sm))
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mine || correct) c.textPrimary else c.textTertiary,
                        )
                    }
                }
                if (revealed && totalVotes > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .padding(start = PulseSpacing.md, end = PulseSpacing.md)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c.surfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(count.toFloat() / totalVotes)
                                .fillMaxHeight()
                                .background(if (correct) c.success else c.accent),
                        )
                    }
                }
            }
        }
        Column(Modifier.padding(start = PulseSpacing.md, end = PulseSpacing.md, bottom = PulseSpacing.sm)) {
            BubbleMetaRow(message = message, isOutgoing = false, onSurface = true)
        }
    }
}

/* ============================ LINK PREVIEW ============================ */

/** Card under a text bubble when URL metadata exists (tap opens the URL). */
@Composable
internal fun LinkPreviewCard(
    preview: LinkPreview,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val uriHandler = LocalUriHandler.current
    val host = runCatching {
        android.net.Uri.parse(preview.url).host?.removePrefix("www.") ?: preview.url
    }.getOrDefault(preview.url)
    val ink = if (isOutgoing) c.onAccent else c.textPrimary
    val sub = if (isOutgoing) c.onAccent.copy(alpha = 0.8f) else c.textTertiary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOutgoing) c.onAccent.copy(alpha = 0.12f) else c.surface)
            .clickable { uriHandler.openUri(preview.url) }
            .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = host,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isOutgoing) c.onAccent.copy(alpha = 0.85f) else c.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = AppIcons.ExternalLink,
                contentDescription = null,
                tint = if (isOutgoing) c.onAccent.copy(alpha = 0.8f) else c.textTertiary,
                modifier = Modifier.size(PulseIconSizes.inline),
            )
        }
        if (!preview.title.isNullOrBlank()) {
            Text(
                text = preview.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (!preview.description.isNullOrBlank()) {
            Text(
                text = preview.description,
                style = MaterialTheme.typography.bodySmall,
                color = sub,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/* ============================ META SHARED ============================ */

/** Scrim pill with clock + ticks at the bottom-end of media/stickers. */
@Composable
private fun BoxScope.MetaPill(message: Message, isOutgoing: Boolean) {
    MetaChips(
        message = message,
        isOutgoing = isOutgoing,
        onScrim = true,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PulseTheme.colors.mediaScrim)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

/** Inline meta (star / edited / clock / ticks) below bubble content. */
@Composable
internal fun ColumnScope.BubbleMetaRow(
    message: Message,
    isOutgoing: Boolean,
    onSurface: Boolean = false,
) {
    MetaChips(
        message = message,
        isOutgoing = isOutgoing,
        onScrim = false,
        onSurface = onSurface,
        modifier = Modifier
            .align(Alignment.End)
            .padding(top = PulseSpacing.tight),
    )
}

@Composable
private fun MetaChips(
    message: Message,
    isOutgoing: Boolean,
    onScrim: Boolean,
    modifier: Modifier = Modifier,
    onSurface: Boolean = false,
) {
    val c = PulseTheme.colors
    val metaColor = when {
        onScrim -> c.onMediaScrim.copy(alpha = 0.9f)
        onSurface -> c.textTertiary
        isOutgoing -> c.onAccent.copy(alpha = 0.8f)
        else -> c.textTertiary
    }
    val starColor = if (onSurface || !isOutgoing) c.warning else c.onAccent.copy(alpha = 0.9f)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (message.isStarred && !message.isDeleted) {
            Icon(
                imageVector = AppIcons.Star,
                contentDescription = null,
                modifier = Modifier.size(13.dp).padding(end = 4.dp),
                tint = starColor,
            )
        }
        if (message.isEdited && !message.isDeleted) {
            Text(
                text = stringResource(R.string.conversation_edited),
                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                color = metaColor,
                modifier = Modifier.padding(end = PulseSpacing.xs),
            )
        }
        Text(
            text = ConversationFormat.clock(message.sentAtMillis),
            style = MaterialTheme.typography.labelSmall,
            color = metaColor,
        )
        if (isOutgoing) {
            Spacer(Modifier.width(4.dp))
            DeliveryTicks(
                status = message.status,
                tint = if (onScrim) c.onMediaScrim.copy(alpha = 0.8f) else if (onSurface) c.textTertiary else c.onAccent.copy(alpha = 0.75f),
                readTint = if (onScrim) c.onMediaScrim else if (onSurface) c.textSecondary else c.onAccent,
            )
        }
    }
}

/* ============================ UNSURFACED ORCHESTRATOR ============================ */

/**
 * Container for bubbles without the classic text-bubble surface: image /
 * video / location keep the bubble color + corner/tail shape as a backing
 * behind flush media; stickers stay fully transparent; polls render as their
 * own surface card. Quote strips and the flash overlay are drawn on top.
 */
@Composable
internal fun RichUnsurfacedBubble(
    message: Message,
    isOutgoing: Boolean,
    shape: Shape,
    maxBubbleWidth: Dp,
    quote: BubbleQuoteData?,
    onQuoteTap: (() -> Unit)?,
    flashAlpha: Float,
    onImageTap: ((Int) -> Unit)?,
    onVideoTap: (() -> Unit)?,
    onLongPress: (() -> Unit)?,
    onPollVote: ((List<Int>) -> Unit)?,
    onPollRetract: (() -> Unit)?,
) {
    val c = PulseTheme.colors
    val content = message.content
    val bubbleColor = if (isOutgoing) c.accent else c.surfaceVariant

    Box {
        when (content) {
            is MessageContent.Image -> {
                Column(Modifier.clip(shape).background(bubbleColor)) {
                    QuoteStrip(message, quote, isOutgoing, onQuoteTap)
                    ImageMessageBody(
                        message = message,
                        isOutgoing = isOutgoing,
                        maxBubbleWidth = maxBubbleWidth,
                        onImageTap = onImageTap,
                        onLongPress = onLongPress,
                    )
                }
            }

            is MessageContent.Video -> {
                Column(Modifier.clip(shape).background(bubbleColor)) {
                    QuoteStrip(message, quote, isOutgoing, onQuoteTap)
                    VideoMessageBody(
                        message = message,
                        isOutgoing = isOutgoing,
                        onVideoTap = onVideoTap,
                        onLongPress = onLongPress,
                    )
                }
            }

            is MessageContent.Location -> {
                Column(
                    Modifier
                        .clip(shape)
                        .background(bubbleColor)
                        .then(if (onLongPress != null) Modifier.combinedClickable(onClick = {}, onLongClick = onLongPress) else Modifier),
                ) {
                    QuoteStrip(message, quote, isOutgoing, onQuoteTap)
                    LocationMessageBody(message = message, isOutgoing = isOutgoing)
                }
            }

            is MessageContent.Sticker -> {
                Column(
                    Modifier
                        .then(if (onLongPress != null) Modifier.combinedClickable(onClick = {}, onLongClick = onLongPress) else Modifier),
                ) {
                    StickerMessageBody(message = message, isOutgoing = isOutgoing)
                }
            }

            is MessageContent.Poll -> {
                Column(Modifier.clip(shape)) {
                    PollMessageBody(
                        message = message,
                        onVote = { indexes -> onPollVote?.invoke(indexes) },
                        onRetract = { onPollRetract?.invoke() },
                    )
                }
            }

            else -> Unit
        }

        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(c.accentContainer.copy(alpha = flashAlpha)),
            )
        }
    }
}

@Composable
private fun QuoteStrip(
    message: Message,
    quote: BubbleQuoteData?,
    isOutgoing: Boolean,
    onQuoteTap: (() -> Unit)?,
) {
    if (quote == null || message.isDeleted) return
    BubbleQuote(
        message = message,
        quote = quote,
        isOutgoing = isOutgoing,
        onClick = onQuoteTap,
        modifier = Modifier.padding(start = PulseSpacing.md, end = PulseSpacing.md, top = PulseSpacing.sm),
    )
}
