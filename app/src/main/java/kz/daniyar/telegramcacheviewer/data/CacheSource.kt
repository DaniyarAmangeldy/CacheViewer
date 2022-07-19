package kz.daniyar.telegramcacheviewer.data

sealed interface CacheSource {
    val packageName: String

    val cacheDirPath: String

    object Telegram: CacheSource {
        override val packageName: String
            get() = "org.telegram.messenger"
        override val cacheDirPath: String
            get() = "Telegram"
    }
}