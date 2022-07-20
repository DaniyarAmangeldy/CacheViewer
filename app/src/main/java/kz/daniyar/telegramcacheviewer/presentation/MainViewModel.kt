package kz.daniyar.telegramcacheviewer.presentation

import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Recomposer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.daniyar.telegramcacheviewer.data.CacheRepository
import kz.daniyar.telegramcacheviewer.data.CacheSource
import kz.daniyar.telegramcacheviewer.domain.entity.Cache

class MainViewModel(private val cacheRepository: CacheRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<Event>()
    val event = MutableSharedFlow<Event>()

    init {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                return@launch
            }
            val uri = cacheRepository.uri
            if (uri == Uri.EMPTY) {
                _uiState.emit(UiState.PermissionRequired)
                return@launch
            }
            onLoadCacheRequest(uri)
        }
    }

    fun onLoadCacheRequest(uri: Uri) = viewModelScope.launch {
        if (_uiState.value is UiState.CacheList) return@launch
        _uiState.emit(UiState.CacheLoading)
        cacheRepository.uri = uri
        val cacheList = cacheRepository
            .getCacheList(uri, CacheSource.Telegram)
        _uiState.emit(UiState.CacheList(cacheList))
    }

    fun onLoadCacheRequest() = viewModelScope.launch {
        onLoadCacheRequestLegacy()
    }

    private suspend fun onLoadCacheRequestLegacy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return
        }
        if (_uiState.value is UiState.CacheList) return
        _uiState.emit(UiState.CacheLoading)
        val cacheList = cacheRepository.getCacheListLegacy(source = CacheSource.Telegram)
        _uiState.emit(UiState.CacheList(cacheList))
    }

    fun onNoReadStoragePermission() = viewModelScope.launch {
        _uiState.emit(UiState.PermissionRequired)
    }

    fun onEventHandled() = viewModelScope.launch {
        _event.emit(Event.Idle)
    }
}

sealed interface UiState {
    object Idle: UiState
    object PermissionRequired: UiState
    class CacheList(val cacheList: List<Cache>): UiState
    object CacheLoading: UiState
}

sealed interface Event {
    object Idle: Event
    object CheckPermission: Event
}