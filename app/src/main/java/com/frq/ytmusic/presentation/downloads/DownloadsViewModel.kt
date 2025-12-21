package com.frq.ytmusic.presentation.downloads

import com.frq.ytmusic.data.local.DownloadState
import com.frq.ytmusic.data.local.entity.DownloadedSongEntity
import com.frq.ytmusic.domain.repository.DownloadRepository
import com.frq.ytmusic.domain.usecase.DeleteDownloadUseCase
import com.frq.ytmusic.domain.usecase.GetDownloadsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class DownloadsUiState(
    val downloads: List<DownloadedSongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentDownload: DownloadState = DownloadState.Idle
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val getDownloadsUseCase: GetDownloadsUseCase,
    private val deleteDownloadUseCase: DeleteDownloadUseCase,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState(isLoading = true))
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        loadDownloads()
        observeDownloadState()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            try {
                getDownloadsUseCase().collect { downloads ->
                    _uiState.value = _uiState.value.copy(downloads = downloads, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = DownloadsUiState(error = e.message, isLoading = false)
            }
        }
    }

    private fun observeDownloadState() {
        viewModelScope.launch {
            downloadRepository.downloadState.collect { state ->
                _uiState.value = _uiState.value.copy(currentDownload = state)
            }
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            deleteDownloadUseCase(videoId)
                .onFailure { e ->
                    e.printStackTrace()
                }
        }
    }
}
