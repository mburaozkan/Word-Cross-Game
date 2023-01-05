package com.hh.hs.wordsearch

import android.app.Application
import com.hh.hs.wordsearch.di.component.AppComponent
import com.hh.hs.wordsearch.di.component.DaggerAppComponent
import com.hh.hs.wordsearch.di.modules.AppModule

/**
 * Created by abdularis on 18/07/17.
 */
class WordSearchApp : Application() {
    lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

}