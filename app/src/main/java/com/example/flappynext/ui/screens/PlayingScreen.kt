package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.edit
import com.example.flappynext.game.CollectibleCoin
import com.example.flappynext.game.PowerUp
import com.example.flappynext.game.PowerUpType
import com.example.flappynext.obstacles.Obstacle
import com.example.flappynext.obstacles.ObstacleGenerator
import com.example.flappynext.player.Player
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

class PlayingScreen(
    context: GameContext
) : BaseScreen(context) {

    private var screenWidth: Int = context.screenWidth
    private var screenHeight: Int = context.screenHeight

    private val player = Player(screenWidth, screenHeight)

    private val obstacleGenerator = ObstacleGenerator(screenWidth, screenHeight)

    private val listLock = Any()
    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<CollectibleCoin>()
    private val powerUps = mutableListOf<PowerUp>()

    private var score: Int = 0
    private var isGameOver: Boolean = false

    private var shieldActive: Boolean = false
    private var invulnerableTimer: Int = 0
    private var slowMoTimer: Int = 0
    private var magnetTimer: Int = 0

    private val gamePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 76f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(8f, 0f, 4f, Color.argb(170, 0, 0, 0))
    }

    private val powerUpIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 230, 80)
        textSize = 32f
        textAlign = Paint.Align.LEFT
        isFakeBoldText = true
        setShadowLayer(6f, 0f, 3f, Color.argb(180, 0, 0, 0))
    }

    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(235, 30, 32, 45)
    }

    private val panelBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.rgb(240, 180, 50)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 80, 80)
        textSize = 68f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val dialogTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 38f
        textAlign = Paint.Align.CENTER
    }

    private val dialogButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val restartButtonRect = RectF()
    private val menuButtonRect = RectF()
    private val panelRect = RectF()

    init {
        loadPlayerCustomization()
        initGame()
    }

    private fun loadPlayerCustomization() {
        // Читаем выбранного персонажа/скина
        val charId = context.preferences.getString("selected_character_id", null)
            ?: context.preferences.getString("selected_skin", "classic")
            ?: "classic"

        player.applyCharacterById(charId)

        // Читаем надетый аксессуар для этого персонажа из SharedPreferences
        val equippedAccessory = context.preferences.getString("equipped_accessory_$charId", "")
            ?: context.preferences.getString("equipped_accessory", "")
            ?: ""
        player.currentAccessoryId = equippedAccessory
    }

    private fun initGame() {
        score = 0
        isGameOver = false

        shieldActive = false
        invulnerableTimer = 0
        slowMoTimer = 0
        magnetTimer = 0

        loadPlayerCustomization()
        player.reset(screenWidth * 0.28f, screenHeight * 0.45f)
        obstacleGenerator.reset()

        synchronized(listLock) {
            obstacles.clear()
            coins.clear()
            powerUps.clear()

            val firstObs = obstacleGenerator.createObstacle(
                currentSpeed = 5.2f,
                score = 0,
                biomeIndex = context.background.biomeIndex
            )
            obstacles.add(firstObs)
        }
    }

    override fun onResize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    override fun update() {
        if (isGameOver) return

        if (slowMoTimer > 0) slowMoTimer--
        if (magnetTimer > 0) magnetTimer--
        if (invulnerableTimer > 0) invulnerableTimer--

        val timeScale = if (slowMoTimer > 0) 0.58f else 1.0f

        val speedProgression = ln((score + 6).toDouble()).toFloat() * 1.15f
        val windMod = context.weatherManager.windSpeedModifier * 0.4f
        val baseSpeed = (3.4f + speedProgression + windMod).coerceIn(5.2f, 8.3f)
        val currentSpeed = baseSpeed * timeScale

        context.background.update(currentSpeed, isPlaying = true, currentScore = score)
        context.weatherManager.update(score, context.background.groundTop, context.background.nightFactor)

        player.hasShieldVisual = shieldActive || invulnerableTimer > 0
        player.isBlinking = invulnerableTimer > 0 && (invulnerableTimer % 6 < 3)
        player.update(context.background.groundTop, timeScale)

        if (player.y + player.collisionRadius >= context.background.groundTop) {
            triggerGameOver()
            return
        }

        synchronized(listLock) {
            val iterator = obstacles.iterator()
            while (iterator.hasNext()) {
                val obs = iterator.next()
                obs.setSpeed(currentSpeed)
                obs.update()

                if (obs.checkPassed(player.x)) {
                    score++
                    context.coinManager.addCoins(1)
                    playScoreSoundSafe()

                    if (score > context.bestScore) {
                        context.bestScore = score
                        context.preferences.edit { putInt("best_score", score) }
                    }
                }

                if (obs.collidesWithCircle(player.x, player.y, player.collisionRadius)) {
                    if (shieldActive) {
                        shieldActive = false
                        invulnerableTimer = 70
                        playPowerUpSoundSafe()
                    } else if (invulnerableTimer <= 0) {
                        triggerGameOver()
                        return
                    }
                }

                if (obs.x + obs.width < -50f) {
                    iterator.remove()
                }
            }

            val lastObstacle = obstacles.lastOrNull()
            val spacing = obstacleGenerator.getHorizontalSpacing(currentSpeed)
            if (lastObstacle == null || (screenWidth.toFloat() + 50f - lastObstacle.x) >= spacing) {
                val newObs = obstacleGenerator.createObstacle(
                    currentSpeed = currentSpeed,
                    score = score,
                    biomeIndex = context.background.biomeIndex
                )
                obstacles.add(newObs)

                val coinSpawnX = newObs.x + newObs.width / 2f
                obstacleGenerator.maybeCreateCoin(newObs.gapY, coinSpawnX)?.let { coin ->
                    coins.add(coin)
                }

                obstacleGenerator.maybeCreatePowerUp(newObs.gapY, coinSpawnX)?.let { powerUp ->
                    powerUps.add(powerUp)
                }
            }

            val coinIterator = coins.iterator()
            while (coinIterator.hasNext()) {
                val coin = coinIterator.next()
                coin.update(currentSpeed)

                if (magnetTimer > 0) {
                    val dx = player.x - coin.x
                    val dy = player.y - coin.y
                    val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    if (dist < 420f && dist > 1f) {
                        val magnetPull = 12f * (1f - dist / 420f) + 4f
                        coin.x += (dx / dist) * magnetPull
                        coin.y += (dy / dist) * magnetPull
                    }
                }

                val dx = coin.x - player.x
                val dy = coin.y - player.y
                val hitDistance = player.collisionRadius + 24f
                if ((dx * dx + dy * dy) <= (hitDistance * hitDistance)) {
                    context.coinManager.addCoins(1)
                    playCoinSoundSafe()
                    coinIterator.remove()
                } else if (coin.x < -60f) {
                    coinIterator.remove()
                }
            }

            val powerUpIterator = powerUps.iterator()
            while (powerUpIterator.hasNext()) {
                val pUp = powerUpIterator.next()
                pUp.update(currentSpeed)

                val dx = pUp.x - player.x
                val dy = pUp.y - player.y
                val hitDistance = player.collisionRadius + 28f
                if ((dx * dx + dy * dy) <= (hitDistance * hitDistance)) {
                    playPowerUpSoundSafe()

                    when (pUp.type) {
                        PowerUpType.SHIELD -> {
                            shieldActive = true
                        }
                        PowerUpType.SLOW_MO -> {
                            slowMoTimer = 360
                        }
                        PowerUpType.MAGNET -> {
                            magnetTimer = 480
                        }
                        else -> {}
                    }

                    powerUpIterator.remove()
                } else if (pUp.x < -60f) {
                    powerUpIterator.remove()
                }
            }
        }
    }

    private fun triggerGameOver() {
        isGameOver = true
        playHitSoundSafe()
        context.addPlayerXp(max(1, score * 2))
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)

        val currentObstacles: List<Obstacle>
        val currentCoins: List<CollectibleCoin>
        val currentPowerUps: List<PowerUp>

        synchronized(listLock) {
            currentObstacles = obstacles.toList()
            currentCoins = coins.toList()
            currentPowerUps = powerUps.toList()
        }

        currentObstacles.forEach { it.draw(canvas) }
        currentCoins.forEach { it.draw(canvas, gamePaint) }
        currentPowerUps.forEach { it.draw(canvas, gamePaint) }

        player.draw(canvas)

        context.weatherManager.draw(canvas, context.background.groundTop, context.background.nightFactor)

        if (!isGameOver) {
            canvas.drawText("$score", screenWidth / 2f, screenHeight * 0.12f, scorePaint)

            var indicatorY = screenHeight * 0.08f
            if (shieldActive) {
                powerUpIndicatorPaint.color = Color.rgb(80, 215, 255)
                canvas.drawText("🛡 ЩИТ АКТИВЕН", 40f, indicatorY, powerUpIndicatorPaint)
                indicatorY += 40f
            }
            if (slowMoTimer > 0) {
                powerUpIndicatorPaint.color = Color.rgb(180, 120, 255)
                val sec = (slowMoTimer / 60) + 1
                canvas.drawText("⏳ ЗАМЕДЛЕНИЕ: ${sec}с", 40f, indicatorY, powerUpIndicatorPaint)
                indicatorY += 40f
            }
            if (magnetTimer > 0) {
                powerUpIndicatorPaint.color = Color.rgb(255, 195, 60)
                val sec = (magnetTimer / 60) + 1
                canvas.drawText("🧲 МАГНИТ: ${sec}с", 40f, indicatorY, powerUpIndicatorPaint)
            }
        } else {
            drawGameOverOverlay(canvas)
        }
    }

    private fun drawGameOverOverlay(canvas: Canvas) {
        canvas.drawColor(Color.argb(160, 0, 0, 0))

        val pWidth = screenWidth * 0.82f
        val pHeight = 440f
        val left = (screenWidth - pWidth) / 2f
        val top = screenHeight * 0.32f
        panelRect.set(left, top, left + pWidth, top + pHeight)

        canvas.drawRoundRect(panelRect, 28f, 28f, panelPaint)
        canvas.drawRoundRect(panelRect, 28f, 28f, panelBorderPaint)

        val cx = screenWidth / 2f
        canvas.drawText("ИГРА ОКОНЧЕНА", cx, top + 80f, titlePaint)
        canvas.drawText("Счет: $score", cx, top + 150f, dialogTextPaint)
        canvas.drawText("Рекорд: ${context.bestScore}", cx, top + 210f, dialogTextPaint)

        val btnW = pWidth * 0.42f
        val btnH = 80f
        val btnY = top + 290f

        restartButtonRect.set(cx - btnW - 14f, btnY, cx - 14f, btnY + btnH)
        dialogButtonPaint.color = Color.rgb(46, 175, 80)
        canvas.drawRoundRect(restartButtonRect, 18f, 18f, dialogButtonPaint)
        canvas.drawText("ЗАНОВО", restartButtonRect.centerX(), btnY + 52f, buttonTextPaint)

        menuButtonRect.set(cx + 14f, btnY, cx + btnW + 14f, btnY + btnH)
        dialogButtonPaint.color = Color.rgb(55, 120, 215)
        canvas.drawRoundRect(menuButtonRect, 18f, 18f, dialogButtonPaint)
        canvas.drawText("В МЕНЮ", menuButtonRect.centerX(), btnY + 52f, buttonTextPaint)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if (isGameOver) {
            if (restartButtonRect.contains(x, y)) {
                initGame()
                return true
            }
            if (menuButtonRect.contains(x, y)) {
                context.switchScreen(MenuScreen(context, onStartGame = { context.startPlaying() }))
                return true
            }
            return true
        }

        player.flap()
        playWingSoundSafe()
        return true
    }

    private fun playWingSoundSafe() {
        try {
            context.soundManager.javaClass.methods.firstOrNull {
                it.name in listOf("playWingSound", "playWing", "playFlap")
            }?.invoke(context.soundManager)
        } catch (_: Exception) {}
    }

    private fun playScoreSoundSafe() {
        try {
            context.soundManager.javaClass.methods.firstOrNull {
                it.name in listOf("playScoreSound", "playScore", "playPoint")
            }?.invoke(context.soundManager)
        } catch (_: Exception) {}
    }

    private fun playHitSoundSafe() {
        try {
            context.soundManager.javaClass.methods.firstOrNull {
                it.name in listOf("playHitSound", "playHit", "playDie")
            }?.invoke(context.soundManager)
        } catch (_: Exception) {}
    }

    private fun playCoinSoundSafe() {
        try {
            context.soundManager.javaClass.methods.firstOrNull {
                it.name in listOf("playCoinSound", "playCoin", "playCollect")
            }?.invoke(context.soundManager)
        } catch (_: Exception) {}
    }

    private fun playPowerUpSoundSafe() {
        try {
            context.soundManager.javaClass.methods.firstOrNull {
                it.name in listOf("playPowerUpSound", "playPowerUp")
            }?.invoke(context.soundManager)
        } catch (_: Exception) {}
    }
}