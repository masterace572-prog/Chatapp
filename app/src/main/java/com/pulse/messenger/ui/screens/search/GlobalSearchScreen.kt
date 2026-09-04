package com.pulse.messenger.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.ui.components.AppBackButton
import com.pulse.messenger.ui.components.AppChip
import com.pulse.messenger.ui.components.Avatar
import com.pulse.messenger.ui.components.ChatListItem
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.AvatarTones
import com.pulse.messenger.ui.theme.PulseIconSizes
import com.pulse.messenger.ui.theme.PulseShapes
import com.pulse.messenger.ui.theme.PulseSpacing
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.ui.util.MessageLabels
import com.pulse.messenger.ui.util.TimeFormat

/**
 * S20 - Global search: full-screen destination over the Chats tab.
 * Debounced mock search across chats/people/messages/media/links/files with
 * filter chips, removable recent history (DataStore) and highlight.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalSearchScreen(
    vm: SearchViewModel,
    onOpenChat: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val c = PulseTheme.colors
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding(),
    ) {
        // Header: back + search field + clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PulseSpacing.screen)
                .padding(top = PulseSpacing.sm, bottom = PulseSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBackButton(onBack = onBack)
            Spacer(Modifier.width(PulseSpacing.xs))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(PulseShapes.full)
                    .background(c.surfaceVariant),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = PulseSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = AppIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(PulseIconSizes.inline),
                        tint = c.textSecondary,
                    )
                    Spacer(Modifier.width(PulseSpacing.md))
                    Box(Modifier.weight(1f)) {
                        BasicTextField(
                            value = uiState.query,
                            onValueChange = vm::onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
                            cursorBrush = SolidColor(c.accent),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    vm.runSearch(uiState.query)
                                    focusManager.clearFocus()
                                },
                            ),
                            decorationBox = { inner ->
                                if (uiState.query.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_hint),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = c.textTertiary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                inner()
                            },
                        )
                    }
                    if (uiState.query.isNotEmpty()) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(R.string.common_clear),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { vm.onQueryChange("") }
                                .size(28.dp)
                                .padding(4.dp),
                            tint = c.textSecondary,
                        )
                    }
                }
            }
        }

        if (uiState.query.isNotBlank()) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PulseSpacing.screen),
                horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
            ) {
                SearchFilter.entries.forEach { filter ->
                    val label = stringResource(
                        when (filter) {
                            SearchFilter.All -> R.string.search_filter_all
                            SearchFilter.Chats -> R.string.search_filter_chats
                            SearchFilter.People -> R.string.search_filter_people
                            SearchFilter.Messages -> R.string.search_filter_messages
                            SearchFilter.Media -> R.string.search_filter_media
                        },
                    )
                    AppChip(
                        label = label,
                        selected = uiState.filter == filter,
                        onClick = { vm.selectFilter(filter) },
                    )
                }
            }
            Spacer(Modifier.height(PulseSpacing.sm))
        }

        when {
            uiState.query.isBlank() -> RecentSearches(
                recent = uiState.recent,
                onSearch = vm::runSearch,
                onRemove = vm::removeRecent,
                onClear = vm::clearRecent,
                modifier = Modifier.weight(1f),
            )
            uiState.searching -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = c.accent,
                    strokeWidth = 2.dp,
                )
            }
            uiState.noResults -> EmptyState(
                icon = AppIcons.Search,
                title = stringResource(R.string.search_no_results_title),
                subtitle = stringResource(R.string.search_no_results_subtitle, uiState.query.trim()),
                modifier = Modifier.weight(1f),
            )
            else -> SearchResultsList(
                state = uiState,
                onOpenChat = onOpenChat,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Recent queries with individual removal (S20). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearches(
    recent: List<String>,
    onSearch: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PulseTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.screen),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.search_recent),
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (recent.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.common_clear),
                    style = MaterialTheme.typography.labelLarge,
                    color = c.accent,
                    modifier = Modifier
                        .clickable(onClick = onClear)
                        .padding(PulseSpacing.sm),
                )
            }
        }
        Spacer(Modifier.height(PulseSpacing.sm))
        if (recent.isEmpty()) {
            Text(
                text = stringResource(R.string.search_recent_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textTertiary,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(PulseSpacing.sm),
            ) {
                recent.forEach { term ->
                    Row(
                        modifier = Modifier
                            .clip(PulseShapes.full)
                            .background(c.surfaceVariant)
                            .clickable { onSearch(term) }
                            .padding(start = PulseSpacing.lg, end = PulseSpacing.xs, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = term,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textPrimary,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(PulseSpacing.xs))
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(R.string.common_remove),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onRemove(term) }
                                .size(20.dp)
                                .padding(3.dp),
                            tint = c.textTertiary,
                        )
                    }
                }
            }
        }
    }
}

/** Grouped, filtered results (S20). */
@Composable
private fun SearchResultsList(
    state: SearchUiState,
    onOpenChat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        state.visible.forEach { (section, items) ->
            item(key = "header-${section.name}") {
                SearchSectionHeader(
                    title = stringResource(section.titleRes),
                    count = items.size,
                )
            }
            when (section) {
                SearchSection.Chats -> items(
                    items = items as List<ChatSummary>,
                    key = { "chat-${it.chatId}" },
                ) { chat ->
                    ChatListItem(
                        summary = chat,
                        onClick = { onOpenChat(chat.chatId) },
                    )
                }
                SearchSection.People -> items(
                    items = items as List<User>,
                    key = { "people-${it.id}" },
                ) { user -> SearchPersonRow(user) }
                SearchSection.Messages,
                SearchSection.Links,
                SearchSection.Media,
                SearchSection.Files,
                -> items(
                    items = items as List<Message>,
                    key = { "msg-${it.id}" },
                ) { message ->
                    SearchMessageRow(
                        message = message,
                        chatName = state.chatNames[message.chatId] ?: message.chatId,
                        query = state.query,
                        onOpen = { onOpenChat(message.chatId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String, count: Int) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = PulseSpacing.screen, end = PulseSpacing.screen, top = PulseSpacing.lg, bottom = PulseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = c.textTertiary,
        )
    }
}

@Composable
private fun SearchPersonRow(user: User) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            name = user.displayName,
            avatarTone = AvatarTones.bySeed(user.avatarSeed),
            size = 44.dp,
        )
        Spacer(Modifier.width(PulseSpacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = c.textPrimary,
                maxLines = 1,
            )
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
                maxLines = 1,
            )
        }
    }
}

/** Message result row: tinted type square, snippet with highlight, context. */
@Composable
private fun SearchMessageRow(
    message: Message,
    chatName: String,
    query: String,
    onOpen: () -> Unit,
) {
    val c = PulseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(horizontal = PulseSpacing.screen, vertical = PulseSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val typeIcon = when (message.type) {
            com.pulse.messenger.domain.model.MessageType.Image -> AppIcons.Image
            com.pulse.messenger.domain.model.MessageType.Video -> AppIcons.Video
            com.pulse.messenger.domain.model.MessageType.File -> AppIcons.File
            com.pulse.messenger.domain.model.MessageType.Voice -> AppIcons.Mic
            else -> AppIcons.MessageSquare
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(PulseShapes.md)
                .background(c.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = typeIcon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.inline),
                tint = c.textSecondary,
            )
        }
        Spacer(Modifier.width(PulseSpacing.lg))
        Column(Modifier.weight(1f)) {
            Text(
                text = chatName,
                style = MaterialTheme.typography.labelMedium,
                color = c.textTertiary,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = highlightedSnippet(message.text, query, c.accent),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(PulseSpacing.sm))
        Text(
            text = TimeFormat.fullDate(message.sentAtMillis),
            style = MaterialTheme.typography.labelSmall,
            color = c.textTertiary,
        )
    }
}

/** Snippet around the first case-insensitive match with accent highlight spans. */
fun highlightedSnippet(
    full: String,
    needle: String,
    accent: androidx.compose.ui.graphics.Color,
): AnnotatedString {
    val trimmed = needle.trim()
    if (trimmed.isEmpty()) return AnnotatedString(full)
    val idx = full.lowercase().indexOf(trimmed.lowercase())
    if (idx < 0) return AnnotatedString(full)
    val start = maxOf(0, idx - 20)
    val end = minOf(full.length, idx + trimmed.length + 40)
    val rawSnippet = full.substring(start, end)
    val snippet = if (start > 0) "…$rawSnippet" else rawSnippet
    val localIdx = snippet.lowercase().indexOf(trimmed.lowercase())
    if (localIdx < 0) return AnnotatedString(snippet)
    val localEnd = minOf(localIdx + trimmed.length, snippet.length)
    return buildAnnotatedString {
        append(snippet.substring(0, localIdx))
        withStyle(
            SpanStyle(
                color = accent,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
        ) {
            append(snippet.substring(localIdx, localEnd))
        }
        append(snippet.substring(localEnd))
    }
}
