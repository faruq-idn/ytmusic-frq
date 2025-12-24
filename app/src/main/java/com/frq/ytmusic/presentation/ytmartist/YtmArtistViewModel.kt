package com.frq.ytmusic.presentation.ytmartist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frq.ytmusic.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class YtmArtistViewModel @Inject constructor(
    private val repository: SongRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<YtmArtistUiState>(YtmArtistUiState.Loading)
    val uiState: StateFlow<YtmArtistUiState> = _uiState.asStateFlow()
    
    private var hasLoaded = false

    init {
        // Try browseId first (normal path)
        val browseId = savedStateHandle.get<String>("browseId") ?: ""
        if (browseId.isNotEmpty()) {
            loadArtist(browseId)
            hasLoaded = true
        }
        // artistName will be handled by setByNameMode()
    }
    
    fun setByNameMode() {
        if (hasLoaded) return
        hasLoaded = true
        
        val artistNameEncoded = savedStateHandle.get<String>("artistName") ?: ""
        if (artistNameEncoded.isNotEmpty()) {
            val artistName = URLDecoder.decode(artistNameEncoded, StandardCharsets.UTF_8.toString())
            loadArtistByName(artistName)
        } else {
            _uiState.value = YtmArtistUiState.Error("Artist name missing")
        }
    }
    
    private fun loadArtistByName(artistName: String) {
        viewModelScope.launch {
            _uiState.value = YtmArtistUiState.Loading
            // First find the browseId by searching
            val browseId = repository.findArtistBrowseId(artistName)
            if (browseId != null) {
                loadArtist(browseId)
            } else {
                _uiState.value = YtmArtistUiState.Error("Artist not found: $artistName")
            }
        }
    }

    private fun loadArtist(browseId: String) {
        viewModelScope.launch {
            _uiState.value = YtmArtistUiState.Loading
            repository.getArtist(browseId)
                .onSuccess { artist ->
                    _uiState.value = YtmArtistUiState.Success(artist)
                }
                .onFailure { error ->
                    _uiState.value = YtmArtistUiState.Error(
                        error.message ?: "Failed to load artist"
                    )
                }
        }
    }
}
