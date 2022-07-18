package kz.daniyar.telegramcacheviewer.data

sealed interface CacheSource {
    val cacheDirPath: String

    object Telegram: CacheSource {
        override val cacheDirPath: String
            get() = "Telegram"
    }
}