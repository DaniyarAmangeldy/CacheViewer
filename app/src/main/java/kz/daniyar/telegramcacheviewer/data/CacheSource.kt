package kz.daniyar.telegramcacheviewer.data

sealed interface CacheSource {
    val packageName: String

    object Telegram: CacheSource {
        override val packageName: String
            get() = "org.telegram.messenger"
    }
}