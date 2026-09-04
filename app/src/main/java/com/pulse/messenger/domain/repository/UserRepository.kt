package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Profile/people contract (PRD §5). Feeds "me" (S54) and later contacts (M5).
 * Kept minimal until those milestones define the exact surface.
 */
interface UserRepository {
    /** The signed-in profile; null until auth exists (M2). */
    fun observeMe(): Flow<User?>
}
