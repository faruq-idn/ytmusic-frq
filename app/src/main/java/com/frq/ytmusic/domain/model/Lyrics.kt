package com.frq.ytmusic.domain.model

data class Lyrics(
    val type: LyricsType,
    val lines: List<LyricsLine>,
    val source: String
)

data class LyricsLine(
    val text: String,
    val startTimeMs: Long?,
    val endTimeMs: Long?
)

enum class LyricsType {
    SYNCED,
    PLAIN
}
