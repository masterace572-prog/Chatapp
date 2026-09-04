package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock profile source. Returns the placeholder identity until M2 wires the
 * session user through the same interface.
 */
@Singleton
class MockUserRepository @Inject constructor() : UserRepository {
    override fun observeMe(): Flow<User?> = flowOf(User.Me)
}
