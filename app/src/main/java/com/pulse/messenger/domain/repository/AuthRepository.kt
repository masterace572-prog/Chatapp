package com.pulse.messenger.domain.repository

import com.pulse.messenger.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

/**
 * Auth contract (PRD §5 - interfaces only).
 *
 * Phase 1 binds [com.pulse.messenger.data.mock.MockAuthRepository];
 * Phase 2 binds a Supabase implementation with identical signatures - the UI
 * layer never knows which one is active. Method surface solidifies in M2 (S03-S17).
 */
interface AuthRepository {
    /** Stream of the current session; UI derives routing from it (S01). */
    fun observeSessionState(): Flow<SessionState>

    /** Email + password (S03-S04). Mock: fails when the password is "wrong". */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /** Phone login entry (S03b). */
    suspend fun signInWithPhone(countryCode: String, phoneNumber: String): Result<Unit>

    /** "Continue with Google" account-picker flow (S05). */
    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun signOut(): Result<Unit>
}
