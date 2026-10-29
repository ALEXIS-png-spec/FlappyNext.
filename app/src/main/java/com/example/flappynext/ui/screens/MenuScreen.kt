package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.example.flappynext.ui.screens.DailyRewardScreen
import com.example.flappynext.ui.screens.QuestsScreen
import com.example.flappynext.ui.screens.AchievementsScreen
import com.example.flappynext.ui.screens.CharacterSelectScreen
import com.example.flappynext.ui.screens.StoreScreen

class MenuScreen(
    context: GameContext,
    private val onStartGame: () -> Unit
) : BaseScreen(context) {

    private val playButton = RectF()
    private val charactersButton = RectF()
    private val storeButton = RectF()
    private val dailyRewardButton = RectF()
    private val questsButton = RectF()
    private val achievementsButton = RectF()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
    }

    private fun setupButtons(w: Int, h: Int) {
        val buttonWidth = w * 0.65f
        val buttonHeight = 80f
        val centerX = w / 2f
        val centerY = h / 2f

        playButton.set(
            centerX - buttonWidth / 2f, centerY - 30f,
            centerX + buttonWidth / 2f, centerY + 30f + buttonHeight
        )
        charactersButton.set(
            centerX - buttonWidth / 2f, centerY + 130f,
            centerX + buttonWidth / 2f, centerY + 130f + buttonHeight
        )
        dailyRewardButton.set(w * 0.15f, h * 0.10f, w * 0.85f, h * 0.10f + 65f)
        questsButton.set(w * 0.15f, h * 0.18f, w * 0.85f, h * 0.18f + 65f)
        achievementsButton.set(w * 0.15f, h * 0.26f, w * 0.85f, h * 0.26f + 65f)
        storeButton.set(w * 0.15f, h * 0.84f, w * 0.85f, h * 0.84f + 65f)
    }

    override fun update() {
        // Фоновый дрейф облаков
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        textPaint.textSize = 72f
        canvas.drawText("FlappyNext", context.screenWidth / 2f, context.screenHeight * 0.30f, textPaint)

        drawLevelProgress(canvas)

        textPaint.textSize = 38f
        canvas.drawText("Твой рекорд: ${context.bestScore}", context.screenWidth / 2f, context.screenHeight * 0.40f, textPaint)

        textPaint.textSize = 34f
        canvas.drawText("🪙 Монеты: ${context.coinManager.coins}", context.screenWidth / 2f, context.screenHeight * 0.48f, textPaint)

        drawButton(canvas, playButton, "ИГРАТЬ")
        drawButton(canvas, charactersButton, "ПЕРСОНАЖИ", textSize = 28f)
        drawButton(canvas, storeButton, "МАГАЗИН", textSize = 28f)
        drawButton(
            canvas,
            dailyRewardButton,
            if (context.dailyRewardManager.canClaimToday()) "ЕЖЕДНЕВНАЯ НАГРАДА" else "НАГРАДА ПОЛУЧЕНА",
            textSize = 24f
        )
        drawButton(canvas, questsButton, "ЗАДАНИЯ", textSize = 28f)
        drawButton(canvas, achievementsButton, "ДОСТИЖЕНИЯ", textSize = 28f)

        textPaint.textSize = 28f
        canvas.drawText("Нажми на экран, чтобы лететь", context.screenWidth / 2f, context.screenHeight * 0.80f, textPaint)
    }

    private fun drawLevelProgress(canvas: Canvas) {
        val centerX = context.screenWidth / 2f
        val barWidth = context.screenWidth * 0.55f
        val barHeight = 18f
        val barLeft = centerX - barWidth / 2f
        val barTop = context.screenWidth * 0.345f
        val barRight = barLeft + barWidth
        val barBottom = barTop + barHeight

        textPaint.textSize = 27f
        canvas.drawText("УРОВЕНЬ ${context.levelManager.level}", centerX, barTop - 14f, textPaint)

        cardFillPaint.color = Color.rgb(55, 65, 85)
        tempRect.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(tempRect, 9f, 9f, cardFillPaint)

        val progressRight = barLeft + barWidth * context.levelManager.levelProgress()
        if (progressRight > barLeft) {
            cardFillPaint.color = Color.rgb(60, 210, 140)
            tempRect.set(barLeft, barTop, progressRight, barBottom)
            canvas.drawRoundRect(tempRect, 9f, 9f, cardFillPaint)
        }

        textPaint.textSize = 20f
        canvas.drawText("${context.levelManager.xp} / ${context.levelManager.xpForNextLevel()} XP", centerX, barBottom + 25f, textPaint)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        when {
            playButton.contains(x, y) -> onStartGame()
            dailyRewardButton.contains(x, y) -> context.switchScreen(DailyRewardScreen(context))
            questsButton.contains(x, y) -> context.switchScreen(QuestsScreen(context))
            achievementsButton.contains(x, y) -> context.switchScreen(AchievementsScreen(context))
            charactersButton.contains(x, y) -> context.switchScreen(CharacterSelectScreen(context))
            storeButton.contains(x, y) -> context.switchScreen(StoreScreen(context))
            else -> onStartGame()
        }
        return true
    }
}