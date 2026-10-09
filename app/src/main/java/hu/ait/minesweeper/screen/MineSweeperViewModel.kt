package hu.ait.minesweeper.screen
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlin.math.max


enum class CellType {BOMB, NUMBER, FLAG, EMPTY}

data class BoardCell(var cellType: CellType, var cellNumber: Int?,  var enabled: Boolean)

data class CellDimension(val row: Int, val col: Int)
class MineSweeperViewModel: ViewModel() {
    val maxRow = 6
    val maxCol = 6

    var firstClick = true

    var board  = mutableStateListOf<BoardCell>()
        .apply{
            repeat(maxRow * maxCol) {
                add(BoardCell(CellType.EMPTY, null, false))
            }
        }


    fun onCellClicked(cellDimension: CellDimension) {
        if (firstClick) {
            addRandomBombs(cellDimension)
            //after we add bombs, we populate cells that have surrounding mines
            populateCellsWithNumbers()
            firstClick = false
        }
        enableCell(cellDimension)

        if(board[mapDimToIndex(cellDimension)].cellType == CellType.EMPTY) {
            openEmptyCells(cellDimension)
        }
    }

    fun mapDimToIndex(cellDimension: CellDimension): Int {
        return maxRow * cellDimension.row + cellDimension.col
    }

    fun openEmptyCells(cellDimension: CellDimension) {
        val visited  = Array(maxRow * maxCol) { false }

        val directions = listOf(
            -1 to 0, // top
            0 to -1, // left
            0 to 1, // right
            1 to 0, // bottom
        )

        val stack = mutableListOf<CellDimension>()
        stack.add(cellDimension)

        while (!stack.isEmpty()) {
            val vertex = stack.removeAt(stack.lastIndex)
            val vertexIndex = mapDimToIndex(vertex)
            visited[vertexIndex] = !visited[vertexIndex] //potential bug(intention: setting to true)

            for (direction in directions) {
                val row = vertex.row + direction.first
                val col = vertex.col + direction.second

                if (row in 0 until maxRow && col in 0 until maxCol) {
                    val neighborCell = CellDimension(vertex.row + direction.first, vertex.col + direction.second)
                    val boardIndex = mapDimToIndex(neighborCell)

                    if (board[boardIndex].cellType == CellType.EMPTY && !visited[boardIndex]) {
                        stack.add(neighborCell)
                        board[boardIndex] = board[boardIndex].copy(
                            enabled = true
                        )
                    } else if (board[boardIndex].cellType == CellType.NUMBER) {
                        board[boardIndex] = board[boardIndex].copy(
                            enabled = true
                        )
                    }
                }
            }
        }
    }


    fun enableCell(cellDimension: CellDimension) {
        val boardIndex = mapDimToIndex(cellDimension)
        board[boardIndex] = board[boardIndex].copy(
            enabled = true
        )
    }

    fun addRandomBombs(lockedCell: CellDimension) {
        val lockedIndex = maxRow * lockedCell.row + lockedCell.col

        repeat(4) {
            val randomIndex = (0 until maxRow * maxCol).random()

            if (randomIndex != lockedIndex) {
                board[randomIndex] = board[randomIndex].copy(
                    cellType = CellType.BOMB,
                )
            }
        }

    }


    fun populateCellsWithNumbers () {
        for( i in 0 until maxRow) {
            for (j in 0 until maxCol) {
                val boardIndex = maxRow * i + j

                if (board[boardIndex].cellType == CellType.BOMB) {
                    continue
                }

                val neighbors = getNeighbors(CellDimension(i, j))

                var countBombs = 0
                for (neighbor in neighbors) {
                    if (board[neighbor].cellType == CellType.BOMB){
                        countBombs += 1
                    }

                }

                if (countBombs > 0) {
                    board[boardIndex] = board[boardIndex].copy(
                        cellType = CellType.NUMBER,
                        cellNumber = countBombs,
                    )
                } else {
                    board[boardIndex] = board[boardIndex].copy(
                        cellType = CellType.EMPTY,
                    )
                }

                Log.d("INDEX_DEBUG", "$boardIndex, neighbors: $neighbors countBombs: $countBombs")


            }
        }

    }


    fun getNeighbors(boardDimension: CellDimension): List<Int> {
        val validNeighbors = mutableListOf<Int>()

        val directions: List<Pair<Int, Int>> =  listOf(
            -1 to -1, //top-left
            -1 to 0, //top
            -1 to 1, //top-right
            0 to -1, //left
            0 to 1, // right
            1 to -1, //bottom-left
            1 to 0, // bottom
            1 to 1, //bottom-right
        )

        for (direction in directions) {

            val row = boardDimension.row + direction.first
            val col = boardDimension.col + direction.second

            if (row in 0 until maxRow && col in 0 until maxCol) {
                val boardIndex = maxRow * row + col
                validNeighbors.add(boardIndex)
            }
        }

        return validNeighbors
    }

}
