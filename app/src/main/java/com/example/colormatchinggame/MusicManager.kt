package com.example.colormatchinggame

import android.content.Context
<<<<<<< HEAD
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
=======
import android.media.MediaPlayer
>>>>>>> origin/master

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val songs = intArrayOf(
<<<<<<< HEAD
        R.raw.aventure_monday_mood,
        R.raw.berlin_dream_unminus,
        R.raw.migfus20_lofi_music_guitar,
        R.raw.tea_by_coldise_unminus,
    )
    private var currentSongIndex = 0 // Текущая песня
    private var isShuffle = true

    // Добавляем SoundPool для звуковых эффектов
    private lateinit var soundPool: SoundPool
    private var moveSoundId: Int = 0
    private var errorSoundId: Int = 0

    init {
        // Инициализация SoundPool с настройками для звуковых эффектов
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        // Загружаем звуковые файлы (добавьте ваши файлы в res/raw)
        moveSoundId = soundPool.load(context, R.raw.move_success, 1)
        errorSoundId = soundPool.load(context, R.raw.cannot_place, 1)
    }

    fun playMoveSound() {
        soundPool.play(moveSoundId, 0.7f, 0.7f, 0, 0, 1f)
    }

    fun playErrorSound() {
        soundPool.play(errorSoundId, 0.7f, 0.7f, 0, 0, 1f)
    }
=======
        R.raw.ethereal_reverie,
    )
    private var currentSongIndex = 0 // Текущая песня
>>>>>>> origin/master

    fun startMusic() {
        playNextSong()
    }

    private fun playNextSong() {
<<<<<<< HEAD
        mediaPlayer?.release()

        // Выбор следующей песни в зависимости от режима
        currentSongIndex = if (isShuffle) {
            (0 until songs.size).random() // Случайный выбор [[1]][[2]]
        } else {
            (currentSongIndex + 1) % songs.size // Последовательный режим
        }

        mediaPlayer = MediaPlayer.create(context, songs[currentSongIndex])
        mediaPlayer?.setOnCompletionListener {
            playNextSong() // Автоматический переход к следующей песне [[5]][[7]]
        }
        mediaPlayer?.isLooping = false // Отключите повторение отдельных треков
        mediaPlayer?.start()
=======
        mediaPlayer?.release() // Освобождаем ресурсы предыдущего MediaPlayer

        // Создаём новый MediaPlayer для текущей песни
        mediaPlayer = MediaPlayer.create(context, songs[currentSongIndex])
        mediaPlayer?.setOnCompletionListener {
            // Когда песня заканчивается, переходим к следующей
            currentSongIndex = (currentSongIndex + 1) % songs.size // Циклический переход
            playNextSong()
        }

        mediaPlayer?.start() // Начинаем воспроизведение
>>>>>>> origin/master
    }

    fun pauseMusic() {
        mediaPlayer?.pause() // Приостанавливаем воспроизведение
    }

    fun resumeMusic() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start() // Возобновляем воспроизведение
        }
    }

<<<<<<< HEAD
    fun setShuffle(enabled: Boolean) {
        isShuffle = enabled
    }

    fun releaseMusic() {
        mediaPlayer?.release()
        soundPool.release() // Освобождаем ресурсы SoundPool
=======
    fun releaseMusic() {
        mediaPlayer?.release() // Освобождаем ресурсы
>>>>>>> origin/master
        mediaPlayer = null
    }
}