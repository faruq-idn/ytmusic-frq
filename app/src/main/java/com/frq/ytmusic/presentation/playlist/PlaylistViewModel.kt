package com.frq.ytmusic.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frq.ytmusic.domain.model.Playlist
import com.frq.ytmusic.domain.model.PlaylistWithSongs
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.usecase.playlist.AddSongToPlaylistUseCase
import com.frq.ytmusic.domain.usecase.playlist.CreatePlaylistUseCase
import com.frq.ytmusic.domain.usecase.playlist.DeletePlaylistUseCase
import com.frq.ytmusic.domain.usecase.playlist.GetPlaylistWithSongsUseCase
import com.frq.ytmusic.domain.usecase.playlist.GetPlaylistsUseCase
import com.frq.ytmusic.domain.usecase.playlist.RemoveSongFromPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val playlists: List<Playlist> = emptyList(),
    val currentPlaylist: PlaylistWithSongs? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val getPlaylistWithSongsUseCase: GetPlaylistWithSongsUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState(isLoading = true))
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            try {
                getPlaylistsUseCase().collect { playlists ->
                    _uiState.value = _uiState.value.copy(
                        playlists = playlists,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadPlaylistDetails(playlistId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                getPlaylistWithSongsUseCase(playlistId).collect { playlistWithSongs ->
                    _uiState.value = _uiState.value.copy(
                        currentPlaylist = playlistWithSongs,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            try {
                createPlaylistUseCase(name, description)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase(playlistId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            try {
                addSongToPlaylistUseCase(playlistId, song)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, videoId: String) {
        viewModelScope.launch {
            try {
                removeSongFromPlaylistUseCase(playlistId, videoId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
