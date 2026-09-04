package com.pulse.messenger.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Cache exports for ACTION_VIEW / system share of bundled and picked media
 * (M4c). Copies a bundled raw resource into the app cache and returns a
 * FileProvider uri - no storage permissions involved (the provider only
 * exposes our own cache dir; see res/xml/pulse_file_paths.xml).
 */
object CacheFiles {

    /** Copies a bundled raw/drawable resource into [dir] under the cache. */
    fun exportRaw(
        context: Context,
        resId: Int?,
        dir: String,
        name: String,
    ): Uri? {
        if (resId == null) return null
        val target = File(File(context.cacheDir, dir), name)
        return runCatching {
            target.parentFile?.mkdirs()
            context.resources.openRawResource(resId).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.pulsefiles",
                target,
            )
        }.getOrNull()
    }
}
