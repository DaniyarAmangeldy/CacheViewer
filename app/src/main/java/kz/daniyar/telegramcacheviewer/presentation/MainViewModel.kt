package kz.daniyar.telegramcacheviewer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.daniyar.telegramcacheviewer.data.CacheRepository
import kz.daniyar.telegramcacheviewer.data.CacheSource
import kz.daniyar.telegramcacheviewer.utils.FileSizeFormatter

class MainViewModel(
    private val cacheRepository: CacheRepository,
    private val fileSizeFormatter: FileSizeFormatter
) : ViewModel() {

    private val _cacheList = MutableStateFlow<List<CacheDVO>>(emptyList())
    val cacheList = _cacheList.asStateFlow()

    fun onLoadCacheRequest() = viewModelScope.launch {
        val cacheList = cacheRepository
            .getCacheList(CacheSource.Telegram)
            .map { CacheDVO.from(it, fileSizeFormatter) }
        _cacheList.emit(cacheList)
    }
}