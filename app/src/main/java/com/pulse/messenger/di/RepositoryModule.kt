package com.pulse.messenger.di

import com.pulse.messenger.data.local.SettingsRepositoryImpl
import com.pulse.messenger.data.mock.MockAuthRepository
import com.pulse.messenger.data.mock.MockChatRepository
import com.pulse.messenger.data.mock.MockUserRepository
import com.pulse.messenger.domain.repository.AuthRepository
import com.pulse.messenger.domain.repository.ChatRepository
import com.pulse.messenger.domain.repository.SettingsRepository
import com.pulse.messenger.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Phase-1 bindings: interfaces -> mock/local implementations (PRD §5).
 *
 * Phase 2 swaps ONLY this module's right-hand sides for Supabase
 * implementations; UI, domain and ViewModels remain untouched.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: MockAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: MockChatRepository): ChatRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: MockUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
