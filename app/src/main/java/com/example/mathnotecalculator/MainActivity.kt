package com.example.mathnotecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mathnotecalculator.screen.HandwritingCanvas
import com.example.mathnotecalculator.ui.theme.MathNoteCalculatorTheme
import com.example.mathnotecalculator.viewmodel.MathNoteViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val mathNoteViewModel: MathNoteViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        setContent {
            MathNoteCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                 HandwritingCanvas(viewModel = mathNoteViewModel ) //Calling HandwritingCanvas
                }
            }
        }
    }
}

