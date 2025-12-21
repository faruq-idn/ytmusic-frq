package com.frq.ytmusic.data.remote.mapper

import com.frq.ytmusic.data.remote.dto.MetadataResponseDto
import com.frq.ytmusic.data.remote.dto.SongDto
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.domain.repository.SongMetadata

/**
 * Extension functions to map DTOs to domain models.
 */

fun SongDto.toDomain(): Song {
    return Song(
        videoId = videoId,
        title = title,
        artist = artist,
        album = album,
        durationText = durationText,
        thumbnailUrl = thumbnailUrl
    )
}

fun List<SongDto>.toDomainList(): List<Song> {
    return map { it.toDomain() }
}

fun MetadataResponseDto.toDomain(): SongMetadata {
    return SongMetadata(
        videoId = videoId,
        title = title,
        artist = artist,
        album = album,
        durationSeconds = durationSeconds,
        hasLyrics = hasLyrics,
        lyrics = lyrics
    )
}
