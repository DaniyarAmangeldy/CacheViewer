package kz.daniyar.telegramcacheviewer.data

import android.os.Environment
import java.io.File
import kz.daniyar.telegramcacheviewer.utils.getAllFilesRecursive

class CacheRepository {

    fun getCacheList(source: CacheSource): List<File> {
        val externalStorageDir = Environment.getExternalStorageDirectory()
        val applicationDataDir = File("$externalStorageDir/Android/media", source.packageName)
        val cacheDir = File(applicationDataDir, source.cacheDirPath)
        if (!cacheDir.exists()) {
            return emptyList()
        }
        return cacheDir.getAllFilesRecursive()
    }
}