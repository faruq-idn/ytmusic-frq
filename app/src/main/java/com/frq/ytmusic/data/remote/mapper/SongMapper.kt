package com.frq.ytmusic.data.remote.mapper

import com.frq.ytmusic.data.remote.dto.LyricsDto
import com.frq.ytmusic.data.remote.dto.LyricsLineDto
import com.frq.ytmusic.data.remote.dto.MetadataResponseDto
import com.frq.ytmusic.data.remote.dto.SongDto
import com.frq.ytmusic.domain.model.Lyrics
import com.frq.ytmusic.domain.model.LyricsLine
import com.frq.ytmusic.domain.model.LyricsType
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
        lyrics = lyrics?.toDomain()
    )
}

fun LyricsDto.toDomain(): Lyrics {
    return Lyrics(
        type = if (type == "synced") LyricsType.SYNCED else LyricsType.PLAIN,
        lines = lines.map { it.toDomain() },
        source = source
    )
}

fun LyricsLineDto.toDomain(): LyricsLine {
    return LyricsLine(
        text = text,
        startTimeMs = startTimeMs,
        endTimeMs = endTimeMs
    )
}
