package com.example.flappynext.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.example.flappynext.audio.SoundManager
import com.example.flappynext.player.PlayerCharacter
import com.example.flappynext.ui.screens.GameContext
import com.example.flappynext.ui.screens.GameScreen
import com.example.flappynext.ui.screens.MenuScreen
import com.example.flappynext.world.Background
import com.example.flappynext.ui.screens.PlayingScreen
import com.example.flappynext.world.WeatherManager

class GameView(context: Context) : View(context), GameContext {

    override val preferences = context.getSharedPreferences("flappynext_stats", Context.MODE_PRIVATE)
    override val soundManager = SoundManager()
    override val screenWidth: Int
        get() = width.coerceAtLeast(1)

    override val screenHeight: Int
        get() = height.coerceAtLeast(1)

    override val coinManager = CoinManager(preferences)
    override val levelManager = LevelManager(preferences)
    override val skinManager = SkinManager(preferences, coinManager)
    override val accessoryManager = AccessoryManager(preferences, coinManager)
    override val questManager = QuestManager(preferences, coinManager)
    override val dailyQuestManager = DailyQuestManager(preferences, coinManager)
    override val dailyRewardManager = DailyRewardManager(preferences, coinManager)
    override val achievementManager = AchievementManager(preferences, coinManager)

    override lateinit var background: Background

    override var bestScore: Int = preferences.getInt("best_score", 0)
    override var selectedCharacterIndex: Int = 0
    override var selectedCharacter: PlayerCharacter = PlayerCharacter.CLASSIC

    private var currentScreen: GameScreen? = null
    private var gameLoop: GameLoop? = null
    private var levelUpMessage: String? = null

    // Вспомогательные переменные для всплывающего сообщения об уровне
    private val levelUpRect = RectF()
    private val levelUpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val levelUpBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.rgb(255, 195, 60)
    }
    private val levelUpTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    override var weatherManager = WeatherManager(1080, 1920)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        // Вот эта строка передаст реальные размеры экрана в погоду:
        weatherManager = WeatherManager(w, h)

        gameLoop?.stopLoop()

        background = Background(w, h)
        loadSelectedCharacter()

        // Твой исходный вызов меню:
        currentScreen = MenuScreen(this, onStartGame = { startPlaying() })
        currentScreen?.onResize(w, h)

        gameLoop = GameLoop(
            update = { currentScreen?.update() },
            render = { postInvalidate() }
        )
        gameLoop?.startLoop()
    }

    override fun startPlaying() {
        switchScreen(PlayingScreen(this))
    }

    override fun switchScreen(screen: GameScreen) {
        currentScreen = screen
        if (width > 0 && height > 0) {
            currentScreen?.onResize(width, height)
        }
        invalidate()
    }

    override fun addPlayerXp(amount: Int) {
        val gainedLevels = levelManager.addXp(amount)
        if (gainedLevels > 0) {
            val coinsReward = gainedLevels * 25
            coinManager.addCoins(coinsReward)
            showLevelUpMessage("НОВЫЙ УРОВЕНЬ! УРОВЕНЬ ${levelManager.level}\n+${coinsReward} 🪙")
        }
    }

    override fun showLevelUpMessage(message: String) {
        levelUpMessage = message
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentScreen?.draw(canvas)

        // Отрисовка поздравления с уровнем поверх любого экрана
        levelUpMessage?.let { msg ->
            drawLevelUpDialog(canvas, msg)
        }
    }

    private fun drawLevelUpDialog(canvas: Canvas, message: String) {
        val cx = width / 2f
        val cy = height / 2f
        val cardW = width * 0.82f
        val cardH = 190f
        levelUpRect.set(cx - cardW / 2f, cy - cardH / 2f, cx + cardW / 2f, cy + cardH / 2f)

        levelUpPaint.color = Color.rgb(25, 35, 60)
        canvas.drawRoundRect(levelUpRect, 24f, 24f, levelUpPaint)
        canvas.drawRoundRect(levelUpRect, 24f, 24f, levelUpBorderPaint)

        levelUpTextPaint.color = Color.rgb(255, 210, 80)
        levelUpTextPaint.textSize = 30f
        canvas.drawText("🏆", cx, cy - 35f, levelUpTextPaint)

        levelUpTextPaint.textSize = 25f
        canvas.drawText(message, cx, cy + 10f, levelUpTextPaint)

        levelUpTextPaint.textSize = 19f
        levelUpTextPaint.color = Color.WHITE
        canvas.drawText("Ты стал сильнее!", cx, cy + 50f, levelUpTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true

        if (levelUpMessage != null) {
            levelUpMessage = null
            invalidate()
            return true
        }

        return currentScreen?.onTouch(event.x, event.y) ?: true
    }

    override fun onDetachedFromWindow() {
        gameLoop?.stopLoop()
        gameLoop = null
        soundManager.release()
        super.onDetachedFromWindow()
    }

    private fun loadSelectedCharacter() {
        val savedId = preferences.getString("selected_character", PlayerCharacter.CLASSIC.id)
        val allChars = listOf(
            PlayerCharacter.CLASSIC, PlayerCharacter.BLUE, PlayerCharacter.RED,
            PlayerCharacter.FIRE, PlayerCharacter.DIAMOND, PlayerCharacter.ROYAL
        )
        selectedCharacterIndex = allChars.indexOfFirst { it.id == savedId }.coerceAtLeast(0)
        selectedCharacter = allChars[selectedCharacterIndex]
    }
}