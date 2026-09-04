package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pulse.messenger.R
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/* =====================================================================
 * M4c S39 - Create-poll composer: full-height sheet with a question,
 * 2..10 reorderable options, multiple/quiz/anonymous toggles and (in quiz
 * mode) a correct-answer selector. Tiles only - no gradients.
 * ===================================================================== */

@Composable
fun PollComposerSheet(
    onDismiss: () -> Unit,
    onSend: (question: String, options: List<String>, allowsMultiple: Boolean, isAnonymous: Boolean, isQuiz: Boolean, correctOptionIndex: Int?) -> Unit,
    modifier: Modifier = Modifier,
    initialQuestion: String = "",
    initialOptions: List<String> = listOf("", ""),
) {
    val c = PulseTheme.colors
    var question by remember { mutableStateOf(initialQuestion) }
    val options = remember { mutableStateListOf<String>().apply { addAll(initialOptions) } }
    var multiple by remember { mutableStateOf(false) }
    var anonymous by remember { mutableStateOf(false) }
    var quiz by remember { mutableStateOf(false) }
    var correctIndex by remember { mutableStateOf<Int?>(null) }
    var attempted by remember { mutableStateOf(false) }

    val cleaned = options.map { it.trim() }.filter { it.isNotEmpty() }
    val hasValidOptions = cleaned.size >= 2
    val valid = question.isNotBlank() && hasValidOptions && (!quiz || correctIndex != null)

    fun move(from: Int, delta: Int) {
        val to = from + delta
        if (to !in options.indices) return
        val item = options.removeAt(from)
        options.add(to, item)
        correctIndex = correctIndex?.let { cur ->
            when {
                cur == from -> to
                from < cur && to >= cur -> cur - 1
                from > cur && to <= cur -> cur + 1
                else -> cur
            }
        }
    }

    fun removeAt(index: Int) {
        if (options.size <= 2) return
        options.removeAt(index)
        correctIndex = correctIndex?.let { cur ->
            when {
                cur == index -> null
                cur > index -> cur - 1
                else -> cur
            }
        }
    }

    fun toggleQuiz(on: Boolean) {
        quiz = on
        if (on) multiple = false
        if (!on) correctIndex = null
    }

    fun toggleMultiple(on: Boolean) {
        if (on && quiz) {
            quiz = false
            correctIndex = null
        }
        multiple = on
    }

    fun send() {
        attempted = true
        if (!valid) return
        onSend(
            question.trim(),
            cleaned,
            multiple,
            anonymous,
            quiz,
            if (quiz) correctIndex else null,
        )
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
                // Top bar.
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
                        text = stringResource(R.string.conversation_attach_poll),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = ::send, enabled = valid || attempted) {
                        Text(
                            text = stringResource(R.string.conversation_media_send),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (valid) c.accent else c.textTertiary,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = PulseSpacing.md,
                        vertical = PulseSpacing.xs,
                    ),
                    verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
                ) {
                    // Question.
                    item {
                        EditorField(
                            value = question,
                            onValueChange = { question = it },
                            hint = stringResource(R.string.conversation_poll_question_hint),
                            maxLines = 2,
                        )
                    }

                    // Options with reorder/remove affordances.
                    itemsIndexed(options) { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            // Correct-answer selector (quiz mode).
                            if (quiz) {
                                val selected = correctIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) c.accent else Color.Transparent)
                                        .border(
                                            2.dp,
                                            if (selected) c.accent else c.border,
                                            CircleShape,
                                        )
                                        .clickable(
                                            role = Role.RadioButton,
                                            onClickLabel = stringResource(R.string.conversation_poll_correct_cd),
                                            onClick = { correctIndex = if (selected) null else index },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(c.onAccent),
                                        )
                                    }
                                }
                                Spacer(Modifier.width(PulseSpacing.sm))
                            }
                            EditorField(
                                value = option,
                                onValueChange = { options[index] = it },
                                hint = stringResource(R.string.conversation_poll_option_hint, index + 1),
                                maxLines = 2,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(PulseSpacing.tight))
                            // Move up / down arrows.
                            Column(verticalArrangement = Arrangement.spacedBy(PulseSpacing.tight)) {
                                IconArrow(
                                    icon = AppIcons.ChevronUp,
                                    label = stringResource(R.string.conversation_poll_move_up_cd),
                                    enabled = index > 0,
                                    tint = c.textSecondary,
                                    onClick = { move(index, -1) },
                                )
                                IconArrow(
                                    icon = AppIcons.ChevronDown,
                                    label = stringResource(R.string.conversation_poll_move_down_cd),
                                    enabled = index < options.lastIndex,
                                    tint = c.textSecondary,
                                    onClick = { move(index, 1) },
                                )
                            }
                            Spacer(Modifier.width(PulseSpacing.tight))
                            IconArrow(
                                icon = AppIcons.CircleX,
                                label = stringResource(R.string.conversation_poll_remove_option_cd),
                                enabled = options.size > 2,
                                tint = c.textTertiary,
                                onClick = { removeAt(index) },
                            )
                        }
                    }

                    // Add option.
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(PulseShapes.md)
                                .clickable(
                                    enabled = options.size < 10,
                                    role = Role.Button,
                                    onClickLabel = stringResource(R.string.conversation_poll_add_option),
                                    onClick = { options.add("") },
                                )
                                .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.xs),
                        ) {
                            Icon(
                                imageVector = AppIcons.Plus,
                                contentDescription = stringResource(R.string.conversation_poll_add_option),
                                modifier = Modifier.size(PulseIconSizes.small),
                                tint = if (options.size < 10) c.accent else c.textTertiary,
                            )
                            Spacer(Modifier.width(PulseSpacing.sm))
                            Text(
                                text = stringResource(R.string.conversation_poll_add_option),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (options.size < 10) c.accent else c.textTertiary,
                            )
                        }
                    }

                    // Toggles.
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = PulseSpacing.xs),
                        ) {
                            ToggleRow(
                                label = stringResource(R.string.conversation_poll_multiple),
                                checked = multiple,
                                onCheckedChange = ::toggleMultiple,
                            )
                            ToggleRow(
                                label = stringResource(R.string.conversation_poll_anonymous),
                                checked = anonymous,
                                onCheckedChange = { anonymous = it },
                            )
                            ToggleRow(
                                label = stringResource(R.string.conversation_poll_make_quiz),
                                checked = quiz,
                                onCheckedChange = ::toggleQuiz,
                            )
                        }
                    }

                    // Validation hint.
                    if (attempted && !valid) {
                        item {
                            Text(
                                text = stringResource(
                                    if (quiz && correctIndex == null) {
                                        R.string.conversation_poll_validation_quiz
                                    } else {
                                        R.string.conversation_poll_validation
                                    },
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.error,
                                modifier = Modifier.padding(vertical = PulseSpacing.xs),
                            )
                        }
                    }
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(PulseSpacing.xs),
                )
            }
        }
    }
}

@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    maxLines: Int,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Box(
        modifier = modifier
            .clip(PulseShapes.md)
            .background(c.surfaceVariant)
            .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.sm),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
            cursorBrush = SolidColor(c.accent),
            maxLines = maxLines,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
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

@Composable
private fun IconArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(PulseIconSizes.small),
            tint = if (enabled) tint else tint.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = c.onAccent,
                checkedTrackColor = c.accent,
                uncheckedThumbColor = c.surface,
                uncheckedTrackColor = c.surfaceVariant,
                uncheckedBorderColor = c.border,
            ),
        )
    }
}
