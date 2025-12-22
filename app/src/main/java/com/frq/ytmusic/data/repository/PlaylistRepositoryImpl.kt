package com.frq.ytmusic.data.repository

import com.frq.ytmusic.data.local.dao.PlaylistDao
import com.frq.ytmusic.data.local.entity.PlaylistEntity
import com.frq.ytmusic.data.local.mapper.toCrossRef
import com.frq.ytmusic.data.local.mapper.toDomain
import com.frq.ytmusic.data.local.mapper.toEntity
import com.frq.ytmusic.data.local.mapper.toSong
import com.frq.ytmusic.domain.model.Playlist
import com.frq.ytmusic.domain.model.PlaylistWithSongs
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PlaylistRepository using Room database.
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {
    
    // ==================== Playlist CRUD ====================
    
    override suspend fun createPlaylist(name: String, description: String?): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description
        )
        return playlistDao.insertPlaylist(playlist)
    }
    
    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }
    
    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }
    
    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val songCount = playlistDao.getSongCountSync(entity.id)
                entity.toDomain(songCount)
            }
        }
    }
    
    override fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return combine(
            playlistDao.getPlaylistByIdFlow(playlistId),
            playlistDao.getSongCount(playlistId)
        ) { entity, count ->
            entity?.toDomain(count)
        }
    }
    
    override suspend fun getPlaylistByIdSync(playlistId: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(playlistId)
        return entity?.let {
            val count = playlistDao.getSongCountSync(playlistId)
            it.toDomain(count)
        }
    }
    
    // ==================== Song Operations ====================
    
    override suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val crossRef = song.toCrossRef(playlistId, maxPosition + 1)
        playlistDao.addSongToPlaylist(crossRef)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }
    
    override suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String) {
        playlistDao.removeSongFromPlaylistById(playlistId, videoId)
        playlistDao.updatePlaylistTimestamp(playlistId)
    }
    
    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId).map { crossRefs ->
            crossRefs.map { it.toSong() }
        }
    }
    
    override fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?> {
        return combine(
            getPlaylistById(playlistId),
            getSongsInPlaylist(playlistId)
        ) { playlist, songs ->
            playlist?.let {
                PlaylistWithSongs(playlist = it, songs = songs)
            }
        }
    }
    
    override suspend fun isSongInPlaylist(playlistId: Long, videoId: String): Boolean {
        return playlistDao.isSongInPlaylist(playlistId, videoId)
    }
    
    // ==================== Reordering ====================
    
    override suspend fun reorderSongs(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val songs = playlistDao.getSongsInPlaylist(playlistId).first().toMutableList()
        if (fromIndex in songs.indices && toIndex in songs.indices) {
            val item = songs.removeAt(fromIndex)
            songs.add(toIndex, item)
            playlistDao.reorderSongs(playlistId, songs)
            playlistDao.updatePlaylistTimestamp(playlistId)
        }
    }
}
