package com.frq.ytmusic.domain.usecase.playlist

import com.frq.ytmusic.domain.model.Playlist
import com.frq.ytmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all playlists.
 */
class GetPlaylistsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }
}
