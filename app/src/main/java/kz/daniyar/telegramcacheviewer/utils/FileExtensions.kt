package kz.daniyar.telegramcacheviewer.utils

import androidx.documentfile.provider.DocumentFile
import java.io.File

fun DocumentFile.getAllFilesRecursive(): List<DocumentFile> {
    if (isDirectory) {
        return listFiles()?.flatMap { it.getAllFilesRecursive() }.orEmpty()
    }
    if (isFile) {
        return listOf(this)
    }
    return emptyList()
}

fun File.getAllFilesRecursive(): List<File> {
    if (isDirectory) {
        return listFiles()?.flatMap { it.getAllFilesRecursive() }.orEmpty()
    }
    if (isFile) {
        return listOf(this)
    }
    return emptyList()
}