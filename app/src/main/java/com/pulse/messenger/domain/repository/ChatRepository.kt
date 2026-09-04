package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chat & message contract (PRD §5 - interfaces only). Phase 1: mock repository;
 * Phase 2: Supabase. Seeded chat data and send/reply behaviours land with M3/M4 -
 * the interface will only grow, never reshaped, so mocks stay drop-in.
 */
interface ChatRepository {
    /** Conversation summaries for the Chats list (S19). */
    fun observeChats(): Flow<List<Chat>>

    /** Ordered messages of one chat (S23). Empty until the message milestone. */
    fun observeMessages(chatId: String): Flow<List<Message>>

    /** Unread markers per chat - consumed by badges in M3. */
    fun observeUnreadCounts(): Flow<Map<String, Int>>
}
