package com.hh.hs.wordsearch.features.gameplay

import com.hh.hs.wordsearch.custom.LetterGridDataAdapter

/**
 * Created by abdularis on 09/07/17.
 */
class ArrayLetterGridDataAdapter internal constructor(
    private var backedGrid: Array<CharArray>
) : LetterGridDataAdapter() {

    var grid: Array<CharArray>?
        get() = backedGrid
        set(grid) {
            if (grid != null && !grid.contentEquals(backedGrid)) {
                backedGrid = grid
                setChanged()
                notifyObservers()
            }
        }

    override fun getColCount(): Int {
        return backedGrid[0].size
    }

    override fun getRowCount(): Int {
        return backedGrid.size
    }

    override fun getLetter(row: Int, col: Int): Char {
        return backedGrid[row][col]
    }

}