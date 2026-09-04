package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock profile source. Exposes the static placeholder identity [User.Me];
 * the signed-in session user lives on AuthRepository. No UI consumer yet -
 * wire this to the session user when the first profile screen lands (M5,
 * Settings S54+). The interface keeps the UI decoupled from the session source.
 */
@Singleton
class MockUserRepository @Inject constructor() : UserRepository {
    override fun observeMe(): Flow<User?> = flowOf(User.Me)
}
