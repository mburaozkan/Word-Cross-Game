package com.hh.hs.wordsearch.features.gameplay

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hh.hs.wordsearch.commons.SingleLiveEvent
import com.hh.hs.wordsearch.commons.Timer
import com.hh.hs.wordsearch.commons.Timer.OnTimeoutListener
import com.hh.hs.wordsearch.commons.Util
import com.hh.hs.wordsearch.commons.orZero
import com.hh.hs.wordsearch.data.room.UsedWordDataSource
import com.hh.hs.wordsearch.data.room.WordDataSource
import com.hh.hs.wordsearch.data.sqlite.GameDataSource
import com.hh.hs.wordsearch.features.settings.Preferences
import com.hh.hs.wordsearch.model.*
import com.hh.hs.wordsearch.model.UsedWord.AnswerLine
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.math.max

class GamePlayViewModel @Inject constructor(
    private val gameDataSource: GameDataSource,
    private val wordDataSource: WordDataSource,
    private val usedWordDataSource: UsedWordDataSource,
    private val preferences: Preferences
) : ViewModel() {

    abstract class GameState
    internal class Generating(
        var rowCount: Int,
        var colCount: Int,
        var name: String
    ) : GameState()

    internal class Loading : GameState()
    internal class Finished(var gameData: GameData?, var win: Boolean) : GameState()
    internal class Paused : GameState()
    internal class Playing(var gameData: GameData?) : GameState()

    class AnswerResult(
        var correct: Boolean,
        var usedWord: UsedWord?,
        var totalAnsweredWord: Int
    )

    private val gameDataCreator: GameDataCreator = GameDataCreator()
    private var currentGameData: GameData? = null
    private val timer: Timer = Timer(TIMER_TIMEOUT.toLong())
    private var currentDuration = 0
    private var currentUsedWord: UsedWord? = null
    private var currentState: GameState? = null

    private lateinit var onTimerLiveData: MutableLiveData<Int>
    private lateinit var onCountDownLiveData: MutableLiveData<Int>
    private lateinit var onCurrentWordCountDownLiveData: MutableLiveData<Int>
    private lateinit var onGameStateLiveData: MutableLiveData<GameState>
    private lateinit var onAnswerResultLiveData: SingleLiveEvent<AnswerResult>
    private lateinit var onCurrentWordChangedLiveData: MutableLiveData<UsedWord>

    val onTimer: LiveData<Int>
        get() = onTimerLiveData

    val onCountDown: LiveData<Int>
        get() = onCountDownLiveData

    val onGameState: LiveData<GameState>
        get() = onGameStateLiveData

    val onAnswerResult: LiveData<AnswerResult>
        get() = onAnswerResultLiveData

    val onCurrentWordChanged: LiveData<UsedWord>
        get() = onCurrentWordChangedLiveData

    val onCurrentWordCountDown: LiveData<Int>
        get() = onCurrentWordCountDownLiveData

    init {
        timer.addOnTimeoutListener(object : OnTimeoutListener {
            override fun onTimeout(elapsedTime: Long) {
                onTimerTimeout()
            }
        })
        resetLiveData()
    }

    fun stopGame() {
        currentGameData = null
        timer.stop()
        resetLiveData()
    }

    fun pauseGame() {
        if (currentState !is Playing) return

        currentGameData?.let {
            if (!it.isFinished && !it.isGameOver) {
                timer.stop()
                setGameState(Paused())
            }
        }
    }

    fun resumeGame() {
        if (currentState is Paused) {
            timer.start()
            setGameState(Playing(currentGameData))
        }
    }

    @SuppressLint("CheckResult")
    fun loadGameRound(gid: Int) {
        if (currentState !is Generating) {
            setGameState(Loading())
            Observable
                .create { e: ObservableEmitter<GameData?> ->
                    e.onNext(gameDataSource.getGameDataSync(gid)!!)
                    e.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { gameData: GameData? ->
                    currentGameData = gameData
                    currentDuration = currentGameData!!.duration
                    startGame()
                }
        }
    }

    @SuppressLint("CheckResult")
    fun generateNewGameRound(
        rowCount: Int,
        colCount: Int,
        gameThemeId: Int,
        gameMode: GameMode,
        difficulty: Difficulty
    ) {
        if (currentState is Generating) return

        val gameName = gameDataName
        setGameState(Generating(rowCount, colCount, gameName))
        val maxChar = max(rowCount, colCount)
        val flowableWords: Flowable<List<Word>>
        flowableWords = if (gameThemeId == GameTheme.NONE.id) {
            wordDataSource.getWords(maxChar)
        } else {
            wordDataSource.getWords(gameThemeId, maxChar)
        }
        flowableWords.toObservable()
            .flatMap { words: List<Word> ->
                Flowable.fromIterable(words)
                    .distinct(Word::string)
                    .map { word: Word ->
                        word.string = word.string.toUpperCase(Locale.getDefault())
                        word
                    }
                    .toList()
                    .toObservable()
            }
            .flatMap { words: MutableList<Word> ->
                val gameData = gameDataCreator.newGameData(words, rowCount, colCount, gameName, gameMode)
                if (gameMode === GameMode.CountDown) {
                    gameData.maxDuration = getMaxCountDownDuration(gameData.usedWords.size, difficulty)
                } else if (gameMode === GameMode.Marathon) {
                    val maxDuration = getMaxDurationPerWord(difficulty)
                    for (usedWord in gameData.usedWords) {
                        usedWord.maxDuration = maxDuration
                    }
                }
                Observable.just(gameData)
            }
            .doOnNext { gameRound: GameData? -> gameDataSource.saveGameData(gameRound!!) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { gameData: GameData? ->
                currentDuration = 0
                currentGameData = gameData
                startGame()
            }
    }

    fun answerWord(answerStr: String, answerLine: AnswerLine?, reverseMatching: Boolean) {
        if (currentState !is Playing) return
        val correctWord: UsedWord? = if (currentGameData?.gameMode == GameMode.Marathon) {
            if (matchCurrentUsedWord(answerStr, reverseMatching)) {
                currentUsedWord
            } else {
                null
            }
        } else {
            findUsedWord(answerStr, reverseMatching)
        }
        var correct = false
        if (correctWord != null) {
            correctWord.answerLine = answerLine
            correct = true
        }
        onAnswerResultLiveData.value = AnswerResult(
            correct,
            correctWord,
            currentGameData?.answeredWordsCount.orZero()
        )
        if (correct) {
            Completable.create { gameDataSource.markWordAsAnswered(correctWord!!) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
            if (currentGameData!!.isFinished) {
                timer.stop()
                finishGame(true)
            } else if (currentGameData!!.gameMode === GameMode.Marathon) {
                nextWord()
            }
        }
    }

    private fun startGame() {
        setGameState(Playing(currentGameData))
        currentGameData?.let {
            if (!it.isFinished && !it.isGameOver) {
                if (it.gameMode == GameMode.Marathon) {
                    nextWord()
                }

                timer.start()
            }
        }
    }

    private fun setGameState(state: GameState) {
        currentState = state
        onGameStateLiveData.value = currentState
    }

    private val gameDataName: String
        get() {
            val num = preferences.previouslySavedGameDataCount.toString()
            preferences.incrementSavedGameDataCount()
            return "Puzzle - $num"
        }

    private fun getMaxCountDownDuration(usedWordsCount: Int, difficulty: Difficulty): Int {
        return when {
            difficulty === Difficulty.Easy -> usedWordsCount * 19 // 19s per word
            difficulty === Difficulty.Medium -> usedWordsCount * 10 // 10s per word
            else -> usedWordsCount * 5 // 5s per word
        }
    }

    private fun getMaxDurationPerWord(difficulty: Difficulty): Int {
        return when {
            difficulty === Difficulty.Easy -> 25 // 19s per word
            difficulty === Difficulty.Medium -> 16 // 10s per word
            else -> 10 // 5s per word
        }
    }

    private fun nextWord() {
        currentGameData?.let {
            if (it.gameMode === GameMode.Marathon) {
                currentUsedWord = null

                for (usedWord in it.usedWords) {
                    if (!usedWord.isAnswered && !usedWord.isTimeout) {
                        currentUsedWord = usedWord
                        break
                    }
                }

                currentUsedWord?.let {
                    timer.stop()
                    timer.start()
                    onCurrentWordChangedLiveData.value = currentUsedWord
                }
            }
        }
    }

    private fun finishGame(win: Boolean) {
        setGameState(Finished(currentGameData, win))
    }

    private fun findUsedWord(word: String, enableReverse: Boolean): UsedWord? {
        val answerStrRev = Util.getReverseString(word)
        for (usedWord in currentGameData?.usedWords.orEmpty()) {
            if (usedWord.isAnswered) continue

            val currUsedWord = usedWord.string
            if (currUsedWord.equals(word, ignoreCase = true) ||
                currUsedWord.equals(answerStrRev, ignoreCase = true) && enableReverse) {
                return usedWord
            }
        }
        return null
    }

    private fun matchCurrentUsedWord(word: String, enableReverse: Boolean): Boolean {
        if (currentUsedWord == null) return false

        val answerStrRev = Util.getReverseString(word)
        val currUsedWord = currentUsedWord!!.string
        return currUsedWord.equals(word, ignoreCase = true) ||
            currUsedWord.equals(answerStrRev, ignoreCase = true) && enableReverse
    }

    private fun onTimerTimeout() {
        currentGameData?.let { gameData ->
            if (timer.isStarted) {
                gameData.duration = ++currentDuration
                val gameMode = gameData.gameMode
                if (gameMode === GameMode.CountDown) {
                    onCountDownLiveData.value = gameData.remainingDuration
                    if (gameData.remainingDuration <= 0) {
                        val win = gameData.answeredWordsCount ==
                            gameData.usedWords.size
                        timer.stop()
                        finishGame(win)
                    }
                } else if (gameMode == GameMode.Marathon) {
                    currentUsedWord?.let { usedWord ->
                        usedWord.duration = usedWord.duration + 1
                        onCurrentWordCountDownLiveData.value = usedWord.maxDuration - usedWord.duration
                        Completable
                            .create { e: CompletableEmitter ->
                                usedWordDataSource.updateUsedWordDuration(
                                    usedWord.id,
                                    usedWord.duration)
                                e.onComplete()
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()

                        if (usedWord.isTimeout) {
                            timer.stop()
                            finishGame(false)
                        }
                    }
                }
                onTimerLiveData.value = currentDuration
                gameDataSource.saveGameDataDuration(gameData.id, currentDuration)
            }
        }
    }

    private fun resetLiveData() {
        onTimerLiveData = MutableLiveData()
        onCountDownLiveData = MutableLiveData()
        onGameStateLiveData = MutableLiveData()
        onAnswerResultLiveData = SingleLiveEvent()
        onCurrentWordChangedLiveData = MutableLiveData()
        onCurrentWordCountDownLiveData = MutableLiveData()
    }

    companion object {
        private const val TIMER_TIMEOUT = 1000
    }
}