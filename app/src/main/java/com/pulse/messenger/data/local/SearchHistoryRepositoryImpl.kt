package com.pulse.messenger.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulse.messenger.domain.repository.SearchHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchDataStore by preferencesDataStore(name = "pulse_search_history")

/**
 * Recent-search history (PRD S20) persisted with DataStore, latest first,
 * deduplicated, capped at [SearchHistoryRepository.MaxEntries].
 */
@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SearchHistoryRepository {

    private object Keys {
        val RecentJson = stringPreferencesKey("recent_json")
    }

    override fun observeRecent(): Flow<List<String>> =
        context.searchDataStore.data.map { prefs ->
            decode(prefs[Keys.RecentJson])
        }

    override suspend fun add(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        context.searchDataStore.edit { prefs ->
            val current = decode(prefs[Keys.RecentJson]).toMutableList()
            current.remove(q)
            current.add(0, q)
            prefs[Keys.RecentJson] = encode(current.take(SearchHistoryRepository.MaxEntries))
        }
    }

    override suspend fun remove(query: String) {
        context.searchDataStore.edit { prefs ->
            val current = decode(prefs[Keys.RecentJson]).toMutableList()
            current.remove(query)
            prefs[Keys.RecentJson] = encode(current)
        }
    }

    override suspend fun clear() {
        context.searchDataStore.edit { prefs -> prefs.remove(Keys.RecentJson) }
    }

    private fun encode(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList { for (i in 0 until arr.length()) add(arr.getString(i)) }
        }.getOrDefault(emptyList())
    }
}
