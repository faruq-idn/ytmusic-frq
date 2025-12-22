package com.frq.ytmusic.domain.usecase.playlist

import com.frq.ytmusic.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to delete a playlist.
 */
class DeletePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }
}
