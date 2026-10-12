package hu.ait.minesweeper.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun MineSweeperScreen(modifier: Modifier, viewModel: MineSweeperViewModel = viewModel()) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val elapsedTime = viewModel.elapsedSeconds
        Text(
            text = "%02d:%02d".format(elapsedTime / 60, elapsedTime % 60),
            fontSize = 50.sp
        )

        MinSweeperBoard(
            maxRow = viewModel.maxRow,
            maxCol  = viewModel.maxCol,
            modifier =  modifier,
            board = viewModel.board,
            flaggedCoordinates = viewModel.flaggedCoordinates,
            onCellClicked = viewModel::onCellClicked,
            onCellLongPressed = viewModel::onCellLongPressed
        )

        Text(viewModel.status)

        Button(
            onClick =  viewModel::resetGame
        ) {
            Text("reset")
        }
    }
}