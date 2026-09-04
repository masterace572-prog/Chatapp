package com.pulse.messenger.ui.screens.chats

import com.pulse.messenger.domain.model.ChatFolder
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.model.ChatSummary

/** Built-in filter chips of the Chats list (S19). */
enum class ChatFilterKind { All, Unread, Groups, Personal }

/** Pure matching rules so filters stay unit-testable and out of the UI. */
object ChatFilters {

    fun matches(kind: ChatFilterKind, summary: ChatSummary): Boolean = when (kind) {
        ChatFilterKind.All -> true
        ChatFilterKind.Unread -> summary.unreadCount > 0
        ChatFilterKind.Groups -> summary.isGroup
        ChatFilterKind.Personal -> !summary.isGroup
    }

    /**
     * A user folder (S22) matches when the chat kind is included AND the
     * unread-only flag (when set) is satisfied. Folders without rules match
     * nothing - the editor requires at least one rule.
     */
    fun matches(folder: ChatFolder, summary: ChatSummary): Boolean {
        if (folder.includeKinds.isEmpty()) return false
        val kindOk = summary.kind in folder.includeKinds
        val unreadOk = !folder.onlyUnread || summary.unreadCount > 0
        return kindOk && unreadOk
    }

    /** Convenience used by the folder editor to preview include counts. */
    fun countIn(folder: ChatFolder, summaries: List<ChatSummary>): Int =
        summaries.count { matches(folder, it) }
}
