package com.frq.ytmusic.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frq.ytmusic.domain.repository.SongRepository
import com.frq.ytmusic.domain.usecase.SearchAllUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the search screen.
 * Suggestions appear as user types; search triggers on Enter press.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAllUseCase: SearchAllUseCase,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var suggestionsJob: Job? = null

    /**
     * Update search query. Fetches suggestions with debounce.
     */
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        
        // Cancel previous suggestions fetch
        suggestionsJob?.cancel()
        
        if (query.isBlank()) {
            // Clear all
            _uiState.update { 
                it.copy(
                    songs = emptyList(), 
                    playlists = emptyList(), 
                    suggestions = emptyList(),
                    isEmpty = false, 
                    error = null
                ) 
            }
        } else if (query.length >= 2) {
            // Fetch suggestions with debounce
            suggestionsJob = viewModelScope.launch {
                delay(300) // Debounce 300ms
                fetchSuggestions(query)
            }
        }
    }

    private suspend fun fetchSuggestions(query: String) {
        songRepository.getSuggestions(query)
            .onSuccess { suggestions ->
                _uiState.update { it.copy(suggestions = suggestions) }
            }
    }

    /**
     * Perform search (when user presses Enter or selects suggestion).
     */
    fun search() {
        suggestionsJob?.cancel()
        val query = _uiState.value.query
        if (query.length >= 2) {
            viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    /**
     * Select a suggestion: fills query and triggers search.
     */
    fun selectSuggestion(suggestion: String) {
        _uiState.update { it.copy(query = suggestion, suggestions = emptyList()) }
        search()
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null, suggestions = emptyList()) }

        searchAllUseCase(query)
            .onSuccess { result ->
                _uiState.update { 
                    it.copy(
                        songs = result.songs,
                        playlists = result.playlists,
                        isLoading = false,
                        isEmpty = result.songs.isEmpty() && result.playlists.isEmpty(),
                        error = null
                    ) 
                }
            }
            .onFailure { exception ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Search failed"
                    ) 
                }
            }
    }

    /**
     * Called when search bar gains focus.
     * Clears results and shows suggestions (like YouTube Music).
     */
    fun onSearchBarFocused() {
        val query = _uiState.value.query
        
        // Clear search results, keep query
        _uiState.update { 
            it.copy(
                songs = emptyList(),
                playlists = emptyList(),
                isEmpty = false,
                error = null
            )
        }
        
        // Fetch suggestions if query exists
        if (query.length >= 2) {
            suggestionsJob?.cancel()
            suggestionsJob = viewModelScope.launch {
                fetchSuggestions(query)
            }
        }
    }

    /**
     * Clear search and reset state.
     */
    fun clearSearch() {
        suggestionsJob?.cancel()
        _uiState.update { SearchUiState() }
    }
}
