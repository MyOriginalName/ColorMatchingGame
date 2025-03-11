package com.example.colormatchinggame

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {

    private val numColumns = 15        // Количество столбцов
    private val numBlocks = 180          // Количество блоков
    private val maxBlocksPerColumn = 10 // Максимальное количество блоков в колонке
    private val blocksToFillPerColumn = maxBlocksPerColumn - 2 // Оставляем 2 пустых места

    // Отступы для регулировки
    private val blockMargin = -8        // Отступы между блоками
    private val columnSpacing = 13      // Отступы между столбцами
    private val platformHeight = 55     // Высота подставки (платформы)

    private lateinit var columns: List<LinearLayout>
    private val blockColors: List<Int> = generateColorsForColumns(numColumns)
    private var isGameStarted = false  // Флаг начала игры
    private lateinit var timerTextView: TextView
    private var gameTimer: CountDownTimer? = null
    private var elapsedTime = 0L  // Время с начала игры

    private val blockDrawableCache = mutableMapOf<Int, Drawable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация таймера
        timerTextView = findViewById(R.id.timerTextView)

        setupColumns()
        addBlocksToColumns()
    }

    private fun getBlockDrawable(color: Int): Drawable {
        return blockDrawableCache.getOrPut(color) {
            resources.getDrawable(R.drawable.block, null).apply {
                colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
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
        val totalBlockHeight = blockHeight * (maxBlocksPerColumn)
        val totalMarginHeight = blockMargin * (maxBlocksPerColumn + 1)
        val maxRodHeight = totalBlockHeight + totalMarginHeight
        return maxRodHeight
    }

    private fun generateColorsForColumns(numColumns: Int): List<Int> {
        val availableColors = listOf(
            Color.parseColor("#FFB3BA"), // Soft Coral
            Color.parseColor("#FFD8A8"), // Peach
            Color.parseColor("#B0E57C"), // Light Lime
            Color.parseColor("#87CEEB"), // Sky Blue
            Color.parseColor("#DDA0DD"), // Pale Violet
            Color.parseColor("#9ACD32"), // Gentle Olive
            Color.parseColor("#E6E6FA"), // Lavender
            Color.parseColor("#AFEEEE"), // Mint
            Color.parseColor("#F5DEB3"), // Wheat
            Color.parseColor("#D8BFD8"), // Thistle
            Color.parseColor("#FFC0CB"), // Baby Pink
            Color.parseColor("#98FB98"), // Pale Green
            Color.parseColor("#B0C4DE"), // Light Steel Blue
            Color.parseColor("#F0E68C"), // Khaki (более мягкий)
            Color.parseColor("#EEDD82"), // Light Goldenrod
            Color.parseColor("#CDB7F6"), // Periwinkle
            Color.parseColor("#FFA07A"), // Light Salmon
            Color.parseColor("#C7CEEA"), // Powder Blue
            Color.parseColor("#F4A460"), // Sandy Brown
            Color.parseColor("#D1E189"), // Pale Chartreuse
            Color.parseColor("#C8A2C8"), // Lilac
            Color.parseColor("#A2D9CE"), // Tiffany Blue
            Color.parseColor("#E5B3BB"), // Dusty Rose
            Color.parseColor("#B5EAD7")  // Celadon
        )

        return availableColors
            .shuffled()
            .distinct() // Дополнительная проверка на уникальность
            .take(numColumns)
    }

    private val scope = MainScope()

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addBlocksToColumns() {
        scope.launch(Dispatchers.Default) {
            val blocks = createBlocks().shuffled()
            var index = 0

            columns.forEach { column ->
                repeat(minOf(blocksToFillPerColumn, blocks.size - index)) {
                    withContext(Dispatchers.Main) {
                        addBlockToColumn(column, blocks[index++])
                    }
                }
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

        val blockView = ImageView(this).apply {
            // Устанавливаем векторный drawable
            setImageDrawable(getBlockDrawable(block.color))

            // Применяем цвет к внутренней части
            val drawable = drawable.mutate() as? VectorDrawable
            drawable?.colorFilter = PorterDuffColorFilter(block.color, PorterDuff.Mode.SRC_IN)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                calculateBlockHeight()
            ).apply {
                setMargins(blockMargin, blockMargin, blockMargin, blockMargin)
            }

            // Сохраняем цвет в теге
            tag = block.color

            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER

            setOnTouchListener { view, motionEvent ->
                val parent = view.parent as? LinearLayout ?: return@setOnTouchListener false
                if (parent.getChildAt(0) != view) return@setOnTouchListener false

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val dragShadow = View.DragShadowBuilder(view)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            view.startDragAndDrop(null, dragShadow, view, 0)
                        } else {
                            @Suppress("DEPRECATION")
                            view.startDrag(null, dragShadow, view, 0)
                        }

                        if (!isGameStarted) {
                            startGameTimer()
                        }
                        true
                    }
                    else -> false
                }
            }
        }
        column.addView(blockView, 0)
    }

    private fun calculateBlockHeight(): Int {
        val screenHeight = resources.displayMetrics.heightPixels
        return (screenHeight / (maxBlocksPerColumn - 2) / 3).coerceIn(50, 200)
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
        var allWin = true
        // Параллельная проверка колонок
        columns.forEach { column ->
            if (!isColumnWin(column)) {
                allWin = false
                return@forEach
            }
        }
        if (allWin) showWinDialog()
    }

    private fun isColumnWin(column: LinearLayout): Boolean {
        if (column.childCount == 0) return false
        val firstColor = (column.getChildAt(0).tag as? Int) ?: return false
        // Быстрая проверка через все элементы
        for (i in 1 until column.childCount) {
            if ((column.getChildAt(i).tag as? Int) != firstColor) return false
        }
        return true
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
