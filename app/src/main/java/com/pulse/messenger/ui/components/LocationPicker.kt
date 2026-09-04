package com.pulse.messenger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
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
 * M4c S40 - Location picker: map placeholder (grid that pans under a
 * centered pin), search over mock nearby places, current-location card,
 * and a live-location duration sheet (15 min / 1 h / 8 h). Solid
 * translucent surfaces only; no gradients. No real maps/GPS.
 * ===================================================================== */

data class MockPlace(
    val name: String,
    val area: String,
    val distanceKm: Float,
    val latitude: Double,
    val longitude: Double,
)

/** Neutral mock places near the demo city (Amritsar); text only, no map tiles. */
val mockNearbyPlaces: List<MockPlace> = listOf(
    MockPlace("Golden Temple", "Harmandir Sahib Complex", 1.4f, 31.6200, 74.8765),
    MockPlace("Jallianwala Bagh", "Golden Temple Rd", 1.6f, 31.6206, 74.8800),
    MockPlace("Gobindgarh Fort", "Old Cantt Road", 2.1f, 31.6250, 74.8627),
    MockPlace("Hall Bazaar", "Old City", 2.3f, 31.6334, 74.8689),
    MockPlace("Amritsar Junction", "Railway Station Rd", 0.8f, 31.6340, 74.8722),
    MockPlace("ISBT Amritsar", "Majitha Road", 2.9f, 31.6410, 74.8840),
    MockPlace("Ram Tirath Temple", "Chogawan Road", 11.0f, 31.7206, 74.7522),
    MockPlace("Wagah Border", "Attari Road", 27.4f, 31.6048, 74.5732),
)

private val currentLocation = MockPlace(
    "Current location", "Pulse demo coordinates", 0f, 31.6340, 74.8723,
)

private const val GRID_STEP = 28f

@Composable
fun LocationPickerSheet(
    onDismiss: () -> Unit,
    onSendLocation: (latitude: Double, longitude: Double, address: String, isLive: Boolean, liveDurationMs: Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(0) } // 0 = current location, 1..n = places
    var liveMode by remember { mutableStateOf(false) }
    val all = listOf(currentLocation) + mockNearbyPlaces
    val q = query.trim()
    val filtered = if (q.isEmpty()) all else {
        all.filter { it.name.contains(q, ignoreCase = true) || it.area.contains(q, ignoreCase = true) }
    }
    val selection = filtered.getOrNull(selected) ?: currentLocation

    fun sendStatic() {
        onSendLocation(
            selection.latitude,
            selection.longitude,
            "${selection.name}, ${selection.area}",
            false,
            null,
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            color = c.surface,
            modifier = modifier
                .fillMaxSize(),
        ) {
            Column(Modifier.fillMaxSize()) {
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
                        text = stringResource(R.string.conversation_attach_location),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (liveMode) {
                    LiveDurationSheetContent(
                        onDismiss = { liveMode = false },
                        onShare = { durationMs ->
                            liveMode = false
                            onSendLocation(
                                selection.latitude,
                                selection.longitude,
                                "${selection.name}, ${selection.area}",
                                true,
                                durationMs,
                            )
                        },
                    )
                    return@Surface
                }

                // Search.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PulseShapes.full)
                            .background(c.surfaceVariant)
                            .padding(horizontal = PulseSpacing.lg, vertical = PulseSpacing.sm),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Search,
                                contentDescription = null,
                                modifier = Modifier.size(PulseIconSizes.inline),
                                tint = c.textTertiary,
                            )
                            Spacer(Modifier.width(PulseSpacing.sm))
                            Box {
                                BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = c.textPrimary),
                                    cursorBrush = SolidColor(c.accent),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        Box {
                                            if (query.isEmpty()) {
                                                Text(
                                                    text = stringResource(R.string.conversation_location_search_hint),
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
                    }
                }

                // Map placeholder: pannable grid under a centered pin.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = PulseSpacing.md, vertical = PulseSpacing.xs)
                        .clip(PulseShapes.lg)
                        .background(c.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    MapGrid(Modifier.fillMaxSize())
                    // Centered pin (solid translucent marker disc + pin icon).
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(c.mediaScrim),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = AppIcons.MapPin,
                                contentDescription = stringResource(R.string.conversation_location_map_cd),
                                modifier = Modifier.size(PulseIconSizes.feature),
                                tint = c.onMediaScrim,
                            )
                        }
                        Text(
                            text = selection.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = c.onMediaScrim,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(PulseShapes.sm)
                                .background(c.mediaScrim)
                                .padding(horizontal = PulseSpacing.sm, vertical = 2.dp),
                        )
                    }
                }

                // Place list.
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.conversation_location_none),
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textTertiary,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = PulseSpacing.md,
                            vertical = PulseSpacing.xs,
                        ),
                    ) {
                        items(filtered.size) { index ->
                            val place = filtered[index]
                            val chosen = selected == index
                            LocationRow(
                                place = place,
                                chosen = chosen,
                                isCurrent = place === currentLocation,
                                onClick = { selected = index },
                            )
                        }
                    }
                }

                // Bottom actions.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(PulseSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedAction(
                        label = stringResource(R.string.conversation_location_live_label),
                        onClick = { liveMode = true },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(PulseSpacing.sm))
                    Surface(
                        onClick = ::sendStatic,
                        shape = RoundedCornerShape(12.dp),
                        color = c.accent,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.conversation_location_send_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = c.onAccent,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationRow(
    place: MockPlace,
    chosen: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PulseShapes.md)
            .background(if (chosen) c.accentContainerMuted else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = PulseSpacing.sm, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (chosen) c.accent else c.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AppIcons.MapPin,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = if (chosen) c.onAccent else c.textSecondary,
            )
        }
        Spacer(Modifier.width(PulseSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = c.textPrimary,
                maxLines = 1,
            )
            Text(
                text = if (isCurrent) {
                    stringResource(R.string.conversation_location_current_sub)
                } else {
                    "${place.area} · ${place.distanceKm} km"
                },
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
                maxLines = 1,
            )
        }
        if (chosen) {
            Icon(
                imageVector = AppIcons.Check,
                contentDescription = stringResource(R.string.conversation_location_place_cd),
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.accent,
            )
        }
    }
}

/** Pan-able grid placeholder ("map"): subtle gridlines shift as you drag. */
@Composable
private fun MapGrid(modifier: Modifier = Modifier) {
    val c = PulseTheme.colors
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    offsetX = (offsetX + drag.x).coerceIn(-GRID_STEP * 6, GRID_STEP * 6)
                    offsetY = (offsetY + drag.y).coerceIn(-GRID_STEP * 6, GRID_STEP * 6)
                }
            },
    ) {
        val color = c.border
        val w = size.width
        val h = size.height
        // Vertical lines.
        var x = (offsetX % GRID_STEP + GRID_STEP) % GRID_STEP
        while (x < w) {
            drawLine(color, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            x += GRID_STEP
        }
        // Horizontal lines.
        var y = (offsetY % GRID_STEP + GRID_STEP) % GRID_STEP
        while (y < h) {
            drawLine(color, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            y += GRID_STEP
        }
    }
}

@Composable
private fun OutlinedAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, c.accent, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = c.accent,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/* ---- Live-location duration sheet (shown inside the picker) ---- */

@Composable
fun LiveDurationSheetContent(
    onDismiss: () -> Unit,
    onShare: (durationMs: Long) -> Unit,
) {
    val c = PulseTheme.colors
    val options = listOf(
        Triple(15 * 60_000L, stringResource(R.string.conversation_location_live_15m), 0),
        Triple(60 * 60_000L, stringResource(R.string.conversation_location_live_1h), 1),
        Triple(8 * 60 * 60_000L, stringResource(R.string.conversation_location_live_8h), 2),
    )
    var choice by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PulseSpacing.md),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PulseShapes.lg)
                .background(c.surfaceVariant)
                .padding(PulseSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.conversation_location_live_label),
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
            )
            Spacer(Modifier.height(PulseSpacing.xs))
            Text(
                text = stringResource(R.string.conversation_location_live_sub),
                style = MaterialTheme.typography.bodySmall,
                color = c.textSecondary,
            )
            Spacer(Modifier.height(PulseSpacing.sm))
            options.forEach { (_, label, index) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PulseShapes.md)
                        .clickable(role = Role.RadioButton, onClick = { choice = index })
                        .padding(vertical = PulseSpacing.sm, horizontal = PulseSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    val checked = choice == index
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (checked) c.accent else androidx.compose.ui.graphics.Color.Transparent)
                            .border(2.dp, if (checked) c.accent else c.border, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (checked) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(c.onAccent),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(PulseSpacing.md))
        Row {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textSecondary,
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onShare(options[choice].first) }) {
                Text(
                    text = stringResource(R.string.conversation_location_live_send),
                    style = MaterialTheme.typography.labelLarge,
                    color = c.accent,
                )
            }
        }
    }
}
