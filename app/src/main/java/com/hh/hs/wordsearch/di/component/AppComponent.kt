package com.hh.hs.wordsearch.di.component

import com.hh.hs.wordsearch.di.modules.AppModule
import com.hh.hs.wordsearch.di.modules.DataSourceModule
import com.hh.hs.wordsearch.di.modules.ViewModelModule
import com.hh.hs.wordsearch.features.FullscreenActivity
import com.hh.hs.wordsearch.features.gamehistory.GameHistoryActivity
import com.hh.hs.wordsearch.features.gameover.GameOverActivity
import com.hh.hs.wordsearch.features.gameplay.GamePlayActivity
import com.hh.hs.wordsearch.features.gamethemeselector.ThemeSelectorActivity
import com.hh.hs.wordsearch.features.mainmenu.MainMenuActivity
import dagger.Component
import javax.inject.Singleton

/**
 * Created by abdularis on 18/07/17.
 */
@Singleton
@Component(modules = [AppModule::class, DataSourceModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(activity: GamePlayActivity)
    fun inject(activity: MainMenuActivity)
    fun inject(activity: GameOverActivity)
    fun inject(activity: FullscreenActivity)
    fun inject(activity: GameHistoryActivity)
    fun inject(activity: ThemeSelectorActivity)
}