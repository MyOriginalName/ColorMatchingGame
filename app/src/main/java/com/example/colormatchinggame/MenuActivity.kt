package com.example.colormatchinggame

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MenuActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private val TAG = "MenuActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_menu)

            // Настройка анимации фона
            setupBackgroundAnimation()

            // Настройка музыки
            setupMusic()

            // Настройка кнопок
            setupButtons()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Произошла ошибка при загрузке меню", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBackgroundAnimation() {
        try {
            val backgroundImage = findViewById<ImageView>(R.id.backgroundImage)
            val floatAnimation = AnimationUtils.loadAnimation(this, R.anim.float_animation)
            backgroundImage.startAnimation(floatAnimation)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up background animation", e)
        }
    }

    private fun setupMusic() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.aventure_monday_mood)
            mediaPlayer?.apply {
                isLooping = true
                setVolume(0.5f, 0.5f) // Уменьшаем громкость
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up music", e)
            Toast.makeText(this, "Не удалось загрузить музыку", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        try {
            findViewById<Button>(R.id.easyButton).setOnClickListener {
                startGame(Difficulty.EASY)
            }

            findViewById<Button>(R.id.mediumButton).setOnClickListener {
                startGame(Difficulty.MEDIUM)
            }

            findViewById<Button>(R.id.hardButton).setOnClickListener {
                startGame(Difficulty.HARD)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up buttons", e)
            Toast.makeText(this, "Ошибка при настройке кнопок", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGame(difficulty: Difficulty) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("difficulty", difficulty.name)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game", e)
            Toast.makeText(this, "Ошибка при запуске игры", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
}

enum class Difficulty {
    EASY, MEDIUM, HARD
} 