package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Chat & message contract (PRD §5 - interfaces only). Phase 1: mock repository;
 * Phase 2: Supabase. M3 extends the surface with list summaries and chat
 * actions; M4 adds send/reply behaviours. The interface only grows, never
 * reshapes, so mocks stay drop-in.
 */
interface ChatRepository {

    /** List summaries for the Chats tab, ordered by last activity (S19). */
    fun observeChatSummaries(): Flow<List<ChatSummary>>

    /** Full list summaries for the Archived screen (S21) - archived only. */
    fun observeArchivedSummaries(): Flow<List<ChatSummary>>

    /** Every message across chats - used by global search (S20). */
    fun observeAllMessages(): Flow<List<Message>>

    /** Ordered messages of one chat (S23). Empty until the M4 milestone. */
    fun observeMessages(chatId: String): Flow<List<Message>>

    /* ---------- Chat list actions (mock/local state) ---------- */

    suspend fun setArchived(chatId: String, archived: Boolean)
    suspend fun setPinned(chatId: String, pinned: Boolean)
    suspend fun setMuted(chatId: String, muted: Boolean)
    suspend fun markRead(chatId: String)
    suspend fun deleteChats(chatIds: List<String>)
    suspend fun markAllRead()

    /** Simulates a fetch round-trip; seeds any periodic "alive" behaviour. */
    suspend fun refresh()
}
