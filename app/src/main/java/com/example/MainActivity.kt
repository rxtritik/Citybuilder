package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.CityBuilderScreen
import com.example.ui.CityGameViewModel
import com.example.ui.theme.MetroForgeTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: CityGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetroForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CityBuilderScreen(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Ensure game is saved whenever player switches apps or screen turns off
        gameViewModel.triggerAutoSave()
    }
}
