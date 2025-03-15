package com.example.colormatchinggame

import android.content.Context
import android.media.MediaPlayer

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val songs = intArrayOf(
        R.raw.ethereal_reverie,
    )
    private var currentSongIndex = 0 // Текущая песня

    fun startMusic() {
        playNextSong()
    }

    private fun playNextSong() {
        mediaPlayer?.release() // Освобождаем ресурсы предыдущего MediaPlayer

        // Создаём новый MediaPlayer для текущей песни
        mediaPlayer = MediaPlayer.create(context, songs[currentSongIndex])
        mediaPlayer?.setOnCompletionListener {
            // Когда песня заканчивается, переходим к следующей
            currentSongIndex = (currentSongIndex + 1) % songs.size // Циклический переход
            playNextSong()
        }

        mediaPlayer?.start() // Начинаем воспроизведение
    }

    fun pauseMusic() {
        mediaPlayer?.pause() // Приостанавливаем воспроизведение
    }

    fun resumeMusic() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start() // Возобновляем воспроизведение
        }
    }

    fun releaseMusic() {
        mediaPlayer?.release() // Освобождаем ресурсы
        mediaPlayer = null
    }
}