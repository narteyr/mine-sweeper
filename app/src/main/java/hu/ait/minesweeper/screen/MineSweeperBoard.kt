package hu.ait.minesweeper.screen

import android.graphics.DashPathEffect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
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
import hu.ait.minesweeper.ui.theme.accentBlueColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


@Composable
fun MinSweeperBoard (
    maxRow: Int,
    maxCol: Int,
    modifier: Modifier,
    board: List<BoardCell>,
    flaggedCoordinates: Set<CellDimension>,
    onCellClicked: (CellDimension) -> Unit,
    onCellLongPressed: (CellDimension) -> Unit
) {
    val borderStrokeWidth = 5.dp
    val mineColor = MaterialTheme.colorScheme.error
    val starColor = MaterialTheme.colorScheme.primary

    val gridColor = MaterialTheme.colorScheme.outline
    val accentBlueColor = accentBlueColor

    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(fontSize = 30.sp, color = MaterialTheme.colorScheme.onSurface)

    Canvas(
        modifier = modifier.fillMaxWidth(0.8f)
            .aspectRatio(1.0f)

            .pointerInput(key1 = Unit) {
                detectTapGestures (
                    onTap = { offset ->

                        val y = (offset.y / (size.height / maxCol)).toInt()
                        val x = (offset.x / (size.width / maxRow)).toInt()
                        Log.d("TAG_CLICKS", "$x $y")

                        onCellClicked(CellDimension(x, y))
                    },
                    onLongPress = {
                        offset ->

                        val y = (offset.y / (size.height / maxCol)).toInt()
                        val x = (offset.x / (size.width / maxRow)).toInt()

                        onCellLongPressed(CellDimension(x, y))
                    }
                )
            }
    ) {

        val gridSize = size.minDimension
        val cellRowSize = gridSize / maxRow
        val cellColSize = gridSize / maxCol
        val gap = 15.dp.toPx()

        val dashedEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(cellRowSize - gap, gap),
            phase = -gap / 2f
        )

        for (i in 1 until maxCol) {
            drawLine(
                color = gridColor,
                strokeWidth = borderStrokeWidth.toPx(),
                start = Offset(cellColSize * i, 0f),
                end = Offset(cellColSize * i, gridSize),
                pathEffect = dashedEffect,
                cap = StrokeCap.Round
            )
        }

        for (i in 1 until maxRow) {
            drawLine(
                color = gridColor,
                strokeWidth = borderStrokeWidth.toPx(),
                start = Offset(0f, cellRowSize * i),
                end = Offset(gridSize, cellRowSize * i),
                pathEffect = dashedEffect,
                cap = StrokeCap.Round
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
                    drawRoundRect(
                        color = accentBlueColor ,
                        topLeft = Offset(i * cellRowSize, j * cellColSize),
                        size = Size(cellRowSize, cellColSize),
                        cornerRadius = CornerRadius(
                            x = 12.dp.toPx(),
                            y = 12.dp.toPx()
                        )
                    )
                }
            }
        }

        //draw flagged coordinates
        for (cord in flaggedCoordinates) {
            val x = cord.row * cellRowSize + (cellRowSize / 2)
            val y = cord.col * cellColSize + (cellColSize / 2)
            starIcon(Offset(x, y), iconSize, 0.5f, 5, starColor)
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


fun DrawScope.starIcon (
    c: Offset,
    iconSize: Float,
    ratio: Float,
    n: Int,
    color: Color
) {
    val outerRadius = iconSize * ratio
    val innerRadius = iconSize * ratio * 0.5f
    val path = Path()
    for (k in 0 .. 2 * n) {
        val angle = -PI / 2 + k * PI / n
        val radius = if (k % 2 == 0) outerRadius else innerRadius

        val x = c.x + radius * cos(angle).toFloat()
        val y = c.y + radius * sin(angle).toFloat()
        if (k == 0) path.moveTo(x,y) else path.lineTo(x,y)
    }


    path.close()
    drawPath(path, color)
}