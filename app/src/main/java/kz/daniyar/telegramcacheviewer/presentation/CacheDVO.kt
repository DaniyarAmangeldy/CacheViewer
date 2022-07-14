package kz.daniyar.telegramcacheviewer.presentation

import java.io.File
import kz.daniyar.telegramcacheviewer.utils.FileSizeFormatter

data class CacheDVO(
    val name: String,
    val size: String,
    val extension: String
) {
    companion object {

        fun from(file: File, fileSizeFormatter: FileSizeFormatter) = CacheDVO(
            name = file.name,
            size = fileSizeFormatter.format(file.length()),
            extension = file.extension
        )
    }
}