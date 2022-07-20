package kz.daniyar.telegramcacheviewer.di

import android.content.Context
import kz.daniyar.telegramcacheviewer.data.CacheRepository
import kz.daniyar.telegramcacheviewer.presentation.MainViewModel
import kz.daniyar.telegramcacheviewer.utils.AndroidFileSizeFormatter
import kz.daniyar.telegramcacheviewer.utils.FileSizeFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory<FileSizeFormatter> {
        AndroidFileSizeFormatter(androidContext())
    }

    factory {
        CacheRepository(
            context = androidContext(),
            fileSizeFormatter = get(),
            sharedPreferences = androidContext().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        )
    }

    viewModel {
        MainViewModel(cacheRepository = get())
    }
}