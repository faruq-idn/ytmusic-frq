package com.frq.ytmusic.presentation.player.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frq.ytmusic.data.local.DownloadState
import com.frq.ytmusic.presentation.common.DownloadButton
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun PlayerSongInfo(
    title: String,
    artist: String,
    isFavorite: Boolean,
    downloadState: DownloadState,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
    onArtistClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // 1. Title & Artist Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onArtistClick)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 2. Action Buttons Row (Capsule Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like / Favorite Capsule
            PlayerActionChip(
                text = if (isFavorite) "Disukai" else "Suka",
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                onClick = onFavoriteClick,
                isActive = isFavorite
            )

            // Dislike Capsule
            PlayerActionChip(
                text = "Tidak Suka",
                icon = Icons.Outlined.ThumbDown,
                onClick = { /* TODO */ }
            )

            // Download Capsule
            // Custom implementation for DownloadButton inside capsule logic?
            // Let's simplify and make a generic capsule for the visual requested
            // But we need the download state spinner.
            // For now, let's use the standard Capsule UI, but overlay the specific icon
            PlayerActionChip(
                text = "Simpan",
                icon = null, // Custom content
                onClick = onDownloadClick,
                customIcon = {
                    DownloadButton(
                        downloadState = downloadState,
                        onClick = onDownloadClick,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            // Share Capsule
            PlayerActionChip(
                text = "Bagikan",
                icon = Icons.Outlined.Share,
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun PlayerActionChip(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    isActive: Boolean = false,
    customIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isActive) Color.White else Color.White.copy(alpha = 0.1f),
        contentColor = if (isActive) Color.Black else Color.White,
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

