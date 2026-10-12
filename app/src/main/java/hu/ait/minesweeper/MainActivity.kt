package hu.ait.minesweeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.ait.minesweeper.game.GameOverScreen
import hu.ait.minesweeper.screen.GameState
import hu.ait.minesweeper.screen.MineSweeperScreen
import hu.ait.minesweeper.screen.MineSweeperViewModel
import hu.ait.minesweeper.ui.theme.MineSweeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MineSweeperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameStateScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}



@Composable
fun GameStateScreen(modifier: Modifier, viewModel: MineSweeperViewModel = viewModel()) {
    when(viewModel.gameState) {
        GameState.GAME_STARTED -> MineSweeperScreen(modifier)
        GameState.GAME_OVER -> GameOverScreen(modifier)
    }
}