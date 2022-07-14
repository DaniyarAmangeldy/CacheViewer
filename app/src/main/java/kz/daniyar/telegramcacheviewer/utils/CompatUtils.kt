package kz.daniyar.telegramcacheviewer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment

object EnvironmentCompat {

    fun checkReadExternalStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Environment.isExternalStorageManager()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val permission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                return permission == PackageManager.PERMISSION_GRANTED
            }
            else -> return true
        }
    }
}