package kz.daniyar.telegramcacheviewer.utils

import android.content.Context
import android.text.format.Formatter

interface FileSizeFormatter {

    fun format(sizeBytes: Long): String
}

class AndroidFileSizeFormatter(private val context: Context): FileSizeFormatter {

    override fun format(sizeBytes: Long): String = Formatter.formatShortFileSize(context, sizeBytes)
}