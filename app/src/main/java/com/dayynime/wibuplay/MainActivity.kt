package com.dayynime.wibuplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dayynime.wibuplay.ui.navigation.AppNavigation
import com.dayynime.wibuplay.ui.theme.BackgroundDark
import com.dayynime.wibuplay.ui.theme.WibuplayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WibuplayApp
        val repository = app.repository

        setContent {
            WibuplayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AppNavigation(repository = repository)
                }
            }
        }
    }
}
