package com.frq.ytmusic.presentation.ytmartist

import com.frq.ytmusic.domain.model.YtmArtistDetail

/**
 * UI state for artist detail screen.
 */
sealed interface YtmArtistUiState {
    object Loading : YtmArtistUiState
    data class Success(val artist: YtmArtistDetail) : YtmArtistUiState
    data class Error(val message: String) : YtmArtistUiState
}
