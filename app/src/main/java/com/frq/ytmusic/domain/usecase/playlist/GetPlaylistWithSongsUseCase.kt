package com.frq.ytmusic.domain.usecase.playlist

import com.frq.ytmusic.domain.model.PlaylistWithSongs
import com.frq.ytmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get a playlist with all its songs.
 */
class GetPlaylistWithSongsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(playlistId: Long): Flow<PlaylistWithSongs?> {
        return repository.getPlaylistWithSongs(playlistId)
    }
}
