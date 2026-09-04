package com.pulse.messenger.di

import javax.inject.Qualifier

/** Qualifier for the application-lifetime coroutine scope. */
@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
