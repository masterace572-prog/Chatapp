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
private fun BubbleQuote(
    message: Message,
    quote: BubbleQuoteData?,
    isOutgoing: Boolean,
    onClick: (() -> Unit)?,
) {
    if (quote == null || message.isDeleted || message.isSystem) return
    val c = PulseTheme.colors
    val accent = if (isOutgoing) c.onAccent else c.accent
    val quoteColor = if (isOutgoing) c.onAccent.copy(alpha = 0.92f) else c.textPrimary
    Row(
        modifier = Modifier
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
) {
    Column {
        // Forwarded label above the content (hidden for deleted/system).
        if (message.forwardedFromUserId != null && !message.isDeleted && !message.isSystem) {
            ForwardedLabel(isOutgoing = isOutgoing)
        }
        when {
            message.isDeleted -> DeletedContent(isOutgoing = isOutgoing)

            message.content is MessageContent.Text -> {
                BubbleText(message = message, isOutgoing = isOutgoing)
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

            else -> PlaceholderContent(message = message, isOutgoing = isOutgoing)
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

/** Neutral placeholder for content types whose rendering lands M4c/M4d. */
@Composable
private fun PlaceholderContent(message: Message, isOutgoing: Boolean) {
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary
    val type = message.type
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = iconForType(type),
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = if (isOutgoing) c.onAccent.copy(alpha = 0.85f) else c.textSecondary,
            )
            Spacer(Modifier.width(PulseSpacing.sm))
            Text(
                text = MessageLabels.bubbleLabel(type),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
        val caption = message.text
        if (caption.isNotBlank() && caption != MessageLabels.bubbleLabel(type)) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = contentColor.copy(alpha = 0.9f),
                ),
                modifier = Modifier.padding(top = PulseSpacing.xs),
            )
        }
    }
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
@Composable
private fun ColumnScope.BubbleMetaRow(message: Message, isOutgoing: Boolean) {
    val c = PulseTheme.colors
    val metaColor = if (isOutgoing) c.onAccent.copy(alpha = 0.8f) else c.textTertiary
    val edited = stringResource(R.string.conversation_edited)
    Row(
        modifier = Modifier
            .align(Alignment.End)
            .padding(top = PulseSpacing.tight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (message.isStarred && !message.isDeleted) {
            Icon(
                imageVector = AppIcons.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(PulseSizes.bubbleMetaIcon)
                    .padding(end = PulseSpacing.xs),
                tint = if (isOutgoing) c.onAccent.copy(alpha = 0.9f) else c.warning,
            )
        }
        if (message.isEdited && !message.isDeleted) {
            Text(
                text = edited,
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
            Spacer(Modifier.width(PulseSpacing.xs))
            DeliveryTicks(
                status = message.status,
                tint = c.onAccent.copy(alpha = 0.75f),
                readTint = c.onAccent,
            )
        }
    }
}

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
