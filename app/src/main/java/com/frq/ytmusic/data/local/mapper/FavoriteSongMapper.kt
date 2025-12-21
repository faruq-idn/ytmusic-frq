package com.frq.ytmusic.data.local.mapper

import com.frq.ytmusic.data.local.entity.FavoriteSongEntity
import com.frq.ytmusic.domain.model.Song

/**
 * Mapper functions for FavoriteSongEntity.
 */
fun FavoriteSongEntity.toSong(): Song {
    return Song(
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        durationText = duration
    )
}

fun Song.toFavoriteSongEntity(): FavoriteSongEntity {
    return FavoriteSongEntity(
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        duration = durationText
    )
}
