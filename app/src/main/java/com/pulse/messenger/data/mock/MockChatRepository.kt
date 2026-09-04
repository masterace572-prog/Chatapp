package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock conversations. M3 seeds ~15 rich chats; M4 adds ~400 messages plus
 * auto-replies, typing and delivery progression (PRD §9). Until then the
 * repository is honest: empty streams after one simulated round-trip.
 */
@Singleton
class MockChatRepository @Inject constructor() : ChatRepository {

    override fun observeChats(): Flow<List<Chat>> = flow {
        Simulator.networkDelay()
        emit(emptyList())
    }

    override fun observeMessages(chatId: String): Flow<List<Message>> = flow {
        Simulator.networkDelay()
        emit(emptyList())
    }

    override fun observeUnreadCounts(): Flow<Map<String, Int>> = flowOf(emptyMap())
}
