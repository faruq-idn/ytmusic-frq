package com.frq.ytmusic.domain.usecase.playlist

import com.frq.ytmusic.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to create a new playlist.
 */
class CreatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, description: String? = null): Long {
        return repository.createPlaylist(name, description)
    }
}
