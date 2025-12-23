package com.frq.ytmusic.presentation.ytmplaylist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frq.ytmusic.domain.repository.PlaylistRepository
import com.frq.ytmusic.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for YTM playlist detail screen.
 */
@HiltViewModel
class YtmPlaylistViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(YtmPlaylistUiState())
    val uiState: StateFlow<YtmPlaylistUiState> = _uiState.asStateFlow()

    private val playlistId: String = savedStateHandle.get<String>("playlistId") ?: ""

    init {
        if (playlistId.isNotEmpty()) {
            loadPlaylist()
        }
    }

    fun loadPlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            songRepository.getPlaylist(playlistId)
                .onSuccess { playlist ->
                    _uiState.update { 
                        it.copy(
                            playlist = playlist,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load playlist"
                        )
                    }
                }
        }
    }

    /**
     * Toggle save status: Save to library if not saved, otherwise delete from library.
     */
    fun savePlaylist() {
        val currentState = _uiState.value
        if (currentState.isSaved) {
            deleteLocalPlaylist(currentState.localPlaylistId)
        } else {
            createLocalPlaylist()
        }
    }

    private fun createLocalPlaylist() {
        val ytmPlaylist = _uiState.value.playlist ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                // Create local playlist
                val newPlaylistId = playlistRepository.createPlaylist(
                    name = ytmPlaylist.title,
                    description = ytmPlaylist.description
                )
                
                // Add all songs to playlist
                ytmPlaylist.songs.forEach { song ->
                    playlistRepository.addSongToPlaylist(newPlaylistId, song)
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        isSaved = true,
                        localPlaylistId = newPlaylistId
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Failed to save playlist")
                }
            }
        }
    }

    private fun deleteLocalPlaylist(localId: Long?) {
        if (localId == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                playlistRepository.deletePlaylist(localId)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        isSaved = false,
                        localPlaylistId = null
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "Failed to remove playlist")
                }
            }
        }
    }

    /**
     * Download all songs in playlist (placeholder - requires download manager).
     */
    fun downloadAll() {
        val songs = _uiState.value.playlist?.songs ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, downloadProgress = 0) }
            
            // TODO: Implement actual download logic using DownloadManager
            // For now, just simulate progress
            songs.forEachIndexed { index, _ ->
                kotlinx.coroutines.delay(100)
                _uiState.update { it.copy(downloadProgress = index + 1) }
            }
            
            _uiState.update { it.copy(isDownloading = false) }
        }
    }
}
