package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class AchievementsScreen(context: GameContext) : BaseScreen(context) {

    private val backButton = RectF()
    private val claimButtons = List(6) { RectF() }
    private val cachedLines = mutableMapOf<String, List<String>>()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
        cacheLines(context.screenWidth)
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
        cacheLines(width)
    }

    private fun setupButtons(w: Int, h: Int) {
        val marginX = w * 0.05f
        val gapX = w * 0.035f
        val cardWidth = (w - marginX * 2f - gapX) / 2f
        val startY = h * 0.15f
        val gapY = h * 0.025f
        val cardHeight = h * 0.19f

        for (index in 0 until 6) {
            val col = index % 2
            val row = index / 2
            val left = marginX + col * (cardWidth + gapX)
            val top = startY + row * (cardHeight + gapY)

            claimButtons[index].set(
                left + 12f,
                top + cardHeight - 58f,
                left + cardWidth - 12f,
                top + cardHeight - 28f
            )
        }

        backButton.set(w * 0.30f, h * 0.90f, w * 0.70f, h * 0.90f + 55f)
    }

    private fun cacheLines(w: Int) {
        cachedLines.clear()
        val cardW = (w - (w * 0.05f) * 2f - (w * 0.035f)) / 2f
        val maxTextWidth = cardW - 28f
        textPaint.textSize = 30f

        context.achievementManager.getAchievements().forEach { ach ->
            val words = ach.description.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (textPaint.measureText(testLine) > maxTextWidth && currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine)
            cachedLines[ach.id] = lines
        }
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        val centerX = context.screenWidth / 2f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        textPaint.textSize = 70f
        canvas.drawText("ДОСТИЖЕНИЯ", centerX, context.screenHeight * 0.09f, textPaint)

        val marginX = context.screenWidth * 0.05f
        val gapX = context.screenWidth * 0.035f
        val cardWidth = (context.screenWidth - marginX * 2f - gapX) / 2f
        val startY = context.screenHeight * 0.15f
        val gapY = context.screenHeight * 0.025f
        val cardHeight = context.screenHeight * 0.19f

        cardFillPaint.color = Color.rgb(35, 48, 75)
        cardBorderPaint.color = Color.rgb(85, 170, 255)

        val achievements = context.achievementManager.getAchievements()
        achievements.forEachIndexed { index, ach ->
            val col = index % 2
            val row = index / 2
            val left = marginX + col * (cardWidth + gapX)
            val top = startY + row * (cardHeight + gapY)
            val right = left + cardWidth
            val bottom = top + cardHeight

            tempRect.set(left, top, right, bottom)
            canvas.drawRoundRect(tempRect, 18f, 18f, cardFillPaint)
            canvas.drawRoundRect(tempRect, 18f, 18f, cardBorderPaint)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 40f
            canvas.drawText(ach.title, left + 14f, top + 30f, textPaint)

            textPaint.textSize = 30f
            var lineY = top + 55f
            cachedLines[ach.id]?.forEach { line ->
                canvas.drawText(line, left + 14f, lineY, textPaint)
                lineY += 20f
            }

            val progress = ach.progress.coerceAtMost(ach.target)
            canvas.drawText("$progress / ${ach.target}", left + 14f, top + cardHeight - 16f, textPaint)

            val button = claimButtons[index]
            if (ach.isClaimed) {
                drawButton(canvas, button, "ПОЛУЧЕНО", textSize = 24f)
            } else if (ach.isCompleted) {
                drawButton(canvas, button, "ЗАБРАТЬ +${ach.reward}", textSize = 18f)
            } else {
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.textSize = 20f
                canvas.drawText("Награда: ${ach.reward}", right - 12f, bottom - 16f, textPaint)
            }
        }

        drawButton(canvas, backButton, "НАЗАД", textSize = 30f)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if (backButton.contains(x, y)) {
            context.switchScreen(MenuScreen(context, onStartGame = { context.switchScreen(PlayingScreen(context)) }))
            return true
        }

        val ids = listOf("first_flight", "experienced_pilot", "ace", "wealth", "veteran", "legend")
        claimButtons.forEachIndexed { index, btn ->
            if (btn.contains(x, y)) {
                val reward = context.achievementManager.claimReward(ids[index])
                if (reward > 0) context.addPlayerXp(40)
            }
        }
        return true
    }
}