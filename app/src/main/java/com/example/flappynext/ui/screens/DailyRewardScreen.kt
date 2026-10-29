package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

class DailyRewardScreen(context: GameContext) : BaseScreen(context) {

    private val claimButton = RectF()
    private val backButton = RectF()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
    }

    private fun setupButtons(w: Int, h: Int) {
        claimButton.set(
            w * 0.15f,
            h * 0.63f,
            w * 0.85f,
            h * 0.63f + 80f
        )
        backButton.set(
            w * 0.25f,
            h * 0.78f,
            w * 0.75f,
            h * 0.78f + 70f
        )
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        val centerX = context.screenWidth / 2f

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = Color.WHITE
        textPaint.textSize = 48f
        canvas.drawText("ЕЖЕДНЕВНАЯ НАГРАДА", centerX, context.screenHeight * 0.13f, textPaint)

        textPaint.textSize = 28f
        textPaint.color = Color.rgb(255, 220, 120)
        val currentDay = context.dailyRewardManager.getNextRewardDay()
        canvas.drawText("День $currentDay из 7", centerX, context.screenHeight * 0.20f, textPaint)

        val cardWidth = context.screenWidth * 0.205f
        val cardHeight = context.screenHeight * 0.115f
        val gap = context.screenWidth * 0.025f
        val totalWidth = cardWidth * 4 + gap * 3

        for (day in 1..7) {
            val row = if (day <= 4) 0 else 1
            val column = if (day <= 4) day - 1 else day - 5
            val rowWidth = if (row == 0) totalWidth else cardWidth * 3 + gap * 2
            val rowStartX = (context.screenWidth - rowWidth) / 2f

            val left = rowStartX + column * (cardWidth + gap)
            val top = context.screenHeight * 0.27f + row * (cardHeight + context.screenHeight * 0.025f)
            tempRect.set(left, top, left + cardWidth, top + cardHeight)

            val claimed = context.dailyRewardManager.hasClaimedDay(day)
            val isCurrent = day == currentDay && context.dailyRewardManager.canClaimToday()

            cardFillPaint.color = when {
                claimed -> Color.rgb(45, 115, 65)
                isCurrent -> Color.rgb(180, 125, 25)
                else -> Color.rgb(65, 70, 85)
            }
            canvas.drawRoundRect(tempRect, 18f, 18f, cardFillPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 25f
            canvas.drawText("ДЕНЬ $day", tempRect.centerX(), tempRect.top + tempRect.height() * 0.36f, textPaint)

            textPaint.textSize = 29f
            canvas.drawText("${context.dailyRewardManager.getRewardForDay(day)} монет", tempRect.centerX(), tempRect.top + tempRect.height() * 0.72f, textPaint)

            if (claimed) {
                textPaint.color = Color.rgb(190, 255, 195)
                textPaint.textSize = 19f
                canvas.drawText("✓ Получено", tempRect.centerX(), tempRect.bottom - 5f, textPaint)
            }
        }

        drawButton(
            canvas,
            claimButton,
            if (context.dailyRewardManager.canClaimToday()) "ЗАБРАТЬ НАГРАДУ" else "УЖЕ ПОЛУЧЕНО",
            textSize = 28f
        )
        drawButton(canvas, backButton, "НАЗАД", textSize = 28f)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        when {
            claimButton.contains(x, y) -> {
                if (context.dailyRewardManager.canClaimToday()) {
                    context.dailyRewardManager.claimToday()
                }
            }
            backButton.contains(x, y) -> {
                context.switchScreen(MenuScreen(context, onStartGame = { context.switchScreen(PlayingScreen(context)) }))
            }
        }
        return true
    }
}