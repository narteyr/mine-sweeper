package hu.ait.minesweeper.screen
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class CellType {BOMB, NUMBER, EMPTY}


enum class GameState {GAME_STARTED, GAME_OVER}

data class BoardCell(var cellType: CellType, var cellNumber: Int?,  var enabled: Boolean)

data class CellDimension(val row: Int, val col: Int)
class MineSweeperViewModel: ViewModel() {

    var gameState: GameState by  mutableStateOf(GameState.GAME_STARTED )


    val maxRow = 5
    val maxCol = 5
    val totalBombs = 1
    var firstClick = true
    var lockClicks = false

    var flagsLeft = totalBombs


    var elapsedSeconds by mutableIntStateOf(0)
        private set

    var timerJob: Job? = null

    var isTimerActive = false

    var totalReveals = maxRow * maxCol  - totalBombs //number of reveals to before win

    var hasWon by mutableStateOf(false)
    var status by mutableStateOf("")

    var board  = mutableStateListOf<BoardCell>()
        .apply{
            repeat(maxRow * maxCol) {
                add(BoardCell(CellType.EMPTY, null, false))
            }
        }

    var flaggedCoordinates = mutableStateSetOf<CellDimension>()


    fun startTimer() {
        if(isTimerActive) return
        isTimerActive = true
        timerJob = viewModelScope.launch {
            while(isTimerActive) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        elapsedSeconds = 0
        isTimerActive = false
    }


    fun onCellClicked(cellDimension: CellDimension) {
        if (cellDimension in flaggedCoordinates || lockClicks) return

        if (firstClick) {
            startTimer()
            addRandomBombs(cellDimension)
            //after we add bombs, we populate cells that have surrounding mines
            populateCellsWithNumbers()
            firstClick = false
        }
        updateTotalReveals(cellDimension)
        enableCell(cellDimension)

        if(board[mapDimToIndex(cellDimension)].cellType == CellType.EMPTY) {
            openEmptyCells(cellDimension)
        }

        checkWin(cellDimension)
    }

    fun checkWin(cellDimension: CellDimension) {
        Log.d("CHECK_WIN", "$totalReveals")
        val boardIndex = mapDimToIndex(cellDimension)
        if(board[boardIndex].cellType == CellType.BOMB) {
            lockClicks = true
            hasWon = false
            viewModelScope.launch {
                delay(1000)
                onGameOver()
            }
        } else {
            if (totalReveals == 0) {
                lockClicks = true
                hasWon = true
                viewModelScope.launch {
                    delay(1000)
                    onGameOver()
                }
            }
        }
    }

    fun onGameOver(){
        gameState = GameState.GAME_OVER
    }

    fun onStartGame() {
        resetGame()
        gameState = GameState.GAME_STARTED
    }

    fun onCellLongPressed(cellDimension: CellDimension) {
        val boardIndex = mapDimToIndex(cellDimension)
        if (board[boardIndex].enabled) return

        if (cellDimension in flaggedCoordinates) {
            flaggedCoordinates.remove(cellDimension)
        } else {
            flaggedCoordinates.add(cellDimension)
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
            visited[vertexIndex] = true
            for (direction in directions) {
                val row = vertex.row + direction.first
                val col = vertex.col + direction.second
                val neighborDimension = CellDimension(row, col)

                if (row in 0 until maxRow && col in 0 until maxCol && neighborDimension !in flaggedCoordinates) {
                    val neighborCell = CellDimension(vertex.row + direction.first, vertex.col + direction.second)
                    val boardIndex = mapDimToIndex(neighborCell)

                    if (board[boardIndex].cellType == CellType.EMPTY && !flaggedCoordinates.contains(neighborDimension) && !visited[boardIndex]) {
                        stack.add(neighborCell)
                        updateTotalReveals(neighborDimension)
                        board[boardIndex] = board[boardIndex].copy(
                            enabled = true
                        )
                    } else if (board[boardIndex].cellType == CellType.NUMBER) {
                        updateTotalReveals(neighborDimension)
                        board[boardIndex] = board[boardIndex].copy(
                            enabled = true
                        )
                    }
                }
            }
        }
    }


    fun resetGame() {
        stopTimer()
        firstClick = true
        lockClicks = false

        flagsLeft = totalBombs

        totalReveals = maxRow * maxCol - totalBombs //number of reveals to before win

        hasWon = false
        status = ""

        board.clear()
        board.apply {
            repeat(maxCol * maxRow) {
                add(BoardCell(CellType.EMPTY, null, false))
            }
        }
        flaggedCoordinates.clear()
    }


    fun updateTotalReveals(cellDimension: CellDimension) {
        val boardIndex = mapDimToIndex(cellDimension)
        val boardCell = board[boardIndex]
        if(boardCell.cellType != CellType.BOMB && !boardCell.enabled) {
            totalReveals--;
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

        val bombIndices = (0 until maxRow * maxCol)
            .filter{it != lockedIndex}
            .shuffled()
            .take(totalBombs)

        for (i in bombIndices) {
            board[i] = board[i].copy(
                cellType = CellType.BOMB
            )
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
