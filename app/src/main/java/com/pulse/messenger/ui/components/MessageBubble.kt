package com.pulse.messenger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageContent
import com.pulse.messenger.domain.model.MessageStatus
import com.pulse.messenger.domain.model.MessageType
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseBubbleShape
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.ConversationFormat
import com.pulse.messenger.ui.util.MessageLabels

private val URL_REGEX = Regex("""https?://[^\s<>"']+""")
private val MENTION_REGEX = Regex("""@\w+""")

/**
 * Resolved reply-quote data for one bubble (sender + excerpt + kind).
 * Non-text targets render their type icon + label instead of an excerpt.
 */
data class BubbleQuoteData(
    val senderName: String,
    val text: String,
    val isText: Boolean,
    val type: MessageType,
)

/**
 * One chat bubble (PRD §7 MessageBubble + S23).
 *
 * M4a core: text with tappable links + accent mentions, content-type
 * placeholders, run geometry, delivery ticks, retry.
 * M4b additions: reply quote (tap scrolls to the original), "Forwarded"
 * label, star marker next to the time, reactions pill row, flash highlight
 * (scroll-to target), long-press / selection hooks and the voice-note
 * content with simulated playback.
 *
 * Stateless: the screen resolves quote data, names, colours, run geometry
 * and passes the callbacks.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    isFirstInRun: Boolean = true,
    isLastInRun: Boolean = true,
    senderName: String? = null,
    senderNameColor: Color? = null,
    avatarSeed: Int? = null,
    onRetry: (() -> Unit)? = null,
    quote: BubbleQuoteData? = null,
    onQuoteTap: (() -> Unit)? = null,
    flashSignal: Int = 0,
    onLongPress: (() -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    onToggleReaction: (String) -> Unit = {},
    onReactionLongPress: (String) -> Unit = {},
    voicePlaying: Boolean = false,
    onVoiceToggle: () -> Unit = {},
    onImageTap: ((Int) -> Unit)? = null,
    onVideoTap: (() -> Unit)? = null,
    onFileTap: (() -> Unit)? = null,
    onPollVote: ((List<Int>) -> Unit)? = null,
    onPollRetract: (() -> Unit)? = null,
) {
    val isOutgoing = message.isOutgoing
    val c = PulseTheme.colors
    val bubbleShape = PulseBubbleShape(isOutgoing, isFirstInRun, isLastInRun)

    // avatarSeed non-null = incoming GROUP rows: avatar on the run's last
    // bubble; the other rows reserve the gutter so bubbles stay aligned.
    val isGroupRow = !isOutgoing && avatarSeed != null
    val showAvatar = isGroupRow && isLastInRun

    // Scroll-target flash (accentContainer overlay fading out over ~600ms).
    var flashAlpha by remember { mutableStateOf(0f) }
    LaunchedEffect(flashSignal) {
        if (flashSignal > 0) {
            val anim = Animatable(0.45f)
            flashAlpha = 0.45f
            anim.animateTo(0f, animationSpec = tween(600))
            flashAlpha = 0f
        }
    }

    val clickModifier = if (onLongPress != null || onTap != null) {
        Modifier.combinedClickable(
            onClick = { onTap?.invoke() },
            onLongClick = onLongPress,
        )
    } else {
        Modifier
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxBubbleWidth = maxWidth * 0.78f
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
        ) {
            if (isGroupRow) {
                if (showAvatar) {
                    Avatar(
                        name = senderName,
                        avatarTone = AvatarTones.bySeed(avatarSeed ?: 0),
                        size = PulseSizes.avatarRun,
                        modifier = Modifier.padding(bottom = PulseSpacing.xs),
                    )
                } else {
                    Spacer(Modifier.width(PulseSizes.avatarRun))
                }
                Spacer(Modifier.width(PulseSpacing.xs))
            }

            Column(Modifier.widthIn(max = maxBubbleWidth)) {
                if (senderName != null && !isOutgoing && !message.isSystem) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = senderNameColor ?: c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = PulseSpacing.xs, bottom = PulseSpacing.tight),
                    )
                }

                val unsurfaced = !message.isDeleted && when (message.content) {
                    is MessageContent.Image,
                    is MessageContent.Video,
                    is MessageContent.Sticker,
                    is MessageContent.Location,
                    is MessageContent.Poll,
                    -> true
                    else -> false
                }
                if (unsurfaced) {
                    RichUnsurfacedBubble(
                        message = message,
                        isOutgoing = isOutgoing,
                        shape = bubbleShape,
                        maxBubbleWidth = maxBubbleWidth,
                        quote = quote,
                        onQuoteTap = onQuoteTap,
                        flashAlpha = flashAlpha,
                        onImageTap = onImageTap,
                        onVideoTap = onVideoTap,
                        onLongPress = onLongPress,
                        onPollVote = onPollVote,
                        onPollRetract = onPollRetract,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(color = bubbleColor(message, isOutgoing), shape = bubbleShape)
                            .then(clickModifier)
                            .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
                    ) {
                        Column {
                            BubbleQuote(message = message, quote = quote, isOutgoing = isOutgoing, onClick = onQuoteTap)
                            BubbleContent(
                                message = message,
                                isOutgoing = isOutgoing,
                                voicePlaying = voicePlaying,
                                onVoiceToggle = onVoiceToggle,
                                onFileTap = onFileTap,
                            )
                            BubbleMetaRow(message = message, isOutgoing = isOutgoing)
                        }
                        // Flash overlay (drawn above content while fading).
                        if (flashAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(bubbleShape)
                                    .background(c.accentContainer.copy(alpha = flashAlpha)),
                            )
                        }
                    }
                }

                if (message.status == MessageStatus.Failed && message.isOutgoing) {
                    RetryRow(onRetry = onRetry)
                }

                // Reactions pill row under the bubble (own side alignment).
                val pills = if (message.isDeleted) emptyList() else reactionPills(message)
                if (pills.isNotEmpty()) {
                    ReactionPillRow(
                        pills = pills,
                        isOutgoing = isOutgoing,
                        modifier = Modifier.padding(top = PulseSpacing.xs),
                        onToggle = onToggleReaction,
                        onLongPress = onReactionLongPress,
                    )
                }
            }
        }
    }
}

@Composable
private fun bubbleColor(message: Message, isOutgoing: Boolean): Color {
    val c = PulseTheme.colors
    return if (isOutgoing) c.accent else c.surfaceVariant
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BubbleQuote(
    message: Message,
    quote: BubbleQuoteData?,
    isOutgoing: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (quote == null || message.isDeleted || message.isSystem) return
    val c = PulseTheme.colors
    val accent = if (isOutgoing) c.onAccent else c.accent
    val quoteColor = if (isOutgoing) c.onAccent.copy(alpha = 0.92f) else c.textPrimary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.combinedClickable(onClick = onClick) else Modifier)
            .padding(bottom = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(accent),
        )
        Spacer(Modifier.width(PulseSpacing.sm))
        Column(Modifier.weight(1f)) {
            Text(
                text = quote.senderName,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (quote.isText) {
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = quoteColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = iconForType(quote.type),
                        contentDescription = null,
                        modifier = Modifier.size(PulseIconSizes.small),
                        tint = if (isOutgoing) c.onAccent.copy(alpha = 0.85f) else c.textSecondary,
                    )
                    Spacer(Modifier.width(PulseSpacing.xs))
                    Text(
                        text = MessageLabels.bubbleLabel(quote.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = quoteColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BubbleContent(
    message: Message,
    isOutgoing: Boolean,
    voicePlaying: Boolean,
    onVoiceToggle: () -> Unit,
    onFileTap: (() -> Unit)?,
) {
    Column {
        // Forwarded label above the content (hidden for deleted/system).
        if (message.forwardedFromUserId != null && !message.isDeleted && !message.isSystem) {
            ForwardedLabel(isOutgoing = isOutgoing)
        }
        when {
            message.isDeleted -> DeletedContent(isOutgoing = isOutgoing)

            message.content is MessageContent.Text -> {
                Column {
                    BubbleText(message = message, isOutgoing = isOutgoing)
                    val preview = message.linkPreview
                    if (preview != null) {
                        Spacer(Modifier.height(PulseSpacing.sm))
                        LinkPreviewCard(
                            preview = preview,
                            isOutgoing = isOutgoing,
                            modifier = Modifier.widthIn(max = 280.dp),
                        )
                    }
                }
            }

            message.content is MessageContent.Voice -> {
                val voice = message.content as MessageContent.Voice
                VoiceNoteContent(
                    isOutgoing = isOutgoing,
                    durationSeconds = voice.durationSeconds,
                    waveformSamples = voice.waveformSamples,
                    isPlaying = voicePlaying,
                    onPlayPause = onVoiceToggle,
                    seedKey = message.id,
                )
            }

            message.content is MessageContent.File -> {
                FileMessageBody(message = message, isOutgoing = isOutgoing, onFileTap = onFileTap)
            }

            message.content is MessageContent.Contact -> {
                ContactMessageBody(message = message, isOutgoing = isOutgoing)
            }
        }
    }
}

/** "This message was deleted" (italic, with icon). */
@Composable
private fun DeletedContent(isOutgoing: Boolean) {
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = AppIcons.AlertTriangle,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.inline),
            tint = contentColor.copy(alpha = 0.7f),
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        Text(
            text = stringResource(R.string.conversation_deleted_message),
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = contentColor.copy(alpha = 0.7f),
        )
    }
}

/** "Forwarded" with a share icon (S23). */
@Composable
private fun ForwardedLabel(isOutgoing: Boolean) {
    val c = PulseTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = AppIcons.Share,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.tiny),
            tint = if (isOutgoing) c.onAccent.copy(alpha = 0.8f) else c.textSecondary,
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        Text(
            text = stringResource(R.string.conversation_forwarded_label),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (isOutgoing) c.onAccent.copy(alpha = 0.9f) else c.textSecondary,
            ),
        )
    }
    Spacer(Modifier.height(PulseSpacing.tight))
}

/** Plain text with tappable links + accent @mention highlights (S23/M4b). */
@Composable
private fun BubbleText(message: Message, isOutgoing: Boolean) {
    val text = message.text
    val c = PulseTheme.colors
    val uriHandler = LocalUriHandler.current
    val links = remember(text) { URL_REGEX.findAll(text).map { it.range }.toList() }
    val mentions = remember(text) { MENTION_REGEX.findAll(text).map { it.range }.toList() }
    val baseStyle = MaterialTheme.typography.bodyLarge.copy(
        color = if (isOutgoing) c.onAccent else c.textPrimary,
    )

    if (links.isEmpty() && mentions.isEmpty()) {
        Text(text = text, style = baseStyle)
        return
    }

    val linkColor = if (isOutgoing) c.onAccent.copy(alpha = 0.9f) else c.accent
    val mentionColor = if (isOutgoing) c.onAccent else c.accent
    val annotated = remember(text, isOutgoing) {
        buildAnnotatedString {
            // Styled segments: mentions first (accent/weight), links underline.
            var cursor = 0
            val boundaries = (links + mentions).sortedBy { it.first }
            boundaries.forEach { range ->
                if (range.first > cursor) append(text.substring(cursor, range.first))
                val inMention = mentions.any { range.first in it && range.last in it }
                val inLink = links.any { range.first in it && range.last in it }
                withStyle(
                    when {
                        inLink -> SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                        inMention -> SpanStyle(
                            color = mentionColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        else -> SpanStyle()
                    },
                ) { append(text.substring(range.first, range.last + 1)) }
                cursor = range.last + 1
            }
            if (cursor < text.length) append(text.substring(cursor))
        }
    }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = annotated,
        style = baseStyle,
        onTextLayout = { layout = it },
        modifier = Modifier.pointerInput(annotated) {
            detectTapGestures { position ->
                val result = layout ?: return@detectTapGestures
                val offset = result.getOffsetForPosition(position)
                links.firstOrNull { offset in it }?.let { range ->
                    uriHandler.openUri(text.substring(range.first, range.last + 1))
                }
            }
        },
    )
}

private fun iconForType(type: MessageType) = when (type) {
    MessageType.Image -> AppIcons.Image
    MessageType.Video -> AppIcons.Play
    MessageType.Voice -> AppIcons.Mic
    MessageType.File -> AppIcons.File
    MessageType.Location -> AppIcons.MapPin
    MessageType.Contact -> AppIcons.User
    MessageType.Poll -> AppIcons.List
    MessageType.Sticker -> AppIcons.Smile
    MessageType.Text -> AppIcons.MessageCircle
    MessageType.System -> AppIcons.Info
}

/** Time (+ star, edited label, ticks) tucked into the bubble's bottom-right. */
/** Error line under a failed outgoing message (S23: "Tap to retry"). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RetryRow(onRetry: (() -> Unit)?) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(enabled = onRetry != null, onClick = { onRetry?.invoke() })
            .padding(top = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = stringResource(R.string.conversation_tap_retry),
            style = MaterialTheme.typography.labelMedium,
            color = c.error,
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        Icon(
            imageVector = AppIcons.AlertTriangle,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.inline),
            tint = c.error,
        )
    }
}
