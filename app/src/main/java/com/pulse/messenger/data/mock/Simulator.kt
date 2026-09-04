package com.pulse.messenger.data.mock

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Phase-1 network feel (PRD §5): simulated 300-800ms latency so loading,
 * skeletons and disable states behave like the real backend will.
 */
object Simulator {
    suspend fun networkDelay() {
        delay(Random.nextLong(300, 800))
    }

    /** Short "perceived" delay for instant-ish operations. */
    suspend fun shortDelay() {
        delay(Random.nextLong(120, 260))
    }
}
