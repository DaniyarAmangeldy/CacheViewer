package kz.daniyar.telegramcacheviewer.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.lang.ProcessBuilder.Redirect.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.daniyar.telegramcacheviewer.data.Mapper.toCache
import kz.daniyar.telegramcacheviewer.domain.entity.Cache
import kz.daniyar.telegramcacheviewer.utils.AndroidFileSizeFormatter
import kz.daniyar.telegramcacheviewer.utils.FileSizeFormatter
import kz.daniyar.telegramcacheviewer.utils.getAllFilesRecursive

class CacheRepository(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val fileSizeFormatter: FileSizeFormatter
) {

    companion object {
        private const val PREFERENCES_EXTERNAL_STORAGE_URI = "preferences_external_storage_uri"
    }

    var uri: Uri
        get() {
            val value = sharedPreferences.getString(PREFERENCES_EXTERNAL_STORAGE_URI, "")
            if (value.isNullOrEmpty()) {
                return Uri.EMPTY
            }
            return Uri.parse(value)
        }
        set(value) {
            sharedPreferences.edit { putString(PREFERENCES_EXTERNAL_STORAGE_URI, value.toString()) }
        }

    suspend fun getCacheList(uri: Uri, source: CacheSource): List<Cache> {
        val pickedDir = DocumentFile.fromTreeUri(context, uri)
        val cacheList = withContext(Dispatchers.IO) {
            pickedDir?.listFiles()
                ?.firstOrNull { it.name == source.packageName }
                ?.getAllFilesRecursive()
                ?.map { it.toCache(fileSizeFormatter) }
        }.orEmpty()
        return cacheList
    }

    suspend fun getCacheListLegacy(source: CacheSource): List<Cache> {
        val externalStorageDir = Environment.getExternalStorageDirectory()
        val cacheDir = File("$externalStorageDir/Android/data", source.packageName)
        if (!cacheDir.canExecute()) {
            return emptyList()
        }
        if (!cacheDir.exists()) {
            return emptyList()
        }
        return cacheDir.getAllFilesRecursive().map { it.toCache(fileSizeFormatter) }
    }
}