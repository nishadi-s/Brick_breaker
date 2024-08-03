//GameViewModel.kt
package com.example.game4
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    var score = 0
    var lives = 3
    var isGameRunning = false
}
