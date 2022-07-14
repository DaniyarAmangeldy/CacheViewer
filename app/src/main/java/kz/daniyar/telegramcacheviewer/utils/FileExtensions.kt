package kz.daniyar.telegramcacheviewer.utils

import java.io.File

fun File.getAllFilesRecursive(): List<File> {
    if (isDirectory) {
        return listFiles()?.flatMap { it.getAllFilesRecursive() }.orEmpty()
    }
    if (isFile) {
        return listOf(this)
    }
    return emptyList()
}