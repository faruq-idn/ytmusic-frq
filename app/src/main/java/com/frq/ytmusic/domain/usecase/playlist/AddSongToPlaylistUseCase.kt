package com.frq.ytmusic.domain.usecase.playlist

import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to add a song to a playlist.
 */
class AddSongToPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, song: Song) {
        repository.addSongToPlaylist(playlistId, song)
    }
}
