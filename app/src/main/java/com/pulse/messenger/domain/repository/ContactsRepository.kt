package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * People directory contract (PRD §5). Phase 1 serves seeded contacts so the
 * Chats list and global search (S20) can resolve names/avatars; M5 grows it
 * into the full People experience. Phase 2 backs it with Supabase profiles.
 */
interface ContactsRepository {
    fun observeContacts(): Flow<List<User>>
}
