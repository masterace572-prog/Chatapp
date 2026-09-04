package com.pulse.messenger.ui.screens.main

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppTopBar
import com.pulse.messenger.ui.components.AppTopBarStyle
import com.pulse.messenger.ui.components.Badge
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.screens.chats.ChatsMoreAction
import com.pulse.messenger.ui.screens.chats.ChatsTab
import com.pulse.messenger.ui.screens.chats.ChatsViewModel
import com.pulse.messenger.ui.theme.PulseSizes
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * S18 - Main shell: 4 bottom tabs (Chats / Calls / People / Settings) with
 * accent-selected / muted-unselected styling, hairline above the bar, unread
 * badge on Chats and a contextual accent FAB (Chats: new chat, People: add).
 * The Chats tab hosts the real S19 list; Calls / People / Settings remain
 * honest placeholders (M5 / M6). Navigation lives in PulseApp - this screen
 * only reports intents.
 */
enum class MainTab(
    @StringRes val labelResId: Int,
    val icon: ImageVector,
) {
    Chats(R.string.tab_chats, AppIcons.MessageCircle),
    Calls(R.string.tab_calls, AppIcons.Phone),
    People(R.string.tab_people, AppIcons.Users),
    Settings(R.string.tab_settings, AppIcons.Settings),
}

@Composable
fun MainScreen(
    onOpenChat: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenArchived: () -> Unit,
    onOpenFolders: () -> Unit,
    onNewChat: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val chatsViewModel: ChatsViewModel = hiltViewModel()
    val chatsState by chatsViewModel.uiState.collectAsStateWithLifecycle()
    val c = PulseTheme.colors
    val context = LocalContext.current

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = MainTab.entries

    // Unread pill on the Chats tab: total unread across non-archived chats.
    val totalUnread = chatsState.chats.sumOf { it.unreadCount }

    fun selectTab(tab: MainTab) {
        selectedTab = tab.ordinal
    }

    // Back-stack rule (PRD S18): back on a non-Chats tab returns to Chats;
    // back while multi-selecting exits selection first; only Chats + no
    // selection lets the system back button finish the activity.
    BackHandler(
        enabled = selectedTab != MainTab.Chats.ordinal || chatsState.selectionMode,
    ) {
        when {
            chatsState.selectionMode -> chatsViewModel.clearSelection()
            else -> selectTab(MainTab.Chats)
        }
    }

    // More-menu items without a screen yet degrade to an honest snackbar;
    // "Settings" simply switches to the Settings tab.
    fun handleMore(action: ChatsMoreAction) {
        when (action) {
            ChatsMoreAction.Settings -> selectTab(MainTab.Settings)
            ChatsMoreAction.NewGroup,
            ChatsMoreAction.NewBroadcast,
            ChatsMoreAction.LinkedDevices,
            ChatsMoreAction.StarredMessages,
            -> {
                val title = context.getString(
                    when (action) {
                        ChatsMoreAction.NewGroup -> R.string.chats_menu_new_group
                        ChatsMoreAction.NewBroadcast -> R.string.chats_menu_new_broadcast
                        ChatsMoreAction.LinkedDevices -> R.string.chats_menu_linked_devices
                        ChatsMoreAction.StarredMessages -> R.string.chats_menu_starred
                        ChatsMoreAction.Settings -> error("handled above")
                    },
                )
                onShowMessage(context.getString(R.string.main_feature_soon, title))
            }
        }
    }

    val currentTab = tabs[selectedTab]
    val showFab = currentTab == MainTab.Chats || currentTab == MainTab.People

    Scaffold(
        modifier = modifier,
        containerColor = c.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                HorizontalDivider(thickness = PulseSizes.dividerHairline, color = c.border)
                NavigationBar(containerColor = c.surface) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = index == selectedTab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectTab(tab) },
                            icon = {
                                Box {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = stringResource(tab.labelResId),
                                    )
                                    if (tab == MainTab.Chats && totalUnread > 0) {
                                        Badge(
                                            count = totalUnread,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 9.dp, y = (-5).dp),
                                        )
                                    }
                                }
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
                                unselectedIconColor = c.textSecondary,
                                unselectedTextColor = c.textSecondary,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        when (currentTab) {
                            MainTab.Chats -> onNewChat()
                            MainTab.People -> onShowMessage(
                                context.getString(R.string.main_people_fab_soon),
                            )
                            else -> Unit
                        }
                    },
                    containerColor = c.accent,
                    contentColor = c.onAccent,
                ) {
                    Icon(
                        imageVector = when (currentTab) {
                            MainTab.Chats -> AppIcons.Pencil
                            MainTab.People -> AppIcons.UserPlus
                            else -> AppIcons.Pencil
                        },
                        contentDescription = stringResource(
                            when (currentTab) {
                                MainTab.Chats -> R.string.chats_new_chat
                                MainTab.People -> R.string.main_add_contact
                                else -> R.string.chats_new_chat
                            },
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (currentTab) {
                MainTab.Chats -> ChatsTab(
                    vm = chatsViewModel,
                    onOpenChat = onOpenChat,
                    onOpenSearch = onOpenSearch,
                    onOpenArchived = onOpenArchived,
                    onOpenFolders = onOpenFolders,
                    onNewChat = onNewChat,
                    onMoreAction = { handleMore(it) },
                )
                MainTab.Calls -> CallsTabPlaceholder()
                MainTab.People -> PeopleTabPlaceholder()
                MainTab.Settings -> SettingsTabPlaceholder()
            }
        }
    }
}

/* ---------- Honest placeholders (Calls M5, People M5, Settings M6) ---------- */

@Composable
private fun TabPlaceholder(
    @StringRes tabTitleRes: Int,
    @StringRes emptyTitleRes: Int,
    @StringRes emptySubtitleRes: Int,
    icon: ImageVector,
) {
    Column(Modifier.fillMaxSize().background(PulseTheme.colors.background)) {
        AppTopBar(
            title = stringResource(tabTitleRes),
            style = AppTopBarStyle.Large,
        )
        EmptyState(
            icon = icon,
            title = stringResource(emptyTitleRes),
            subtitle = stringResource(emptySubtitleRes),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CallsTabPlaceholder() {
    TabPlaceholder(
        tabTitleRes = R.string.tab_calls,
        emptyTitleRes = R.string.placeholder_calls_title,
        emptySubtitleRes = R.string.placeholder_calls_subtitle,
        icon = AppIcons.Phone,
    )
}

@Composable
private fun PeopleTabPlaceholder() {
    TabPlaceholder(
        tabTitleRes = R.string.tab_people,
        emptyTitleRes = R.string.placeholder_people_title,
        emptySubtitleRes = R.string.placeholder_people_subtitle,
        icon = AppIcons.Users,
    )
}

@Composable
private fun SettingsTabPlaceholder() {
    TabPlaceholder(
        tabTitleRes = R.string.tab_settings,
        emptyTitleRes = R.string.placeholder_settings_title,
        emptySubtitleRes = R.string.placeholder_settings_subtitle,
        icon = AppIcons.Settings,
    )
}
