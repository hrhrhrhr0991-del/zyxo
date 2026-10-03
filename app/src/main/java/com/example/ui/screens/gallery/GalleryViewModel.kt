package com.example.ui.screens.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GeneratedImageEntity
import com.example.data.local.GeneratedVideoEntity
import com.example.data.local.SavedPromptEntity
import com.example.data.repository.AppLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GalleryUiState(
    val selectedTab: Int = 0, // 0: Images, 1: Videos, 2: Prompts
    val selectedPromptCategory: String = "All",
    val selectedFullscreenImage: GeneratedImageEntity? = null
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val localRepo = AppLocalRepository(db)

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    val images: StateFlow<List<GeneratedImageEntity>> = localRepo.getAllImages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val videos: StateFlow<List<GeneratedVideoEntity>> = localRepo.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val prompts: StateFlow<List<SavedPromptEntity>> = localRepo.getAllPrompts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setPromptCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedPromptCategory = category)
    }

    fun openFullscreenImage(image: GeneratedImageEntity) {
        _uiState.value = _uiState.value.copy(selectedFullscreenImage = image)
    }

    fun closeFullscreenImage() {
        _uiState.value = _uiState.value.copy(selectedFullscreenImage = null)
    }

    fun deleteImage(image: GeneratedImageEntity) {
        viewModelScope.launch {
            localRepo.deleteImage(image)
        }
    }

    fun deleteVideo(video: GeneratedVideoEntity) {
        viewModelScope.launch {
            localRepo.deleteVideo(video)
        }
    }

    fun deletePrompt(prompt: SavedPromptEntity) {
        viewModelScope.launch {
            localRepo.deletePrompt(prompt)
        }
    }
}
