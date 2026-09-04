package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Pulse text field.
 * Visual spec: sm 8dp radius, surfaceVariant container, hairline border,
 * accent border when focused, error colour when invalid (PRD §4 + auth rules).
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
) {
    val c = PulseTheme.colors
    var passwordVisible by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus && enabled) focusRequester.requestFocus()
    }

    val showTrailing = trailingIcon != null || isPassword
    val trailing = when {
        trailingIcon != null -> Triple(trailingIcon, onTrailingIconClick, false)
        isPassword -> {
            if (passwordVisible) Triple(AppIcons.EyeOff, { passwordVisible = false }, false)
            else Triple(AppIcons.Eye, { passwordVisible = true }, false)
        }
        else -> null
    }

    val borderColor = when {
        isError -> c.error
        focused -> c.accent
        else -> Color.Transparent
    }
    val fieldTint = when {
        isError -> c.error
        focused -> c.accent
        else -> c.textSecondary
    }
    val textStyle = MaterialTheme.typography.bodyLarge

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) c.error else c.textSecondary,
            )
            Spacer(Modifier.size(PulseSpacing.sm))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = PulseSizes.inputHeight)
                .background(color = c.surfaceVariant, shape = PulseShapes.sm)
                .border(width = 1.dp, color = borderColor, shape = PulseShapes.sm)
                .padding(horizontal = PulseSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(PulseIconSizes.inline),
                    tint = fieldTint,
                )
                Spacer(Modifier.width(PulseSpacing.md))
            }
            Box(Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    enabled = enabled,
                    singleLine = singleLine,
                    maxLines = if (singleLine) 1 else maxLines,
                    textStyle = textStyle.copy(
                        color = if (enabled) c.textPrimary else c.textTertiary,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction,
                        autoCorrectEnabled = true,
                    ),
                    keyboardActions = keyboardActions ?: KeyboardActions.Default,
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    cursorBrush = SolidColor(c.accent),
                    interactionSource = interaction,
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = textStyle,
                                    color = c.textTertiary,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
            if (showTrailing && trailing != null) {
                Spacer(Modifier.width(PulseSpacing.sm))
                val (icon, onClick, _) = trailing
                val iconContent: @Composable () -> Unit = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = fieldTint,
                    )
                }
                if (onClick != null) {
                    Box(
                        modifier = Modifier
                            .size(PulseSizes.minTouchTarget)
                            .clipCircle()
                            .clickable(onClick = onClick),
                        contentAlignment = Alignment.Center,
                    ) { iconContent() }
                } else {
                    iconContent()
                }
            }
        }
        if (supportingText != null) {
            Spacer(Modifier.size(PulseSpacing.xs))
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) c.error else c.textSecondary,
            )
        }
    }
}

private fun Modifier.clipCircle(): Modifier = this.then(
    Modifier.background(color = Color.Transparent, shape = CircleShape),
)
