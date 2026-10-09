package hu.ait.minesweeper.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun MineSweeperScreen(modifier: Modifier, viewModel: MineSweeperViewModel = viewModel()) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MinSweeperBoard(
            maxRow = viewModel.maxRow,
            maxCol  = viewModel.maxCol,
            modifier =  modifier,
            board = viewModel.board,
            onCellClicked = viewModel::onCellClicked
        )
    }
}