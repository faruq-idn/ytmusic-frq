package com.frq.ytmusic.presentation.player.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Full player header with collapse button and more options menu.
 */
@Composable
fun PlayerHeader(
    isSleepTimerActive: Boolean,
    onCollapse: () -> Unit,
    onSleepTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collapse Button
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Minimize",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // More Options Menu
        Box {
            IconButton(onClick = { showMoreMenu = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Show timer indicator if active
                    if (isSleepTimerActive) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }
            
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false }
            ) {
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = if (isSleepTimerActive) 
                                    "Sleep Timer (Aktif)" 
                                else 
                                    "Sleep Timer"
                            )
                        }
                    },
                    onClick = {
                        showMoreMenu = false
                        onSleepTimerClick()
                    }
                )
            }
        }
    }
}
