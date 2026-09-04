package com.pulse.messenger.ui.util

import com.pulse.messenger.R

/**
 * Maps bundled sample-media references to resources.
 *
 * Seed/send uris use one of three shapes:
 *  - `sample://photos/<stem>.jpg`  -> drawable-nodpi photo (Coil model)
 *  - `sample://videos/<stem>.mp4`  -> raw video + matching video_thumb_<n> still
 *  - `sample://files/<name>`       -> raw document (opened via cache + FileProvider)
 * Content:// uris from the Photo Picker/SAF never pass through here - Coil and
 * the system pickers load them directly.
 *
 * Asset provenance: every bundled item is license-free and synthesized for the
 * demo (photos/stickers/videos/audio rendered locally; docs are tiny samples).
 * No network, no attribution obligations. See docs/M4_READINESS.md (M4c).
 */
object SampleMedia {

    private val photoStems: Map<String, Int> = mapOf(
        "photo_hills" to R.drawable.photo_hills,
        "photo_city" to R.drawable.photo_city,
        "photo_park" to R.drawable.photo_park,
        "photo_tea" to R.drawable.photo_tea,
        "photo_books" to R.drawable.photo_books,
        "photo_beach" to R.drawable.photo_beach,
        "photo_route" to R.drawable.photo_route,
        "photo_palette" to R.drawable.photo_palette,
    )

    private val videoRaws: Map<String, Int> = mapOf(
        "sample_video_1" to R.raw.sample_video_1,
        "sample_video_2" to R.raw.sample_video_2,
    )

    private val videoThumbs: Map<String, Int> = mapOf(
        "sample_video_1" to R.drawable.video_thumb_1,
        "sample_video_2" to R.drawable.video_thumb_2,
    )

    private val fileRaws: Map<String, Int> = mapOf(
        "sample_archive.zip" to R.raw.sample_archive,
        "sample_doc_pdf.pdf" to R.raw.sample_doc_pdf,
        "sample_doc_docx.docx" to R.raw.sample_doc_docx,
        "sample_audio.mp3" to R.raw.sample_audio,
    )

    private val stickerKeys: Map<String, Int> = mapOf(
        "sticker_pulse" to R.drawable.sticker_pulse,
        "sticker_run" to R.drawable.sticker_run,
        "sticker_sun" to R.drawable.sticker_sun,
    )

    /** Voice-note audio by duration class (seeds use 7/15/28s on purpose). */
    private fun voiceRawFor(durationSeconds: Int): Int = when {
        durationSeconds <= 7 -> R.raw.voice_note_short
        durationSeconds <= 15 -> R.raw.voice_note_medium
        else -> R.raw.voice_note_long
    }

    /** Coil model for one bubble image: drawable res id for samples, or the raw uri. */
    fun imageModel(uri: String): Any? {
        val trimmed = uri.trim()
        if (trimmed.startsWith("sample://")) {
            return photoRes(trimmed)
        }
        return trimmed.takeIf { it.isNotEmpty() }
    }

    /** Drawable res for a `sample://photos/...` uri, or null. */
    fun photoRes(uri: String): Int? {
        val stem = uri.substringAfterLast('/').substringBeforeLast('.').ifEmpty { return null }
        return photoStems[stem]
    }

    /** Raw video res for a `sample://videos/...` uri, or null (content uris). */
    fun videoRawRes(uri: String): Int? {
        val stem = uri.substringAfterLast('/').substringBeforeLast('.').ifEmpty { return null }
        return videoRaws[stem]
    }

    /** Thumbnail drawable for a video uri (bundled samples only). */
    fun videoThumbRes(uri: String): Int? {
        val stem = uri.substringAfterLast('/').substringBeforeLast('.').ifEmpty { return null }
        return videoThumbs[stem]
    }

    /** Raw res behind a `sample://files/...` uri, or null. */
    fun fileRawRes(uri: String): Int? {
        val name = uri.substringAfterLast('/').ifEmpty { return null }
        return fileRaws[name]
    }

    /** Whether this uri refers to a bundled file (openable via cache). */
    fun isBundledFile(uri: String): Boolean = fileRawRes(uri) != null

    /** Sticker drawable for an asset key, or null when the key is unknown. */
    fun stickerRes(assetKey: String): Int? = stickerKeys[assetKey]

    /** Raw audio res for a voice-note duration class. */
    fun voiceRawRes(durationSeconds: Int): Int = voiceRawFor(durationSeconds)
}
