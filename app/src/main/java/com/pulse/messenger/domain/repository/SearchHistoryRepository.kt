package com.pulse.messenger.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Recent-search history (PRD S20): latest first, deduplicated, capped at
 * [MaxEntries]. DataStore-backed in Phase 1.
 */
interface SearchHistoryRepository {
    fun observeRecent(): Flow<List<String>>
    suspend fun add(query: String)
    suspend fun remove(query: String)
    suspend fun clear()

    companion object {
        const val MaxEntries = 8
    }
}
