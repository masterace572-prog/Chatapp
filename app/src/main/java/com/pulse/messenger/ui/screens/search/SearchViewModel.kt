package com.pulse.messenger.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.messenger.domain.model.ChatSummary
import com.pulse.messenger.domain.model.Message
import com.pulse.messenger.domain.model.MessageType
import com.pulse.messenger.domain.model.User
import com.pulse.messenger.domain.repository.ChatRepository
import com.pulse.messenger.domain.repository.ContactsRepository
import com.pulse.messenger.domain.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Global-search result sections (S20). */
data class SearchResults(
    val chats: List<ChatSummary> = emptyList(),
    val people: List<User> = emptyList(),
    val messages: List<Message> = emptyList(),
    val media: List<Message> = emptyList(),
    val links: List<Message> = emptyList(),
    val files: List<Message> = emptyList(),
) {
    val hasAny: Boolean
        get() = chats.isNotEmpty() || people.isNotEmpty() || messages.isNotEmpty() ||
            media.isNotEmpty() || links.isNotEmpty() || files.isNotEmpty()
}

/** Content filters of the search screen (S20). */
enum class SearchFilter { All, Chats, People, Messages, Media }

data class SearchUiState(
    val query: String = "",
    val recent: List<String> = emptyList(),
    /** chatId -> display title for message/media/footer context. */
    val chatNames: Map<String, String> = emptyMap(),
    val searching: Boolean = false,
    val results: SearchResults = SearchResults(),
    val filter: SearchFilter = SearchFilter.All,
    /** True when a query ran and produced no results at all. */
    val noResults: Boolean = false,
) {
    /** Results visible under the active filter, as typed sections. */
    val visible: List<SearchGroup> by lazy {
        val groups = listOf(
            SearchGroup.ChatGroup(results.chats),
            SearchGroup.PersonGroup(results.people),
            SearchGroup.MessageGroup(results.messages, SearchSection.Messages),
            SearchGroup.MessageGroup(results.media, SearchSection.Media),
            SearchGroup.MessageGroup(results.links, SearchSection.Links),
            SearchGroup.MessageGroup(results.files, SearchSection.Files),
        )
        val allowed: Set<SearchSection> = when (filter) {
            SearchFilter.All -> SearchSection.entries.toSet()
            SearchFilter.Chats -> setOf(SearchSection.Chats)
            SearchFilter.People -> setOf(SearchSection.People)
            SearchFilter.Messages -> setOf(SearchSection.Messages, SearchSection.Links)
            SearchFilter.Media -> setOf(SearchSection.Media, SearchSection.Links, SearchSection.Files)
        }
        groups.filter { it.section in allowed && it.isNotEmpty }
    }
}

/** One typed, ordered search-result section (replaces unchecked Any casts). */
sealed interface SearchGroup {
    val section: SearchSection
    val isNotEmpty: Boolean
    val itemCount: Int

    data class ChatGroup(val chats: List<ChatSummary>) : SearchGroup {
        override val section = SearchSection.Chats
        override val isNotEmpty: Boolean get() = chats.isNotEmpty()
        override val itemCount: Int get() = chats.size
    }

    data class PersonGroup(val people: List<User>) : SearchGroup {
        override val section = SearchSection.People
        override val isNotEmpty: Boolean get() = people.isNotEmpty()
        override val itemCount: Int get() = people.size
    }

    data class MessageGroup(val messages: List<Message>, override val section: SearchSection) : SearchGroup {
        override val isNotEmpty: Boolean get() = messages.isNotEmpty()
        override val itemCount: Int get() = messages.size
    }
}

/** Ordered result sections with their display titles. */
enum class SearchSection(val titleRes: Int) {
    Chats(com.pulse.messenger.R.string.search_section_chats),
    People(com.pulse.messenger.R.string.search_section_people),
    Messages(com.pulse.messenger.R.string.search_section_messages),
    Media(com.pulse.messenger.R.string.search_section_media),
    Links(com.pulse.messenger.R.string.search_section_links),
    Files(com.pulse.messenger.R.string.search_section_files),
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    chatRepository: ChatRepository,
    contactsRepository: ContactsRepository,
    private val historyRepository: SearchHistoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(SearchFilter.All)

    private data class BaseData(
        val chats: List<ChatSummary>,
        val contacts: List<User>,
        val messages: List<Message>,
        val recent: List<String>,
        val chatNames: Map<String, String>,
    )

    /** All chats (active + archived) so search can resolve display names. */
    private val allSummaries: Flow<List<ChatSummary>> = combine(
        chatRepository.observeChatSummaries(),
        chatRepository.observeArchivedSummaries(),
    ) { active, archived ->
        active + archived
    }

    private val baseData = combine(
        allSummaries,
        contactsRepository.observeContacts(),
        chatRepository.observeAllMessages(),
        historyRepository.observeRecent(),
    ) { chats: List<ChatSummary>,
        contacts: List<User>,
        messages: List<Message>,
        recent: List<String> ->
        BaseData(
            chats = chats.filterNot { it.isArchived },
            contacts = contacts,
            messages = messages,
            recent = recent,
            chatNames = chats.associate { it.chatId to it.displayName },
        )
    }

    private val queryFlow = query.debounce(280)

    val uiState: StateFlow<SearchUiState> = combine(
        baseData,
        queryFlow,
        filter,
    ) { base: BaseData, q: String, flt: SearchFilter -> Triple(base, q, flt) }
        .flatMapLatest { triple ->
            val (base, q, flt) = triple
            val trimmed = q.trim()
            if (trimmed.isEmpty()) {
                flowOf(
                    SearchUiState(
                        query = q,
                        recent = base.recent,
                        chatNames = base.chatNames,
                        filter = flt,
                    ),
                )
            } else {
                flow {
                    // Pending state while the "request" is in flight.
                    emit(
                        SearchUiState(
                            query = q,
                            recent = base.recent,
                            chatNames = base.chatNames,
                            searching = true,
                            filter = flt,
                        ),
                    )
                    delay(220)
                    val results = buildResults(trimmed, base)
                    emit(
                        SearchUiState(
                            query = q,
                            recent = base.recent,
                            chatNames = base.chatNames,
                            searching = false,
                            results = results,
                            filter = flt,
                            noResults = !results.hasAny,
                        ),
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    private fun buildResults(q: String, base: BaseData): SearchResults {
        val needle = q.lowercase()
        fun matches(text: String) = text.lowercase().contains(needle)

        return SearchResults(
            chats = base.chats.filter { matches(it.displayName) },
            people = base.contacts.filter { matches(it.displayName) || matches(it.username) },
            messages = base.messages.filter {
                it.type == MessageType.Text && matches(it.text) && !isLink(it.text)
            },
            media = base.messages.filter {
                (it.type == MessageType.Image || it.type == MessageType.Video) && matches(it.text)
            },
            links = base.messages.filter { matches(it.text) && isLink(it.text) },
            files = base.messages.filter { it.type == MessageType.File && matches(it.text) },
        )
    }

    /* ---------- Interaction ---------- */

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun selectFilter(f: SearchFilter) {
        filter.value = f
    }

    /** Persist a query into recent history (on submit or result tap). */
    fun runSearch(value: String) {
        val q = value.trim()
        if (q.isNotEmpty()) {
            query.value = q
            viewModelScope.launch { historyRepository.add(q) }
        }
    }

    fun removeRecent(value: String) {
        viewModelScope.launch { historyRepository.remove(value) }
    }

    fun clearRecent() {
        viewModelScope.launch { historyRepository.clear() }
    }

    private fun isLink(text: String) = text.contains("http://") || text.contains("https://")
}
