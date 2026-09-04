package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.ChatFolder
import com.pulse.messenger.domain.model.ChatKind
import kotlinx.coroutines.flow.Flow

/**
 * Chat folders contract (PRD S22). Persisted locally via DataStore in Phase 1;
 * the UI stays repository-driven so Phase 2 can sync folders without changes.
 */
interface FoldersRepository {

    /** Ordered folders; the index IS the display order. */
    fun observeFolders(): Flow<List<ChatFolder>>

    suspend fun addFolder(name: String, includeKinds: Set<ChatKind>, onlyUnread: Boolean)

    suspend fun updateFolder(folder: ChatFolder)

    suspend fun deleteFolder(id: String)

    /** Move the folder one slot up (index-1) - stable ordering controls. */
    suspend fun moveUp(id: String)

    /** Move the folder one slot down (index+1) - stable ordering controls. */
    suspend fun moveDown(id: String)
}
