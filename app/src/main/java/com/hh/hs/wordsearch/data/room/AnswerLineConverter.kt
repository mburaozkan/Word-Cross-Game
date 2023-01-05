package com.hh.hs.wordsearch.data.room

import androidx.room.TypeConverter
import com.hh.hs.wordsearch.model.UsedWord.AnswerLine

object AnswerLineConverter {
    @JvmStatic
    @TypeConverter
    fun answerLineToString(answerLine: AnswerLine?): String? {
        return answerLine?.toString()
    }

    @JvmStatic
    @TypeConverter
    fun stringToAnswerLine(answerLineData: String?): AnswerLine? {
        if (answerLineData == null) return null
        val answerLine = AnswerLine()
        answerLine.fromString(answerLineData)
        return answerLine
    }
}