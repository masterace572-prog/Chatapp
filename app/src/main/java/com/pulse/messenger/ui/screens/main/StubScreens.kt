package com.pulse.messenger.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pulse.messenger.R
import com.pulse.messenger.ui.components.AppBackButton
import com.pulse.messenger.ui.components.AppTopBar
import com.pulse.messenger.ui.components.EmptyState
import com.pulse.messenger.ui.icons.AppIcons
import com.pulse.messenger.ui.theme.PulseTheme

/**
 * Honest M3 stubs for destinations whose real screens land in M4
 * (conversation / new chat). They never fake functionality - the subtitle
 * states which milestone brings the real screen.
 */
@Composable
fun NewChatStubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.chats_new_chat)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PulseTheme.colors.background),
    ) {
        AppTopBar(
            title = title,
            navigationIcon = { AppBackButton(onBack = onBack) },
        )
        EmptyState(
            icon = AppIcons.Pencil,
            title = title,
            subtitle = stringResource(R.string.stub_new_chat_body),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ConversationStubScreen(
    chatId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PulseTheme.colors.background),
    ) {
        AppTopBar(
            title = chatId,
            navigationIcon = { AppBackButton(onBack = onBack) },
        )
        EmptyState(
            icon = AppIcons.MessageCircle,
            title = stringResource(R.string.stub_conversation_title),
            subtitle = stringResource(R.string.stub_conversation_body),
            modifier = Modifier.weight(1f),
        )
    }
}
