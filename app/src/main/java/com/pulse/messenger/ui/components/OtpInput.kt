package com.pulse.messenger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import kotlin.math.roundToInt

/**
 * Six-digit OTP input (PRD S06/S11b): individual digit boxes, auto-advance,
 * paste support (a single invisible field feeds the boxes), auto-submit on the
 * final digit and a subtle horizontal shake on error.
 *
 * @param onComplete invoked once the full code is entered (6th digit).
 * @param resetSignal increment to clear the entered digits (e.g. after an error).
 */
@Composable
fun OtpInput(
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
    resetSignal: Int = 0,
) {
    val c = PulseTheme.colors
    var value by remember { mutableStateOf("") }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val shake = remember { Animatable(0f) }
    val submitted = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (enabled) focusRequester.requestFocus()
    }
    LaunchedEffect(resetSignal) {
        if (resetSignal > 0) {
            value = ""
            submitted.value = false
        }
    }
    LaunchedEffect(isError) {
        if (isError) {
            value = ""
            submitted.value = false
            repeat(4) {
                shake.animateTo(if (it % 2 == 0) 6f else -6f, tween(90))
            }
            shake.animateTo(0f, tween(90))
        }
    }

    fun onInput(raw: String) {
        val digits = raw.filter(Char::isDigit).take(length)
        value = digits
        if (digits.length == length && !submitted.value) {
            submitted.value = true
            onComplete(digits)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shake.value.roundToInt(), 0) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
        ) {
            repeat(length) { index ->
                val char = value.getOrNull(index)?.toString().orEmpty()
                val isActive = focused && index == value.length
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(c.surfaceVariant, PulseShapes.sm)
                        .border(
                            width = 1.dp,
                            color = when {
                                isError -> c.error
                                isActive -> c.accent
                                else -> Color.Transparent
                            },
                            shape = PulseShapes.sm,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (char.isNotEmpty()) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge.copy(
                                                            ),
                            color = c.textPrimary,
                        )
                    } else if (isActive) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .background(c.accent, CircleShape),
                        )
                    }
                }
            }
        }
        // Invisible overlay field: tap anywhere to focus; receives typing + paste.
        BasicTextField(
            value = value,
            onValueChange = ::onInput,
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focusRequester)
                .alpha(0.01f),
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            interactionSource = interaction,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {},
        )
    }
}
