// GameOverActivity.kt
package com.example.game4

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // Retrieve current score from intent
        val score = intent.getIntExtra("score", 0)

        // Display current score
        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        scoreTextView.text = "Your Score: $score"

        // Retrieve previous high score from SharedPreferences
        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val previousHighScore = prefs.getInt("highScore", 0)

        // Display previous high score
        val highScoreTextView = findViewById<TextView>(R.id.highScoreTextView)
        highScoreTextView.text = "High Score: $previousHighScore"

        // Update high score if current score is higher
        if (score > previousHighScore) {
            val editor = prefs.edit()
            editor.putInt("highScore", score)
            editor.apply()
            highScoreTextView.text = "New High Score: $score"
        }

        // Set up new game button to return to MainActivity
        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
