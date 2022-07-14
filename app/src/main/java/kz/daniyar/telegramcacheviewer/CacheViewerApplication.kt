package kz.daniyar.telegramcacheviewer

import android.app.Application
import kz.daniyar.telegramcacheviewer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CacheViewerApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CacheViewerApplication)
            modules(appModule)
        }
    }
}