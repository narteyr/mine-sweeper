package hu.ait.minesweeper.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.ait.minesweeper.R
import hu.ait.minesweeper.screen.MineSweeperViewModel
import hu.ait.minesweeper.screen.mineIcon
import hu.ait.minesweeper.ui.theme.UnkemptFamily


@Composable
fun GameOverScreen(
    modifier: Modifier,
    viewModel: MineSweeperViewModel = viewModel())

{
    val mineColor = MaterialTheme.colorScheme.outline
    Column(
        modifier = modifier.fillMaxSize(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        if (!viewModel.hasWon) {
            Text(stringResource(R.string.game_over),
                fontFamily = UnkemptFamily,
                fontSize = 50.sp
            )
        } else {
            Text(stringResource(R.string.you_win),
                fontFamily = UnkemptFamily,
                fontSize = 50.sp
            )
        }

        Canvas(
            modifier = modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
        ) {
            val x= size.width / 2f
            val y = size.height / 2f

            mineIcon(c = Offset(x, y), size.minDimension * 0.5f, color = mineColor )
        }

        Button(
            onClick = viewModel::onStartGame
        ) {
            Text(stringResource(R.string.start_new_game)
            )
        }
    }
}