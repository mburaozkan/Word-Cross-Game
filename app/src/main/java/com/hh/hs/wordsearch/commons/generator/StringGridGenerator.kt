package com.hh.hs.wordsearch.commons.generator

import com.hh.hs.wordsearch.model.Grid

/**
 * Created by abdularis on 06/07/17.
 *
 * Parse dataInput kedalam array grid[][]
 */
class StringGridGenerator : GridGenerator<String?, Boolean>() {
    override fun setGrid(dataInput: String?, grid: Array<CharArray>): Boolean {
        if (dataInput == null) return false
        val trimmed = dataInput.trim { it <= ' ' }
        var row = 0
        var col = 0
        for (element in trimmed) {
            if (element == Grid.GRID_NEWLINE_SEPARATOR) {
                row++
                col = 0
            } else {
                if (row >= grid.size || col >= grid[0].size) {
                    resetGrid(grid)
                    return false
                }
                grid[row][col] = element
                col++
            }
        }
        return true
    }
}