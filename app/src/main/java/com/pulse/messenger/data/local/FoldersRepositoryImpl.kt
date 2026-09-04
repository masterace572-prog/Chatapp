package com.pulse.messenger.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulse.messenger.domain.model.ChatFolder
import com.pulse.messenger.domain.model.ChatKind
import com.pulse.messenger.domain.repository.FoldersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.foldersDataStore by preferencesDataStore(name = "pulse_folders")

/**
 * Chat folders persisted with DataStore (PRD S22 + stack: DataStore for
 * settings; org.json keeps the encoding dependency-free).
 */
@Singleton
class FoldersRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FoldersRepository {

    private object Keys {
        val FoldersJson = stringPreferencesKey("folders_json")
    }

    override fun observeFolders(): Flow<List<ChatFolder>> =
        context.foldersDataStore.data.map { prefs ->
            prefs[Keys.FoldersJson]?.let(::decode) ?: SeedFolders
        }

    override suspend fun addFolder(
        name: String,
        includeKinds: Set<ChatKind>,
        onlyUnread: Boolean,
    ) {
        val current = decodeCurrent()
        val folder = ChatFolder(
            id = "f-${System.currentTimeMillis()}",
            name = name.trim(),
            includeKinds = includeKinds,
            onlyUnread = onlyUnread,
        )
        persist(current + folder)
    }

    override suspend fun updateFolder(folder: ChatFolder) {
        val current = decodeCurrent().map {
            if (it.id == folder.id) folder.copy(name = folder.name.trim()) else it
        }
        persist(current)
    }

    override suspend fun deleteFolder(id: String) {
        persist(decodeCurrent().filterNot { it.id == id })
    }

    override suspend fun moveUp(id: String) = move(id, direction = -1)

    override suspend fun moveDown(id: String) = move(id, direction = +1)

    private suspend fun move(id: String, direction: Int) {
        val current = decodeCurrent().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        val target = index + direction
        if (index in current.indices && target in current.indices) {
            val folder = current.removeAt(index)
            current.add(target, folder)
            persist(current)
        }
    }

    private suspend fun decodeCurrent(): List<ChatFolder> =
        context.foldersDataStore.data.first()
            .let { prefs -> decode(prefs[Keys.FoldersJson]) }

    private suspend fun persist(folders: List<ChatFolder>) {
        context.foldersDataStore.edit { prefs ->
            prefs[Keys.FoldersJson] = encode(folders)
        }
    }

    /**
     * First-launch demo folder (never re-seeded once the user edits/deletes:
     * any persisted value - including "[]" - wins).
     */
    private val SeedFolders: List<ChatFolder> = listOf(
        ChatFolder(
            id = "f-work",
            name = "Work",
            includeKinds = setOf(ChatKind.Group),
            onlyUnread = false,
        ),
    )

    private fun encode(folders: List<ChatFolder>): String {
        val arr = JSONArray()
        folders.forEach { f ->
            val kinds = JSONArray()
            f.includeKinds.forEach { kinds.put(it.name) }
            arr.put(
                JSONObject()
                    .put("id", f.id)
                    .put("name", f.name)
                    .put("kinds", kinds)
                    .put("onlyUnread", f.onlyUnread),
            )
        }
        return arr.toString()
    }

    private fun decode(raw: String?): List<ChatFolder> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val kinds = buildSet {
                        val k = o.optJSONArray("kinds")
                        if (k != null) {
                            for (j in 0 until k.length()) {
                                ChatKind.entries.firstOrNull { it.name == k.getString(j) }?.let(::add)
                            }
                        }
                    }
                    add(
                        ChatFolder(
                            id = o.getString("id"),
                            name = o.getString("name"),
                            includeKinds = kinds,
                            onlyUnread = o.optBoolean("onlyUnread", false),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
