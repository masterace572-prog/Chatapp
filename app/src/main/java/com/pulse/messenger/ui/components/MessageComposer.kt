package com.pulse.messenger.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

/**
 * Composer surface state (S23, M4b). The conversation screen owns one of
 * these; [MessageComposer] renders the matching surface.
 *
 * Idle/Typing are the empty/filled text-field states (mic+camera cluster
 * swaps to the send/check circle while typing); Reply/Edit add their bars;
 * Recording/LockedRecording drive the hold-to-record and hands-free UI;
 * Blocked/ReadOnly replace the whole composer.
 */
sealed interface ComposerUiState {
    data object Idle : ComposerUiState
    data object Typing : ComposerUiState

    /** Reply bar above the input; sending attaches replyToMessageId. */
    data class Reply(
        val messageId: String,
        val senderName: String,
        val excerpt: String,
    ) : ComposerUiState

    /** Edit bar; the field is prefilled and send turns into a check. */
    data class Edit(val messageId: String, val excerpt: String) : ComposerUiState

    /** Hold-to-record: pulsing red dot, timer, "Slide to cancel", lock icon. */
    data object Recording : ComposerUiState

    /** Locked hands-free recording: waveform, timer, trash, pause, send. */
    data object LockedRecording : ComposerUiState

    /** "You blocked this contact. Unblock" row. */
    data object Blocked : ComposerUiState

    /** "Only admins can send messages" row. */
    data object ReadOnly : ComposerUiState
}

/** True when [text] ends with an incomplete "@mention" token. */
internal fun mentionQueryAtEnd(text: String): IntRange? {
    val at = text.lastIndexOf('@')
    if (at < 0) return null
    val before = at == 0 || text[at - 1].isWhitespace()
    if (!before) return null
    val tail = text.substring(at + 1)
    if (!tail.all { it.isLetterOrDigit() || it == '_' || it == '.' }) return null
    return at..text.length
}

/** Inline mention span colour (from the theme accent). */
private fun mentionColoring(accent: Color) = object : VisualTransformation {
    private val token = Regex("""@\w+""")
    override fun filter(text: AnnotatedString): TransformedText {
        val out = buildAnnotatedString {
            append(text)
            token.findAll(text.text).forEach { m ->
                addStyle(SpanStyle(color = accent), m.range.first, m.range.last + 1)
            }
        }
        return TransformedText(out, OffsetMapping.Identity)
    }
}

/**
 * Basic conversation composer (S23, M4b): growing pill field, attachment,
 * camera, hold-to-record mic with slide-to-cancel/lock, reply + edit bars,
 * group mention suggestions, blocked/read-only replacement rows.
 *
 * Gesture stability: while the mic is visible (empty field) the trailing
 * cluster keeps the SAME child order ([camera][hold-mic]) when recording
 * starts, so the press is never dropped mid-hold. Sending routes through
 * [onSend] (edit sends an edit); mic finishing routes through
 * [onRecordFinish] with the measured duration + waveform samples.
 */
@Composable
fun MessageComposer(
    state: ComposerUiState,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mentionMembers: List<User> = emptyList(),
    onSend: () -> Unit = {},
    onAttachment: () -> Unit = {},
    onCamera: () -> Unit = {},
    onMicPress: () -> Unit = {},
    onRecordCancel: () -> Unit = {},
    onRecordLock: () -> Unit = {},
    onRecordFinish: (Long, List<Int>) -> Unit = { _: Long, _: List<Int> -> },
    onRecordTooShort: () -> Unit = {},
    onCloseBar: () -> Unit = {},
    onMentionSelected: (String) -> Unit = {},
    onUnblock: () -> Unit = {},
) {
    val c = PulseTheme.colors

    val isRecording = state == ComposerUiState.Recording
    val isLocked = state == ComposerUiState.LockedRecording
    val chatLike = state is ComposerUiState.Idle ||
        state is ComposerUiState.Typing ||
        state is ComposerUiState.Reply ||
        state is ComposerUiState.Edit
    val hasText = value.isNotBlank()

    // ---- Hold-recording session (root-level so both the status row and the
    // gesture button read the same clock). Reset when the surface leaves the
    // recording modes.
    var holdElapsed by remember { mutableLongStateOf(0L) }
    val holdSamples = remember { ArrayList<Int>() }
    var holdRunning by remember { mutableStateOf(false) }
    LaunchedEffect(holdRunning) {
        while (holdRunning) {
            delay(100)
            if (holdElapsed >= 300_000L) break
            holdElapsed += 100
            if (holdSamples.size > 200) holdSamples.removeAt(0)
            holdSamples.add(amplitudeSample(holdElapsed))
        }
    }
    LaunchedEffect(isRecording, isLocked) {
        if (!isRecording && !isLocked) {
            holdRunning = false
            holdElapsed = 0L
            holdSamples.clear()
        }
    }

    // Captured when the hold is released over the lock zone.
    var lockedSession by remember { mutableStateOf<Pair<Long, List<Int>>?>(null) }
    LaunchedEffect(isLocked) {
        if (!isLocked) lockedSession = null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.background),
    ) {
        when {
            state == ComposerUiState.Blocked -> ComposerStatusRow(
                icon = AppIcons.Ban,
                message = stringResource(R.string.conversation_blocked_hint),
                actionLabel = stringResource(R.string.conversation_unblock),
                onAction = onUnblock,
            )
            state == ComposerUiState.ReadOnly -> ComposerStatusRow(
                icon = AppIcons.Shield,
                message = stringResource(R.string.conversation_read_only_hint),
                actionLabel = null,
                onAction = {},
            )
            state == ComposerUiState.LockedRecording -> LockedRecordingRow(
                sessionMs = lockedSession?.first ?: 0L,
                sessionSamples = lockedSession?.second ?: emptyList(),
                onDelete = {
                    lockedSession = null
                    onRecordCancel()
                },
                onFinish = { ms, samples ->
                    lockedSession = null
                    onRecordFinish(ms, samples)
                },
            )
            else -> {
                // Reply / edit bars above the main row.
                when (val s = state) {
                    is ComposerUiState.Reply -> MessageBarRow(
                        icon = AppIcons.CornerUpLeft,
                        title = stringResource(R.string.conversation_reply_to, s.senderName),
                        excerpt = s.excerpt,
                        tint = c.accent,
                        onClose = onCloseBar,
                    )
                    is ComposerUiState.Edit -> MessageBarRow(
                        icon = AppIcons.Pencil,
                        title = stringResource(R.string.conversation_edit_message),
                        excerpt = s.excerpt,
                        tint = c.accent,
                        onClose = onCloseBar,
                    )
                    else -> Unit
                }

                val isEdit = state is ComposerUiState.Edit
                val micVisible = !isLocked && (chatLike && !hasText || isRecording)
                val sendVisible = chatLike && hasText && !isRecording

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = PulseSpacing.xs, end = PulseSpacing.xs, bottom = PulseSpacing.xs),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    // Attachment (hidden while recording).
                    if (!isRecording) {
                        AppIconButton(
                            icon = AppIcons.Paperclip,
                            contentDescription = stringResource(R.string.conversation_composer_attachment_cd),
                            onClick = onAttachment,
                            tint = c.textSecondary,
                            enabled = chatLike,
                        )
                        Spacer(Modifier.width(PulseSpacing.xs))
                    }

                    // Field area: normal input or the recording status line.
                    if (isRecording) {
                        RecordingStatusLine(
                            elapsedMs = holdElapsed,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PulseShapes.full)
                                .background(if (chatLike) c.surfaceVariant else c.surfaceVariant)
                                .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
                        ) {
                            BasicTextField(
                                value = value,
                                onValueChange = onValueChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        min = PulseSizes.inputHeight - PulseSpacing.sm * 2,
                                        max = PulseSpacing.sm * 2 + 20.dp * 6,
                                    ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
                                cursorBrush = SolidColor(c.accent),
                                enabled = chatLike,
                                keyboardOptions = KeyboardOptions.Default,
                                maxLines = 6,
                                visualTransformation = if (mentionMembers.isNotEmpty()) {
                                    mentionColoring(c.accent)
                                } else {
                                    VisualTransformation.None
                                },
                                decorationBox = { inner ->
                                    Box {
                                        if (value.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.conversation_composer_hint),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = c.textTertiary,
                                            )
                                        }
                                        inner()
                                    }
                                },
                            )
                        }
                    }

                    Spacer(Modifier.width(PulseSpacing.xs))

                    // Trailing cluster. Child order stays [camera][mic] while
                    // the mic is visible so the press survives state switches.
                    Box(contentAlignment = Alignment.Center) {
                        when {
                            sendVisible -> SendButton(
                                icon = if (isEdit) AppIcons.Check else AppIcons.Send,
                                contentDescription = stringResource(
                                    if (isEdit) R.string.conversation_action_edit
                                    else R.string.conversation_composer_send_cd,
                                ),
                                onClick = onSend,
                                enabled = true,
                            )
                            micVisible -> {
                                AppIconButton(
                                    icon = AppIcons.Camera,
                                    contentDescription = stringResource(R.string.conversation_composer_camera_cd),
                                    onClick = onCamera,
                                    tint = c.textSecondary,
                                    enabled = chatLike && !isRecording,
                                )
                                MicAndHoldButton(
                                    enabled = micVisible,
                                    onPress = {
                                        holdElapsed = 0L
                                        holdSamples.clear()
                                        holdRunning = true
                                        onMicPress()
                                    },
                                    onCancel = {
                                        holdRunning = false
                                        onRecordCancel()
                                    },
                                    onLock = {
                                        holdRunning = false
                                        lockedSession = holdElapsed to holdSamples.toList()
                                        onRecordLock()
                                    },
                                    onFinish = { heldMs ->
                                        holdRunning = false
                                        onRecordFinish(heldMs, downsample(holdSamples.toList(), 60))
                                    },
                                    onTooShort = {
                                        holdRunning = false
                                        onRecordCancel()
                                        onRecordTooShort()
                                    },
                                    lockIconVisible = isRecording,
                                )
                            }
                        }
                    }
                }


                // Mention popup above the composer (groups only, "@" at end).
                val mentionRange = if (mentionMembers.isNotEmpty() && chatLike) {
                    mentionQueryAtEnd(value)
                } else {
                    null
                }
                if (mentionRange != null) {
                    val fragment = value.substring(mentionRange.first + 1, mentionRange.last + 1)
                    MentionPopup(
                        members = mentionMembers,
                        filter = fragment,
                        modifier = Modifier.padding(horizontal = PulseSpacing.md),
                        onSelect = { user ->
                            val start = mentionRange.first
                            val end = mentionRange.last
                            val mention = "@${user.username} "
                            val next = value.replaceRange(start, end, mention)
                            onValueChange(next)
                            onMentionSelected(user.username)
                        },
                    )
                }
            }
        }
    }
}

/* ================= Surface rows ================= */

/** "You blocked this contact." + Unblock (or the read-only note). */
@Composable
private fun ComposerStatusRow(
    icon: ImageVector,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.inline),
            tint = c.textTertiary,
        )
        Spacer(Modifier.width(PulseSpacing.md))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = c.accent,
                )
            }
        }
    }
}

/** Reply / edit bar: 3dp accent bar, icon chip, title + excerpt, close. */
@Composable
private fun MessageBarRow(
    icon: ImageVector,
    title: String,
    excerpt: String,
    tint: Color,
    onClose: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = PulseSpacing.xs, end = PulseSpacing.xs, bottom = PulseSpacing.tight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(PulseShapes.full)
                .background(tint),
        )
        Spacer(Modifier.width(PulseSpacing.md))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(c.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = tint,
            )
        }
        Spacer(Modifier.width(PulseSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = excerpt,
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AppIconButton(
            icon = AppIcons.Close,
            contentDescription = stringResource(R.string.conversation_close_bar_cd),
            onClick = onClose,
            tint = c.textTertiary,
        )
    }
}

/** Accent circle send / check button. */
@Composable
private fun SendButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val c = PulseTheme.colors
    Box(
        modifier = Modifier
            .size(PulseSizes.minTouchTarget)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(PulseSizes.sendCircle)
                .clip(CircleShape)
                .background(if (enabled) c.accent else c.accent.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.onAccent,
            )
        }
    }
}

/* ================= Hold-to-record ================= */

/**
 * Mic with a press-and-hold gesture. The gesture node is the LAST child of
 * the trailing cluster in both the empty-field and recording layouts, so the
 * press survives the Idle -> Recording state switch.
 *
 * Drag left past ~140dp cancels (haptic); releasing higher than ~110dp above
 * the mic locks the recording (haptic); a release shorter than 500ms shows
 * the "hold a little longer" hint. [onFinish] receives the measured duration.
 */
@Composable
private fun MicAndHoldButton(
    enabled: Boolean,
    onPress: () -> Unit,
    onCancel: () -> Unit,
    onLock: () -> Unit,
    onFinish: (Long) -> Unit,
    onTooShort: () -> Unit,
    lockIconVisible: Boolean,
) {
    val c = PulseTheme.colors
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val cancelThresholdPx = with(density) { 140.dp.toPx() }
    val lockRaisePx = with(density) { 110.dp.toPx() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (lockIconVisible) {
            // Lock "zone" the finger slides up into.
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(c.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Lock,
                    contentDescription = stringResource(R.string.conversation_lock_recording_cd),
                    modifier = Modifier.size(PulseIconSizes.small),
                    tint = c.textSecondary,
                )
            }
            Spacer(Modifier.height(4.dp))
        }
        Box(
            modifier = Modifier
                .size(PulseSizes.minTouchTarget)
                .clip(CircleShape)
                .pointerInput(enabled) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val downMillis = SystemClockMillis()
                        onPress()
                        var handledUp = false
                        var cancelVibrated = false
                        var totalX = 0f
                        var totalY = 0f
                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null) break // pointer cancelled
                                if (!change.pressed) {
                                    // Finger up: decide cancel / lock / too-short / finish.
                                    handledUp = true
                                    val heldMs = SystemClockMillis() - downMillis
                                    when {
                                        cancelVibrated -> Unit
                                        totalY <= -lockRaisePx -> {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onLock()
                                        }
                                        heldMs < 500L -> onTooShort()
                                        else -> onFinish(heldMs.coerceAtMost(300_000L))
                                    }
                                    break
                                }
                                val delta = change.positionChange()
                                change.consume()
                                totalX += delta.x
                                totalY += delta.y
                                if (totalX < -cancelThresholdPx && !cancelVibrated) {
                                    cancelVibrated = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onCancel()
                                }
                            }
                        } finally {
                            // Pointer stream ended without an up event.
                            if (!handledUp && !cancelVibrated) onCancel()
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.Mic,
                contentDescription = stringResource(R.string.conversation_composer_mic_cd),
                modifier = Modifier.size(PulseIconSizes.default),
                tint = c.textSecondary,
            )
        }
    }
}

private fun SystemClockMillis(): Long = android.os.SystemClock.uptimeMillis()

/** Red pulsing dot + tabular timer (left of the held mic). */
@Composable
private fun RecordingStatusLine(
    elapsedMs: Long,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    val pulse = rememberInfiniteTransition(label = "rec")
    val alpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "rec-alpha",
    )
    Row(
        modifier = modifier
            .clip(PulseShapes.full)
            .background(c.surfaceVariant)
            .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(c.error),
        )
        Spacer(Modifier.width(PulseSpacing.sm))
        androidx.compose.material3.Text(
            text = mmss(elapsedMs),
            style = MaterialTheme.typography.labelLarge.copy(
                color = c.textPrimary,
                fontFeatureSettings = "tnum",
            ),
        )
        Spacer(Modifier.width(PulseSpacing.md))
        Icon(
            imageVector = AppIcons.ChevronLeft,
            contentDescription = null,
            modifier = Modifier.size(PulseIconSizes.small),
            tint = c.textTertiary,
        )
        Text(
            text = stringResource(R.string.conversation_slide_to_cancel),
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
        )
    }
}

/** Hands-free recording row: waveform, timer, trash, pause, send. */
@Composable
private fun LockedRecordingRow(
    sessionMs: Long,
    sessionSamples: List<Int>,
    onDelete: () -> Unit,
    onFinish: (Long, List<Int>) -> Unit,
) {
    val c = PulseTheme.colors
    var progressMs by remember { mutableLongStateOf(sessionMs) }
    val samples = remember { ArrayList<Int>().apply { addAll(sessionSamples) } }
    var paused by remember { mutableStateOf(false) }

    LaunchedEffect(paused) {
        var t = progressMs
        while (!paused) {
            delay(100)
            if (t >= 300_000L) break
            t += 100
            progressMs = t
            if (samples.size > 240) samples.removeAt(0)
            samples.add(amplitudeSample(t))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIconButton(
            icon = AppIcons.Trash,
            contentDescription = stringResource(R.string.conversation_delete_recording_cd),
            onClick = onDelete,
            tint = c.error,
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        VoiceWaveform(
            samples = samples,
            playedFraction = 1f,
            maxSample = 100,
            baseColor = Color.Transparent,
            playedColor = c.accent,
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
        )
        Spacer(Modifier.width(PulseSpacing.md))
        Text(
            text = mmss(progressMs),
            style = MaterialTheme.typography.labelLarge.copy(
                color = c.textPrimary,
                fontFeatureSettings = "tnum",
            ),
        )
        Spacer(Modifier.width(PulseSpacing.xs))
        AppIconButton(
            icon = if (paused) AppIcons.Play else AppIcons.Pause,
            contentDescription = stringResource(
                if (paused) R.string.conversation_resume_recording_cd
                else R.string.conversation_pause_recording_cd,
            ),
            onClick = { paused = !paused },
            tint = c.textSecondary,
        )
        SendButton(
            icon = AppIcons.Send,
            contentDescription = stringResource(R.string.conversation_composer_send_cd),
            onClick = {
                val duration = progressMs
                if (duration >= 500L) onFinish(duration, downsample(samples, 60))
            },
            enabled = progressMs >= 500L,
        )
    }
}

private fun amplitudeSample(tMillis: Long): Int {
    val wave = (sin(tMillis / 300.0) * 0.5 + 0.5) * 45
    val jitter = abs(kotlin.random.Random(tMillis / 50).nextInt(-18, 18))
    return ((wave + jitter + 16).toInt()).coerceIn(10, 96)
}

private fun downsample(list: List<Int>, target: Int): List<Int> {
    if (list.isEmpty()) return listOf(30)
    if (list.size <= target) return list
    val step = list.size.toFloat() / target
    return List(target) { i -> list[((i + 0.5f) * step).toInt().coerceAtMost(list.lastIndex)] }
}

private fun mmss(ms: Long): String {
    val total = (ms / 1000).toInt()
    return "%d:%02d".format(total / 60, total % 60)
}

/* ================= Mention suggestions ================= */

/** Popup listing group members filtered by the typed fragment. */
@Composable
private fun MentionPopup(
    members: List<User>,
    filter: String,
    modifier: Modifier = Modifier,
    onSelect: (User) -> Unit,
) {
    val c = PulseTheme.colors
    val q = filter.lowercase()
    val filtered = remember(members, q) {
        members.filter {
            it.username.lowercase().contains(q) ||
                it.firstName.lowercase().contains(q) ||
                it.lastName.lowercase().contains(q)
        }
    }
    if (filtered.isEmpty()) return
    Surface(
        shape = PulseShapes.lg,
        color = c.surface,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = 224.dp)) {
            items(filtered, key = { it.id }) { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(user) }
                        .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
                ) {
                    Avatar(
                        name = user.displayName,
                        avatarTone = AvatarTones.bySeed(user.avatarSeed),
                        size = 32.dp,
                    )
                    Spacer(Modifier.width(PulseSpacing.md))
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textTertiary,
                    )
                }
            }
        }
    }
}
