package kz.daniyar.telegramcacheviewer.data

import androidx.documentfile.provider.DocumentFile
import java.io.File
import kz.daniyar.telegramcacheviewer.domain.entity.Cache
import kz.daniyar.telegramcacheviewer.utils.FileSizeFormatter

object Mapper {

    fun DocumentFile.toCache(fileSizeFormatter: FileSizeFormatter) = Cache(
        name = name.orEmpty(),
        size = fileSizeFormatter.format(length()),
        extension = type.orEmpty()
    )

    fun File.toCache(fileSizeFormatter: FileSizeFormatter) = Cache(
        name = name.orEmpty(),
        size = fileSizeFormatter.format(length()),
        extension = extension
    )
}