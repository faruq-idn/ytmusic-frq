package com.frq.ytmusic.data.local.mapper

import com.frq.ytmusic.data.local.entity.PlaylistEntity
import com.frq.ytmusic.data.local.entity.PlaylistSongCrossRef
import com.frq.ytmusic.domain.model.Playlist
import com.frq.ytmusic.domain.model.Song

/**
 * Extension functions to map between Entity and Domain models for Playlist.
 */

fun PlaylistEntity.toDomain(songCount: Int = 0): Playlist {
    return Playlist(
        id = id,
        name = name,
        description = description,
        thumbnailUrl = thumbnailUrl,
        songCount = songCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        description = description,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun PlaylistSongCrossRef.toSong(): Song {
    return Song(
        videoId = videoId,
        title = title,
        artist = artist,
        album = null,
        durationText = duration,
        thumbnailUrl = thumbnailUrl
    )
}

fun Song.toCrossRef(playlistId: Long, position: Int): PlaylistSongCrossRef {
    return PlaylistSongCrossRef(
        playlistId = playlistId,
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        duration = durationText,
        position = position
    )
}
