package com.frq.ytmusic.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frq.ytmusic.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the search screen.
 * Handles search logic with debouncing.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        // Debounced search
        searchQuery
            .debounce(500) // Wait 500ms after typing stops
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Update search query (triggers debounced search).
     */
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchQuery.value = query
        
        // Clear results if query is empty
        if (query.isBlank()) {
            _uiState.update { 
                it.copy(songs = emptyList(), isEmpty = false, error = null) 
            }
        }
    }

    /**
     * Perform immediate search (e.g., when user presses search button).
     */
    fun search() {
        val query = _uiState.value.query
        if (query.length >= 2) {
            viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        searchSongsUseCase(query)
            .onSuccess { songs ->
                _uiState.update { 
                    it.copy(
                        songs = songs,
                        isLoading = false,
                        isEmpty = songs.isEmpty(),
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
     * Clear search and reset state.
     */
    fun clearSearch() {
        searchQuery.value = ""
        _uiState.update { SearchUiState() }
    }
}
