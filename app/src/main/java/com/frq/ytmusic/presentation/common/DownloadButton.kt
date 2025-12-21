package com.frq.ytmusic.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.frq.ytmusic.data.local.DownloadState

@Composable
fun DownloadButton(
    downloadState: DownloadState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (downloadState) {
                is DownloadState.Downloading -> {
                    val progress by animateFloatAsState(
                        targetValue = downloadState.progress,
                        label = "progress"
                    )
                    
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                    )
                    
                    // Optional: Small stop/pause icon inside? 
                    // For now handling click as cancel/delete is implied but let's keep it simple
                }
                
                is DownloadState.Completed -> {
                    Icon(
                        imageVector = Icons.Default.DownloadDone,
                        contentDescription = "Downloaded",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                is DownloadState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Retry Download",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                else -> { // Idle
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
