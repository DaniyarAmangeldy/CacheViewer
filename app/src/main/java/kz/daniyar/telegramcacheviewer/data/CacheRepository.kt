package kz.daniyar.telegramcacheviewer.data

import android.os.Environment
import java.io.File
import kz.daniyar.telegramcacheviewer.utils.getAllFilesRecursive

class CacheRepository {

    fun getCacheList(source: CacheSource): List<File> {
        val storageDir = Environment.getExternalStorageDirectory()
        val cacheDir = File(storageDir, source.cacheDirPath)
        if (!cacheDir.exists()) {
            return emptyList()
        }
        return cacheDir.getAllFilesRecursive()
    }
}