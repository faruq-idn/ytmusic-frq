package com.frq.ytmusic.presentation.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.frq.ytmusic.domain.model.Lyrics
import com.frq.ytmusic.domain.model.Song
import com.frq.ytmusic.presentation.player.LyricsView

/**
 * Fullscreen tab overlay for Queue, Lyrics, and Related content.
 */
@Composable
fun TabOverlayContent(
    activeTab: String,
    songTitle: String,
    songThumbnailUrl: String?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    // Queue
    queue: List<Song> = emptyList(),
    currentIndex: Int = -1,
    onPlayFromQueue: (Int) -> Unit = {},
    // Lyrics
    lyrics: Lyrics? = null,
    isLyricsLoading: Boolean = false,
    currentPosition: Long = 0L,
    onSeekToLine: (Long) -> Unit = {},
    // Related
    relatedSongs: List<Song> = emptyList(),
    isRelatedLoading: Boolean = false,
    onPlayRelated: (Song) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            // Block all touch events from passing through to background
            .pointerInput(Unit) { awaitPointerEventScope { while (true) { awaitPointerEvent() } } }
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with song info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Artwork
                AsyncImage(
                    model = songThumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.DarkGray)
                )
                
                // Tab title & song title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when(activeTab) {
                            "queue" -> "Antrean"
                            "lyrics" -> "Lirik"
                            "related" -> "Terkait"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = songTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Play/Pause
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                
                // Close
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.KeyboardArrowDown, "Close", tint = Color.White)
                }
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            
            // Content Area based on activeTab
            when (activeTab) {
                "queue" -> {
                    QueueContent(
                        queue = queue,
                        currentIndex = currentIndex,
                        onPlayFromQueue = onPlayFromQueue,
                        modifier = Modifier.weight(1f)
                    )
                }
                "lyrics" -> {
                    LyricsContent(
                        lyrics = lyrics,
                        isLoading = isLyricsLoading,
                        currentPosition = currentPosition,
                        onSeekToLine = onSeekToLine,
                        modifier = Modifier.weight(1f)
                    )
                }
                "related" -> {
                    RelatedContent(
                        relatedSongs = relatedSongs,
                        isLoading = isRelatedLoading,
                        onPlayRelated = onPlayRelated,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsContent(
    lyrics: Lyrics?,
    isLoading: Boolean,
    currentPosition: Long,
    onSeekToLine: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                androidx.compose.material3.CircularProgressIndicator(
                    color = Color.White
                )
            }
            lyrics != null -> {
                LyricsView(
                    lyrics = lyrics,
                    currentPosition = currentPosition,
                    onLineClick = onSeekToLine
                )
            }
            else -> {
                Text(
                    text = "Lirik tidak tersedia",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}


