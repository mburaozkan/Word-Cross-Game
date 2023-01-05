package com.hh.hs.wordsearch.features.gamehistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hh.hs.wordsearch.data.sqlite.GameDataSource
import com.hh.hs.wordsearch.features.settings.Preferences
import com.hh.hs.wordsearch.model.GameDataInfo
import javax.inject.Inject

class GameHistoryViewModel @Inject constructor(
    private val gameDataSource: GameDataSource,
    private val preferences: Preferences
) : ViewModel() {

    private val _onGameDataInfoLoaded: MutableLiveData<List<GameDataInfo>> = MutableLiveData()
    val onGameDataInfoLoaded: LiveData<List<GameDataInfo>>
        get() = _onGameDataInfoLoaded

    suspend fun loadGameHistory() {
        val gameDataList = gameDataSource.getGameDataInfoList()
        _onGameDataInfoLoaded.value = gameDataList
        if (gameDataList.isEmpty()) {
            preferences.resetSaveGameDataCount()
        }
    }

    suspend fun deleteGameData(gameDataId: Int) {
        gameDataSource.deleteGameData(gameDataId)
        loadGameHistory()
    }

    suspend fun clear() {
        gameDataSource.clearGameData()
        _onGameDataInfoLoaded.value = emptyList()
        preferences.resetSaveGameDataCount()
    }
}