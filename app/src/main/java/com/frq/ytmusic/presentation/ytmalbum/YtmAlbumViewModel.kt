package com.frq.ytmusic.presentation.ytmalbum

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frq.ytmusic.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for album detail screen.
 */
@HiltViewModel
class YtmAlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val songRepository: SongRepository
) : ViewModel() {

    private val browseId: String = checkNotNull(savedStateHandle["browseId"])

    private val _uiState = MutableStateFlow(YtmAlbumUiState())
    val uiState: StateFlow<YtmAlbumUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
    }

    private fun loadAlbum() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            songRepository.getAlbum(browseId)
                .onSuccess { album ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            album = album,
                            songs = album.songs
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load album"
                        )
                    }
                }
        }
    }

    fun retry() {
        loadAlbum()
    }
}
