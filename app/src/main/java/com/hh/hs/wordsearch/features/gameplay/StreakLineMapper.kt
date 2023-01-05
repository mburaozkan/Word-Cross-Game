package com.hh.hs.wordsearch.features.gameplay

import com.hh.hs.wordsearch.commons.Mapper
import com.hh.hs.wordsearch.custom.StreakView.StreakLine
import com.hh.hs.wordsearch.model.UsedWord.AnswerLine

/**
 * Created by abdularis on 09/07/17.
 */
class StreakLineMapper : Mapper<AnswerLine, StreakLine>() {
    override fun map(obj: AnswerLine): StreakLine {
        return StreakLine().apply {
            startIndex.set(obj.startRow, obj.startCol)
            endIndex.set(obj.endRow, obj.endCol)
            color = obj.color
        }
    }

    override fun revMap(obj: StreakLine): AnswerLine {
        return AnswerLine(
            startRow = obj.startIndex.row,
            startCol = obj.startIndex.col,
            endRow = obj.endIndex.row,
            endCol = obj.endIndex.col,
            color = obj.color
        )
    }
}