package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.Chat
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Chat & message contract (PRD §5 - interfaces only). Phase 1: mock repository;
 * Phase 2: Supabase.
 *
 * Messages live on THIS interface (rather than a separate MessageRepository)
 * because a conversation and its messages are one aggregate: list summaries
 * derive from the last message, unread counts track the message stream, and
 * drafts edit what the list previews. The Phase-1 store keeps both in a single
 * singleton, so splitting the contract would fork one source of truth. The
 * interface only grows, never reshapes, so mocks stay drop-in.
 *
 * M3: list summaries + chat actions. M4a: conversation surface (message
 * streams, typing, sending, drafts, read state). M4b+: replies, edits,
 * deletes and reactions extend the same seams.
 */
interface ChatRepository {

    /* ---------- List surface (M3, S19/S20/S21) ---------- */

    /** List summaries for the Chats tab, ordered by last activity (S19). */
    fun observeChatSummaries(): Flow<List<ChatSummary>>

    /** Full list summaries for the Archived screen (S21) - archived only. */
    fun observeArchivedSummaries(): Flow<List<ChatSummary>>

    /** Every message across chats - used by global search (S20). */
    fun observeAllMessages(): Flow<List<Message>>

    /* ---------- Conversation surface (M4a, S23) ---------- */

    /** Live chat header state (members, flags, metadata). */
    fun observeChat(chatId: String): Flow<Chat>

    /** Ordered messages of one chat, newest last. Hot: emits on every change. */
    fun observeMessages(chatId: String): Flow<List<Message>>

    /**
     * User ids currently typing in a chat (mock pulses + pre-reply typing).
     * Empty set when nobody types.
     */
    fun observeTyping(chatId: String): Flow<Set<String>>

    /**
     * Sends a text message; returns its id so the sender can track it.
     * A mock pipeline then moves it Sending → Sent → Delivered → Read
     * (Read only when the peer is online); ~5% of sends fail so retry is
     * exercisable - [retryMessage] always succeeds.
     */
    suspend fun sendText(chatId: String, text: String, replyToMessageId: String? = null): String

    /** Retries a failed message; always succeeds in the mock. */
    suspend fun retryMessage(messageId: String)

    /** Marks one chat as read (used by multi-select and the conversation screen). */
    suspend fun markChatRead(chatId: String)

    /**
     * Presence hint: the chat currently on screen (or null). Incoming mock
     * messages bump unread counts only while the chat is NOT on screen;
     * opening a chat routes through [markChatRead] to clear the badge.
     */
    suspend fun setActiveConversation(chatId: String?)

    /** Saves the unsent composer text; empty string clears the draft (S19 prefix). */
    suspend fun setDraft(chatId: String, text: String)

    /* ---------- Message actions (M4b: reply/edit/delete/reactions/pin) ---------- */

    /** Replaces the text of a sent message and marks it [Message.isEdited]. */
    suspend fun editMessage(messageId: String, newText: String)

    /**
     * Deletes a message. forEveryone marks it [Message.isDeleted] so peers'
     * clients render "This message was deleted"; forMe removes it from this
     * user's copy of the chat (the mock keeps one store, so the deletion is
     * simply applied to the local history).
     */
    suspend fun deleteMessage(messageId: String, forEveryone: Boolean)

    /** Adds the current user to (or removes them from) a reaction's user ids. */
    suspend fun toggleReaction(messageId: String, emoji: String)

    suspend fun setStarred(messageId: String, starred: Boolean)

    /** Pins/unpins a message id on the chat (drives the S23 pinned banner). */
    suspend fun pinMessage(chatId: String, messageId: String)
    suspend fun unpinMessage(chatId: String, messageId: String)

    /**
     * Copies messages into each target chat with forwardedFromUserId set to
     * the original sender (own messages keep no forwarded label). A non-blank
     * [comment] is appended to each target as a separate text message.
     */
    suspend fun forwardMessages(messageIds: List<String>, targetChatIds: List<String>, comment: String?)

    /**
     * Sends a voice note built from the recorded duration + waveform samples
     * (the mock does not need a real microphone; M4c wires real audio).
     */
    suspend fun sendVoice(chatId: String, durationMs: Long, waveformSamples: List<Int>, replyToMessageId: String? = null): String

    /** Blocks/unblocks a conversation (composer state + mock reply gating). */
    suspend fun setBlocked(chatId: String, blocked: Boolean)

    /** People mentionable in a group chat (members, excluding the current user). */
    fun observeMentionCandidates(chatId: String): Flow<List<User>>

    /* ---------- Chat list actions (mock/local state, M3) ---------- */

    suspend fun setArchived(chatId: String, archived: Boolean)
    suspend fun setPinned(chatId: String, pinned: Boolean)
    suspend fun setMuted(chatId: String, muted: Boolean)
    suspend fun markRead(chatId: String)
    suspend fun deleteChats(chatIds: List<String>)
    suspend fun markAllRead()

    /** Simulates a fetch round-trip; seeds any periodic "alive" behaviour. */
    suspend fun refresh()
}
