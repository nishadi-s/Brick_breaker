package com.example.game4

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var brickContainer: LinearLayout

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f

    private var paddleX = 0f

    private var score = 0
    private val brickRows = 9
    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4

    private var isBallLaunched = false
    private var lives = 3

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)

        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener {
            initializeBricks()
            startGame()
            newGameButton.isVisible = false
        }
    }

    private fun initializeBricks() {
        brickContainer.removeAllViews()

        for (row in 0 until brickRows) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until brickColumns) {
                val brick = View(this)
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams
                brick.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))
                rowLayout.addView(brick)
            }

            brickContainer.addView(rowLayout)
        }
    }

    private fun startGame() {
        resetBallPosition()
        movePaddle()

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            moveBall()
            checkCollision()
        }
        animator.start()
    }

    private fun resetBallPosition() {
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2

        ball.x = ballX
        ball.y = ballY

        ballSpeedX = 2 * screenDensity // Decreased ball speed
        ballSpeedY = -2 * screenDensity // Decreased ball speed

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX
    }

    private fun moveBall() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ball.x = ballX
        ball.y = ballY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun movePaddle() {
        paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    paddleX = event.rawX - paddle.width / 2
                    paddle.x = paddleX
                }
            }
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkCollision() {
        // Collision detection logic
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Collision with walls
        if (ballX <= 0 || ballX + ball.width >= screenWidth) {
            ballSpeedX *= -1
        }

        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        // Collision with paddle
        if (ballY + ball.height >= paddle.y && ballY + ball.height <= paddle.y + paddle.height
            && ballX + ball.width >= paddle.x && ballX <= paddle.x + paddle.width
        ) {
            ballSpeedY *= -1
        }

        // Collision with bricks
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.isVisible) {
                    // Calculate brick boundaries
                    val brickLeft = brick.x
                    val brickRight = brick.x + brick.width
                    val brickTop = brick.y
                    val brickBottom = brick.y + brick.height

                    // Check collision
                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        // Break the brick
                        brick.isVisible = false
                        score++
                        scoreText.text = "Score: $score"

                        // Adjust ball direction based on collision
                        val ballCenterX = ballX + ball.width / 2
                        val ballCenterY = ballY + ball.height / 2

                        val brickCenterX = brickLeft + brick.width / 2
                        val brickCenterY = brickTop + brick.height / 2

                        val distX = (ballCenterX - brickCenterX) / (brick.width / 2)
                        val distY = (ballCenterY - brickCenterY) / (brick.height / 2)

                        if (Math.abs(distX) > Math.abs(distY)) {
                            ballSpeedX *= -1
                        } else {
                            ballSpeedY *= -1
                        }

                        // Exit the function after breaking the brick
                        return
                    }
                }
            }
        }

        // Collision with bottom wall
        if (ballY + ball.height >= screenHeight) {
            resetBallPosition()
            lives--
            if (lives > 0) {
                Toast.makeText(this, "Lives left: $lives", Toast.LENGTH_SHORT).show()
            } else {
                gameOver()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun gameOver() {
        // Game over logic
        scoreText.text = "Game Over"

        // Save high score
        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val highScore = prefs.getInt("highScore", 0)
        if (score > highScore) {
            val editor = prefs.edit()
            editor.putInt("highScore", score)
            editor.apply()
        }

        // Start a new game
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
    }
}
