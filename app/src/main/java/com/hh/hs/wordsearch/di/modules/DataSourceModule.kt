package com.hh.hs.wordsearch.di.modules

import android.content.Context
import com.hh.hs.wordsearch.data.room.GameDatabase
import com.hh.hs.wordsearch.data.room.GameThemeDataSource
import com.hh.hs.wordsearch.data.room.UsedWordDataSource
import com.hh.hs.wordsearch.data.room.WordDataSource
import com.hh.hs.wordsearch.data.sqlite.DbHelper
import com.hh.hs.wordsearch.data.sqlite.GameDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by abdularis on 18/07/17.
 */
@Module
class DataSourceModule {
    @Provides
    @Singleton
    fun provideGameDatabase(context: Context): GameDatabase {
        return GameDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDbHelper(context: Context): DbHelper {
        return DbHelper(context)
    }

    @Provides
    @Singleton
    fun provideGameRoundDataSource(
        dbHelper: DbHelper,
        usedWordDataSource: UsedWordDataSource
    ): GameDataSource {
        return GameDataSource(dbHelper, usedWordDataSource)
    }

    @Provides
    @Singleton
    fun provideGameThemeDataSource(gameDatabase: GameDatabase): GameThemeDataSource {
        return gameDatabase.gameThemeDataSource
    }

    @Provides
    @Singleton
    fun provideWordDataSource(gameDatabase: GameDatabase): WordDataSource {
        return gameDatabase.wordDataSource
    }

    @Provides
    @Singleton
    fun provideUsedWordDataSource(gameDatabase: GameDatabase): UsedWordDataSource {
        return gameDatabase.usedWordDataSource
    }
}