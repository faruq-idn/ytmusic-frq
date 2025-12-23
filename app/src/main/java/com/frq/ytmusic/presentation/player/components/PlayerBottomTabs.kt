package com.frq.ytmusic.presentation.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Bottom tabs for player (Berikutnya, Lirik, Terkait).
 * Uses weights for even distribution and easier clicking.
 */
@Composable
fun PlayerBottomTabs(
    activeTab: String?,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabInteractionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        PlayerTab(
            text = "BERIKUTNYA",
            isActive = activeTab == "queue",
            onClick = { onTabClick("queue") },
            interactionSource = tabInteractionSource,
            modifier = Modifier.weight(1f)
        )
        PlayerTab(
            text = "LIRIK",
            isActive = activeTab == "lyrics",
            onClick = { onTabClick("lyrics") },
            interactionSource = tabInteractionSource,
            modifier = Modifier.weight(1f)
        )
        PlayerTab(
            text = "TERKAIT",
            isActive = activeTab == "related",
            onClick = { onTabClick("related") },
            interactionSource = tabInteractionSource,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlayerTab(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}
