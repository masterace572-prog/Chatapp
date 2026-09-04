package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
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
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.ConversationFormat
import com.pulse.messenger.ui.util.MessageLabels

private val URL_REGEX = Regex("""https?://[^\s<>"']+""")

/**
 * One chat bubble (PRD §7 MessageBubble + S23).
 *
 * Handles text (tappable links, wrapping, max ~78% width), content-type
 * placeholders (image/video/voice/file/location/contact/poll/sticker render
 * as a neutral labelled bubble until M4b/M4c), edited/deleted labels, the
 * delivery tick row, failed-message retry, group sender names on run starts
 * and the run avatar on the last bubble of an incoming run.
 *
 * Stateless: run geometry (first/last), resolved names/colours and the retry
 * callback come from the screen. System messages have their own row
 * (SystemMessageRow in ChatExtras.kt) and never reach this composable.
 */
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
) {
    val isOutgoing = message.isOutgoing
    val c = PulseTheme.colors
    val bubbleShape = PulseBubbleShape(isOutgoing, isFirstInRun, isLastInRun)

    // avatarSeed is non-null for incoming GROUP runs only: the avatar is drawn
    // on the last bubble of a run and every other group row reserves its
    // gutter so bubbles stay aligned. Direct chats draw no gutter at all.
    val isGroupRow = !isOutgoing && avatarSeed != null
    val showAvatar = isGroupRow && isLastInRun

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
                if (senderName != null && !isOutgoing) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelMedium,
                        color = senderNameColor ?: c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = PulseSpacing.xs, bottom = PulseSpacing.tight),
                    )
                }

                Column(
                    modifier = Modifier
                        .background(
                            color = if (isOutgoing) c.accent else c.surfaceVariant,
                            shape = bubbleShape,
                        )
                        .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
                ) {
                    BubbleContent(message = message, isOutgoing = isOutgoing)
                    BubbleMetaRow(message = message, isOutgoing = isOutgoing)
                }

                if (message.status == MessageStatus.Failed && message.isOutgoing) {
                    RetryRow(onRetry = onRetry)
                }
            }
        }
    }
}

@Composable
private fun BubbleContent(message: Message, isOutgoing: Boolean) {
    val c = PulseTheme.colors
    val contentColor = if (isOutgoing) c.onAccent else c.textPrimary

    when {
        message.isDeleted -> {
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

        message.content is MessageContent.Text -> BubbleText(
            text = message.text,
            isOutgoing = isOutgoing,
        )

        else -> PlaceholderContent(message = message, isOutgoing = isOutgoing)
    }
}

/**
 * Plain text with tappable link spans (S23). Tap detection maps the pointer
 * position back through the measured layout - stable Text API (ClickableText
 * is deprecated in current Compose).
 */
@Composable
private fun BubbleText(text: String, isOutgoing: Boolean) {
    val c = PulseTheme.colors
    val uriHandler = LocalUriHandler.current
    val links = remember(text) { URL_REGEX.findAll(text).map { it.range }.toList() }
    val baseStyle = MaterialTheme.typography.bodyLarge.copy(
        color = if (isOutgoing) c.onAccent else c.textPrimary,
    )

    if (links.isEmpty()) {
        Text(text = text, style = baseStyle)
        return
    }

    val linkColor = if (isOutgoing) c.onAccent.copy(alpha = 0.9f) else c.accent
    val annotated = remember(text) {
        buildAnnotatedString {
            var cursor = 0
            links.forEach { range ->
                append(text.substring(cursor, range.first))
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) { append(text.substring(range.first, range.last + 1)) }
                cursor = range.last + 1
            }
            append(text.substring(cursor))
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

/** Neutral placeholder for content types whose real rendering lands M4b/M4c. */
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

/** Time (+ edited label + ticks) tucked into the bubble's bottom-right. */
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
@Composable
private fun RetryRow(onRetry: (() -> Unit)?) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onRetry != null, onClick = { onRetry?.invoke() })
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
