package com.pulse.messenger.ui.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pulse.messenger.ui.components.AppTopBar
import com.pulse.messenger.ui.components.AppTopBarStyle
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseTheme
import com.pulse.messenger.R

/**
 * S18 - Main scaffold: 4 bottom tabs (Chats / Calls / People / Settings).
 * Edge-to-edge with insets; content behind the bars is inset by the Scaffold.
 * Tab bodies are honest placeholders until M3 (Chats), M5 (Calls/People),
 * M6 (Settings) - each screen already defines its own feature package.
 */
enum class MainTab(
    val labelResId: Int,
    val icon: ImageVector,
) {
    Chats(R.string.tab_chats, AppIcons.MessageCircle),
    Calls(R.string.tab_calls, AppIcons.Phone),
    People(R.string.tab_people, AppIcons.Users),
    Settings(R.string.tab_settings, AppIcons.Settings),
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val c = PulseTheme.colors
    val tabs = MainTab.entries

    Scaffold(
        modifier = modifier,
        containerColor = c.background,
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = c.border)
                NavigationBar(containerColor = c.surface) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = index == selectedTab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.labelResId),
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.labelResId),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = c.accent,
                                selectedTextColor = c.accent,
                                indicatorColor = c.accentContainer,
                                unselectedIconColor = c.textTertiary,
                                unselectedTextColor = c.textTertiary,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (tabs[selectedTab]) {
                MainTab.Chats -> ChatsTabPlaceholder()
                MainTab.Calls -> CallsTabPlaceholder()
                MainTab.People -> PeopleTabPlaceholder()
                MainTab.Settings -> SettingsTabPlaceholder()
            }
        }
    }
}

/* ---------- Placeholder tab bodies (replaced by M3 / M5 / M6) ---------- */

@Composable
private fun ChatsTabPlaceholder() {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.tab_chats),
            style = AppTopBarStyle.Large,
        )
        EmptyState(
            icon = AppIcons.MessageCircle,
            title = "No conversations yet",
            subtitle = "Every message you send and receive will live here.",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CallsTabPlaceholder() {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.tab_calls),
            style = AppTopBarStyle.Large,
        )
        EmptyState(
            icon = AppIcons.Phone,
            title = "No calls yet",
            subtitle = "Your call history will appear here.",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PeopleTabPlaceholder() {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.tab_people),
            style = AppTopBarStyle.Large,
        )
        EmptyState(
            icon = AppIcons.Users,
            title = "No contacts yet",
            subtitle = "People from your chats will appear here.",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SettingsTabPlaceholder() {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.tab_settings),
            style = AppTopBarStyle.Large,
        )
        EmptyState(
            icon = AppIcons.Settings,
            title = "Settings are on the way",
            subtitle = "Account, privacy, appearance and more (milestone M6).",
            modifier = Modifier.weight(1f),
        )
    }
}
