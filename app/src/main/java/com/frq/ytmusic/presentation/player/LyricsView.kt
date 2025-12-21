package com.frq.ytmusic.presentation.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frq.ytmusic.domain.model.Lyrics

@Composable
fun LyricsView(
    lyrics: Lyrics,
    currentPosition: Long,
    onLineClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Find active line index
    val activeIndex = lyrics.lines.indexOfLast { line ->
        (line.startTimeMs ?: 0L) <= currentPosition
    }.coerceAtLeast(0)

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            // Scroll to center the active line
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -300 // Offset to center somewhat
            )
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 200.dp), // Add padding to allow scrolling top/bottom items to center
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val isActive = index == activeIndex
            
            // Animations
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1.1f else 1.0f,
                animationSpec = tween(durationMillis = 300),
                label = "scale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.5f,
                animationSpec = tween(durationMillis = 300),
                label = "alpha"
            )
            val color by animateColorAsState(
                targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                animationSpec = tween(durationMillis = 300),
                label = "color"
            )
            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { 
                        line.startTimeMs?.let { onLineClick(it) } 
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.Center
                    ),
                    color = color,
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .padding(horizontal = 32.dp)
                )
            }
        }
    }
}
