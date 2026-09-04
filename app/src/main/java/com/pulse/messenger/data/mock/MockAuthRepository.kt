package com.pulse.messenger.data.mock

import com.pulse.messenger.domain.model.SessionState
import com.pulse.messenger.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock auth (PRD §9):
 *  - any well-formed email + password >= 8 chars succeeds; the literal password
 *    "wrong" always fails (error-state demo);
 *  - phone + OTP flows succeed when OTP is 123456 (M2 wires the UI);
 *  - Google picker always succeeds.
 */
@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {

    private val session = MutableStateFlow<SessionState>(SessionState.LoggedOut)

    override fun observeSessionState(): Flow<SessionState> = session

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        Simulator.networkDelay()
        val validEmail = email.contains("@") && email.contains(".")
        val validPassword = password.length >= 8 && password != "wrong"
        return if (validEmail && validPassword) {
            session.value = SessionState.LoggedOut // real user object arrives with M2
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Invalid email or password"))
        }
    }

    override suspend fun signInWithPhone(countryCode: String, phoneNumber: String): Result<Unit> {
        Simulator.networkDelay()
        val valid = phoneNumber.length >= 7
        return if (valid) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Invalid phone number"))
    }

    override suspend fun signInWithGoogle(): Result<Unit> {
        Simulator.networkDelay()
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        Simulator.shortDelay()
        session.value = SessionState.LoggedOut
        return Result.success(Unit)
    }
}
