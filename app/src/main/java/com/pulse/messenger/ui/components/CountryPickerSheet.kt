package com.pulse.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulse.messenger.domain.model.CountryCode
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme

private val DefaultCountries = listOf(
    CountryCode("IN", "India", "+91"),
    CountryCode("US", "United States", "+1"),
    CountryCode("GB", "United Kingdom", "+44"),
    CountryCode("AE", "United Arab Emirates", "+971"),
    CountryCode("SG", "Singapore", "+65"),
    CountryCode("AU", "Australia", "+61"),
    CountryCode("CA", "Canada", "+1"),
    CountryCode("DE", "Germany", "+49"),
    CountryCode("FR", "France", "+33"),
    CountryCode("JP", "Japan", "+81"),
    CountryCode("SA", "Saudi Arabia", "+966"),
    CountryCode("BR", "Brazil", "+55"),
)

/**
 * Searchable country picker sheet (PRD S03b). Flags are rendered as text ISO
 * badges - never emoji. Returns the selection through [onSelect].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerSheet(
    onDismiss: () -> Unit,
    onSelect: (CountryCode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) DefaultCountries
        else DefaultCountries.filter {
            it.name.lowercase().contains(q) ||
                it.iso.lowercase().contains(q) ||
                it.prefix.contains(q)
        }
    }

    AppBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Text(
            text = "Select country",
            style = MaterialTheme.typography.titleLarge,
            color = c.textPrimary,
        )
        Spacer(Modifier.height(PulseSpacing.md))
        AppTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search country or code",
            leadingIcon = AppIcons.Search,
            autoFocus = true,
        )
        Spacer(Modifier.height(PulseSpacing.md))
        LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth()) {
            items(filtered, key = { it.iso }) { country ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PulseShapes.md)
                        .clickable {
                            onSelect(country)
                            onDismiss()
                        }
                        .padding(vertical = PulseSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Text ISO badge instead of an emoji flag (PRD UI rules).
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 28.dp)
                            .clip(PulseShapes.sm)
                            .background(c.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = country.iso,
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textPrimary,
                        )
                    }
                    Spacer(Modifier.width(PulseSpacing.lg))
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = country.prefix,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textSecondary,
                    )
                }
            }
        }
    }
}
