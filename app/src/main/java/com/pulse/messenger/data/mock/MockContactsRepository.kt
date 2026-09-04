package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock people directory: seeded contacts from SeedData with a simulated
 * first-load delay. M5 grows this into the full People experience.
 */
@Singleton
class MockContactsRepository @Inject constructor() : ContactsRepository {
    override fun observeContacts(): Flow<List<User>> = flow {
        Simulator.shortDelay()
        emit(SeedData.contacts)
    }
}
