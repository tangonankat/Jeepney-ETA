package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.main.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JeepneyViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: JeepneyViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        MainAppScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
      }
    }
  }
}
