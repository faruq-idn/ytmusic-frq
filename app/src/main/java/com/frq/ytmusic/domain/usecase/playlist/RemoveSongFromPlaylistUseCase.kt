package com.frq.ytmusic.domain.usecase.playlist

import com.frq.ytmusic.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to remove a song from a playlist.
 */
class RemoveSongFromPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, videoId: String) {
        repository.removeSongFromPlaylist(playlistId, videoId)
    }
}
