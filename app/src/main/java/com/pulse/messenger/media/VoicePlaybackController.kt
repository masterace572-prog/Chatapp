package com.pulse.messenger.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.pulse.messenger.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * M4c - Real voice-note playback over the bundled raw assets.
 *
 * A process-wide singleton voice player:
 *  - one active MediaPlayer at a time (starting another message stops the
 *    current one);
 *  - play/pause toggle, resume-after-pause and tap-to-seek;
 *  - playback speed 1x / 1.5x / 2x via [MediaPlayer.playbackParams];
 *  - audio focus (pause on loss) and stop-on-leave handled by the host UI.
 *
 * Voice messages are simulated recordings: the mock chat layer stores only a
 * duration and a waveform (see ChatMessage.Voice). The clip is chosen from the
 * bundled raw voices by that duration - short <= 10s, medium <= 20s, otherwise
 * long - so the prepared clip duration stays close to the seeded metadata and,
 * once prepared, becomes the duration the UI shows (VoicePlaybackUi.durationMs).
 *
 * The UI (ConversationScreen) reads [state] and mirrors it into the voice
 * bubble; previews and non-Hilt hosts simply never create the controller.
 */
data class VoicePlaybackUi(
    val activeMessageId: String? = null,
    val isPlaying: Boolean = false,
    /** Real clip length once prepared; 0 while unknown. */
    val durationMs: Int = 0,
    /** Playback-speed index into [VoicePlaybackController.SPEEDS]. */
    val speedIndex: Int = 0,
    /** Fresh-start counter: bumped on every new play of a message so the
     *  bubble resets its visual play-head exactly when audio restarts. */
    val session: Int = 0,
)

@Singleton
class VoicePlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        /** Speeds cycled by the voice bubble chip, kept in sync with PlaybackParams. */
        val SPEEDS = floatArrayOf(1f, 1.5f, 2f)

        /** Raw resource picked for a simulated voice note of [durationSeconds]. */
        fun rawResFor(durationSeconds: Int): Int = when {
            durationSeconds <= 10 -> R.raw.voice_note_short
            durationSeconds <= 20 -> R.raw.voice_note_medium
            else -> R.raw.voice_note_long
        }
    }

    private enum class Mode { Idle, Prepared, Paused, Playing, Completed }

    private val _state = MutableStateFlow(VoicePlaybackUi())
    val state: StateFlow<VoicePlaybackUi> = _state.asStateFlow()

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> pause()
            else -> Unit
        }
    }
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(attributes)
        .setOnAudioFocusChangeListener(focusListener)
        .build()

    private var player: MediaPlayer? = null
    private var mode = Mode.Idle
    private var preparedDurationMs = 0

    /**
     * User tapped a voice note. Same message toggles play/pause (resuming a
     * paused one); any other message (or a completed one) starts fresh.
     */
    fun toggle(messageId: String, durationSeconds: Int) {
        val s = _state.value
        when {
            s.activeMessageId == messageId && mode == Mode.Playing -> pause()
            s.activeMessageId == messageId &&
                (mode == Mode.Paused || mode == Mode.Prepared) -> resume()
            else -> start(messageId, durationSeconds)
        }
    }

    fun pause() {
        val mp = player ?: return
        if (mode == Mode.Playing) {
            runCatching { mp.pause() }
            mode = Mode.Paused
        } else if (mode == Mode.Prepared) {
            // Paused before the clip finished preparing.
            mode = Mode.Paused
        }
        _state.value = _state.value.copy(isPlaying = false)
    }

    /** Seeks the active clip to [fraction] (0..1); a completed clip becomes
     *  paused at the tapped position so the next tap resumes there. */
    fun seekToFraction(fraction: Float) {
        val mp = player ?: return
        if (mode == Mode.Idle) return
        val total = preparedDurationMs.takeIf { it > 0 } ?: mp.duration
        if (total <= 0) return
        val position = (total * fraction.coerceIn(0f, 1f)).toInt()
        runCatching { mp.seekTo(position) }
        if (mode == Mode.Completed) {
            mode = Mode.Paused
            _state.value = _state.value.copy(isPlaying = false)
        }
    }

    /** Cycles 1x -> 1.5x -> 2x on the active player (PlaybackParams). */
    fun cycleSpeed() {
        val mp = player ?: return
        if (mode !in setOf(Mode.Playing, Mode.Paused, Mode.Prepared)) return
        val next = (_state.value.speedIndex + 1) % SPEEDS.size
        runCatching { mp.playbackParams = mp.playbackParams.setSpeed(SPEEDS[next]) }
        _state.value = _state.value.copy(speedIndex = next)
    }

    /** Stops playback and forgets the active message (leave chat / background). */
    fun stop() {
        releasePlayer()
        _state.value = _state.value.copy(
            activeMessageId = null,
            isPlaying = false,
            durationMs = 0,
            speedIndex = 0,
            session = _state.value.session + 1,
        )
    }

    private fun start(messageId: String, durationSeconds: Int) {
        releasePlayer()
        val mp = MediaPlayer()
        player = mp
        mode = Mode.Prepared
        preparedDurationMs = 0
        try {
            mp.setAudioAttributes(attributes)
            mp.setDataSource(
                context,
                Uri.parse("android.resource://${context.packageName}/${rawResFor(durationSeconds)}"),
            )
            mp.setOnPreparedListener { _ ->
                preparedDurationMs = mp.duration.coerceAtLeast(0)
                if (mode == Mode.Paused) {
                    // User paused while preparing: stay paused at the start.
                    _state.value = _state.value.copy(durationMs = preparedDurationMs)
                } else if (mode != Mode.Idle) {
                    mp.start()
                    mode = Mode.Playing
                    _state.value = _state.value.copy(
                        activeMessageId = messageId,
                        isPlaying = true,
                        durationMs = preparedDurationMs,
                    )
                }
            }
            mp.setOnCompletionListener { _ ->
                mode = Mode.Completed
                audioManager.abandonAudioFocusRequest(focusRequest)
                _state.value = _state.value.copy(isPlaying = false)
            }
            mp.setOnErrorListener { _, _, _ ->
                mode = Mode.Idle
                _state.value = _state.value.copy(
                    activeMessageId = null,
                    isPlaying = false,
                    durationMs = 0,
                    session = _state.value.session + 1,
                )
                releasePlayer()
                true
            }
            mp.prepareAsync()
            audioManager.requestAudioFocus(focusRequest)
            // Session bumps on intent, so the bubble resets its head even while
            // the clip is still preparing.
            _state.value = _state.value.copy(
                activeMessageId = messageId,
                durationMs = 0,
                session = _state.value.session + 1,
            )
        } catch (t: Throwable) {
            mode = Mode.Idle
            releasePlayer()
        }
    }

    private fun resume() {
        val mp = player ?: return
        if (mode != Mode.Paused && mode != Mode.Prepared) return
        audioManager.requestAudioFocus(focusRequest)
        runCatching { mp.start() }
        mode = Mode.Playing
        _state.value = _state.value.copy(isPlaying = true)
    }

    private fun releasePlayer() {
        player?.let { mp ->
            runCatching { mp.reset() }
            runCatching { mp.release() }
        }
        player = null
        audioManager.abandonAudioFocusRequest(focusRequest)
        mode = Mode.Idle
    }
}

/**
 * Hilt entry point used by Compose (ConversationScreen) to reach the singleton
 * without ViewModel plumbing; previews run in inspection mode and skip it.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface VoicePlaybackEntryPoint {
    fun voicePlaybackController(): VoicePlaybackController
}

/** Convenience accessor so hosts only depend on this entry point. */
fun voicePlaybackController(context: Context): VoicePlaybackController =
    EntryPointAccessors.fromApplication(
        context.applicationContext,
        VoicePlaybackEntryPoint::class.java,
    ).voicePlaybackController()
