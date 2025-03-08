package com.example.colormatchinggame

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val numColumns = 15         // Количество столбцов
    private val numBlocks = 90          // Количество блоков
    private val maxBlocksPerColumn = 10 // Максимальное количество блоков в колонке
    private val blocksToFillPerColumn = maxBlocksPerColumn - 2 // Оставляем 2 пустых места

    // Отступы для регулировки
    private val blockMargin = 8         // Отступы между блоками
    private val columnSpacing = 10      // Отступы между столбцами
    private val platformHeight = 20     // Высота подставки (платформы)

    private lateinit var columns: List<LinearLayout>
    private val blockColors: List<Int> = generateColorsForColumns(numColumns)
    private var isGameStarted = false  // Флаг начала игры
    private lateinit var timerTextView: TextView
    private var gameTimer: CountDownTimer? = null
    private var elapsedTime = 0L  // Время с начала игры

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация таймера
        timerTextView = findViewById(R.id.timerTextView)

        setupColumns()
        addBlocksToColumns()
    }

    private fun setupColumns() {
        val columnContainer = findViewById<LinearLayout>(R.id.columnContainer)
        if (columnContainer != null) {
            columns = List(numColumns) {
                val frameLayout = FrameLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    ).apply {
                        setMargins(columnSpacing, 0, columnSpacing, 0)
                    }
                }

                // Колонка с блоками
                val columnLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = android.view.Gravity.BOTTOM
                    setPadding(0, 0, 0, platformHeight)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }

                // Подставка (платформа)
                val basePlatform = View(this).apply {
                    setBackgroundColor(Color.DKGRAY)
                    layoutParams = FrameLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        platformHeight,
                        android.view.Gravity.BOTTOM
                    )
                }

                // Линия-ориентир
                val rodView = View(this).apply {
                    setBackgroundColor(Color.DKGRAY)
                    alpha = 0.5f
                    val rodHeight = calculateRodHeight()
                    layoutParams = FrameLayout.LayoutParams(
                        12,  // Ширина линии
                        rodHeight,  // Высота линии
                        android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                    )
                }

                // Добавляем подставку и линию в FrameLayout
                frameLayout.addView(basePlatform)
                frameLayout.addView(rodView)
                frameLayout.addView(columnLayout)
                columnContainer.addView(frameLayout)
                setupDragListener(columnLayout)

                columnLayout
            }
        } else {
            Toast.makeText(this, "Не удалось найти контейнер для колонок.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateRodHeight(): Int {
        // Получаем высоту блока
        val blockHeight = calculateBlockHeight()
        // Учитываем отступы между блоками при расчете высоты линии
        val totalBlockHeight = blockHeight * (maxBlocksPerColumn + 1)
        val totalMarginHeight = blockMargin * (maxBlocksPerColumn + 1)
        val maxRodHeight = totalBlockHeight + totalMarginHeight
        return maxRodHeight
    }

    private fun generateColorsForColumns(numColumns: Int): List<Int> {
        // Обновленный список с 20 яркими и различимыми цветами
        val availableColors = listOf(
            Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN,
            Color.MAGENTA, Color.CYAN, Color.BLACK, Color.GRAY,
            Color.DKGRAY, Color.LTGRAY, Color.WHITE, Color.parseColor("#FF6347"), // Tomato
            Color.parseColor("#FFD700"), // Gold
            Color.parseColor("#FF1493"), // DeepPink
            Color.parseColor("#FF4500"), // OrangeRed
            Color.parseColor("#8A2BE2"), // BlueViolet
            Color.parseColor("#A52A2A"), // Brown
            Color.parseColor("#7FFF00"), // Chartreuse
            Color.parseColor("#FF69B4"), // HotPink
            Color.parseColor("#F0E68C"), // Khaki
            Color.parseColor("#D2691E"), // Chocolate
            Color.parseColor("#9932CC"), // DarkOrchid
            Color.parseColor("#FF8C00"), // DarkOrange
            Color.parseColor("#00CED1"), // DarkTurquoise
            Color.parseColor("#8B0000")  // DarkRed
        )
        // Возвращаем случайный порядок цветов, который не превышает количество столбцов
        return availableColors.shuffled().take(numColumns)
    }




    @SuppressLint("ClickableViewAccessibility")
    private fun addBlocksToColumns() {
        val blocks = createBlocks().shuffled()
        var index = 0

        columns.forEach { column ->
            repeat(minOf(blocksToFillPerColumn, blocks.size - index)) {
                addBlockToColumn(column, blocks[index++])
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createBlocks(): List<Block> {
        return List(numBlocks) { Block(blockColors[it % numColumns], it + 1) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addBlockToColumn(column: LinearLayout, block: Block) {
        if (column.childCount >= blocksToFillPerColumn) return

        val blockView = View(this).apply {
            setBackgroundColor(block.color)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, calculateBlockHeight()
            ).apply { setMargins(blockMargin, blockMargin, blockMargin, blockMargin) }

            tag = block.id
            setOnTouchListener { view, motionEvent ->
                val parent = view.parent as? LinearLayout ?: return@setOnTouchListener false
                if (parent.getChildAt(0) != view) return@setOnTouchListener false

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    val dragShadow = View.DragShadowBuilder(view)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        view.startDragAndDrop(null, dragShadow, view, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        view.startDrag(null, dragShadow, view, 0)
                    }

                    // Если это первый перетаскиваемый блок, запускаем таймер
                    if (!isGameStarted) {
                        startGameTimer()
                    }
                    true
                } else {
                    false
                }
            }
        }
        column.addView(blockView, 0)
    }

    private fun calculateBlockHeight(): Int {
        val screenHeight = resources.displayMetrics.heightPixels
        val blockHeight = (screenHeight / (maxBlocksPerColumn - 2) / 3).coerceIn(100, 200)
        return blockHeight
    }

    private fun setupDragListener(column: LinearLayout) {
        column.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedView = dragEvent.localState as? View ?: return@setOnDragListener false
                    val parent = draggedView.parent as? LinearLayout ?: return@setOnDragListener false

                    parent.removeView(draggedView)
                    if (column.childCount < maxBlocksPerColumn) {
                        column.addView(draggedView, 0)
                    } else {
                        parent.addView(draggedView, 0)
                        Toast.makeText(this, "Нет места в колонке", Toast.LENGTH_SHORT).show()
                    }
                    checkWinCondition()
                    true
                }
                else -> true
            }
        }
    }

    private fun checkWinCondition() {
        if (columns.all { isColumnWin(it) }) {
            showWinDialog()
            gameTimer?.cancel()  // Останавливаем таймер после победы
        }
    }

    private fun isColumnWin(column: LinearLayout): Boolean {
        val firstBlockColor = (column.getChildAt(0) as? View)?.background?.let {
            (it as? android.graphics.drawable.ColorDrawable)?.color
        }
        return (0 until column.childCount).all { i ->
            val child = column.getChildAt(i)
            val blockColor = (child.background as? android.graphics.drawable.ColorDrawable)?.color
            blockColor == firstBlockColor
        }
    }

    private fun startGameTimer() {
        isGameStarted = true
        elapsedTime = 0L

        gameTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime++
                val minutes = (elapsedTime / 60).toInt()
                val seconds = (elapsedTime % 60).toInt()
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                // Здесь не будет вызвано, так как таймер работает бесконечно
            }
        }.start()
    }

    private fun showWinDialog() {
        val minutes = (elapsedTime / 60).toInt()
        val seconds = (elapsedTime % 60).toInt()
        AlertDialog.Builder(this)
            .setTitle("Ты победил!")
            .setMessage("Поздравляем, все блоки на местах! Время: ${String.format("%02d:%02d", minutes, seconds)}")
            .setPositiveButton("ОК") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
