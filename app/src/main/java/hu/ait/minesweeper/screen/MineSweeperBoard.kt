package hu.ait.minesweeper.screen

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min


@Composable
fun MinSweeperBoard (
    maxRow: Int,
    maxCol: Int,
    modifier: Modifier,
    board: List<BoardCell>,
    onCellClicked: (CellDimension) -> Unit
) {
    val borderStrokeWidth = 5.dp
    val mineColor = MaterialTheme.colorScheme.primary

    val gridColor = MaterialTheme.colorScheme.outline

    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(fontSize = 30.sp, color = Color.Red)

    Canvas(
        modifier = modifier.fillMaxWidth(0.8f)
            .aspectRatio(1.0f)
            .border(
                color = gridColor,
                width = borderStrokeWidth
            )
            .pointerInput(key1 = Unit) {
                detectTapGestures {
                    offset ->

                    val y = (offset.y / (size.height / maxCol)).toInt()
                    val x = (offset.x / (size.width / maxRow)).toInt()
                    Log.d("TAG_CLICKS", "$x $y")

                    onCellClicked(CellDimension(x, y))
                }
            }
    ) {

        val gridSize = size.minDimension
        val cellRowSize = gridSize / maxRow
        val cellColSize = gridSize / maxCol

        for (i in 1 .. maxCol) {
            drawLine(
                color = gridColor,
                strokeWidth = borderStrokeWidth.toPx(),
                start = Offset(cellColSize * i, 0f),
                end = Offset(cellColSize * i, gridSize)
            )
        }

        for (i in 1 .. maxRow) {
            drawLine(
                color = gridColor,
                strokeWidth = borderStrokeWidth.toPx(),
                start = Offset(0f, cellRowSize * i),
                end = Offset(gridSize, cellRowSize * i)
            )
        }

        val iconSize = min(cellRowSize, cellColSize) * 0.75f
        // Draw Cell Types
        for (i in 0 until maxRow) {
            for (j in 0 until maxCol) {
                val centerX = i * cellRowSize + cellRowSize / 2
                val centerY = j * cellColSize + cellColSize / 2

                // [0,2]
                // [maxRow * row + col]

                val boardIndex = maxRow * i + j
                val boardCell = board[boardIndex]
                val centerOffset = Offset(centerX, centerY)
                if (boardCell.enabled) {
                    //Mine
                    when {
                        (boardCell.cellType == CellType.BOMB) -> mineIcon(centerOffset, iconSize, mineColor)
                        (boardCell.cellType == CellType.NUMBER) -> drawNumber(centerOffset, boardCell.cellNumber, textMeasurer, style)
                    }

                } else {
                    drawRect(
                        color = Color.Gray,
                        topLeft = Offset(i * cellRowSize, j * cellColSize),
                        size = Size(cellRowSize, cellColSize)
                    )
                }
            }
        }
    }
}


fun DrawScope.mineIcon(c: Offset, iconSize: Float, color: Color) {
    val r = iconSize * 0.36f
    val spikeW = r * 0.22f
    val spikeEnd = iconSize * 0.5f

    repeat(8) {
        i ->
        rotate(i * 45f, pivot = c) {
            drawRect(
                color = color,
                topLeft = Offset(c.x - spikeW / 2, c.y - spikeEnd),
                size = Size(spikeW, spikeEnd - r * 0.8f)
            )
        }
    }

    //fill Color
    val body = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(c, r))
        addOval(Rect(c, r * 0.22f))
    }
    drawPath(body, color)
}


fun DrawScope.drawNumber(
    c: Offset,
    number: Int?,
    textMeasurer: TextMeasurer,
    style: TextStyle
) {


    val layout: TextLayoutResult = textMeasurer.measure(number.toString(), style)
    drawText(
        textMeasurer = textMeasurer,
        topLeft = Offset(c.x - layout.size.width / 2, c.y - layout.size.height / 2),
        text = number.toString(),
        style = style
    )
}