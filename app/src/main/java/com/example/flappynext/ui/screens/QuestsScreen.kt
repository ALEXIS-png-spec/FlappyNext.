package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

class QuestsScreen(context: GameContext) : BaseScreen(context) {

    private var questsTab = 0 // 0 = Основные, 1 = Ежедневные

    private val mainQuestsTabButton = RectF()
    private val dailyQuestsTabButton = RectF()
    private val backButton = RectF()
    private val claimButton1 = RectF()
    private val claimButton2 = RectF()
    private val claimButton3 = RectF()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
    }

    private fun setupButtons(w: Int, h: Int) {
        mainQuestsTabButton.set(w * 0.08f, h * 0.16f, w * 0.47f, h * 0.16f + 60f)
        dailyQuestsTabButton.set(w * 0.53f, h * 0.16f, w * 0.92f, h * 0.16f + 60f)
        backButton.set(w * 0.25f, h * 0.84f, w * 0.75f, h * 0.84f + 65f)

        claimButton1.set(w * 0.60f, h * 0.29f, w * 0.88f, h * 0.29f + 55f)
        claimButton2.set(w * 0.60f, h * 0.475f, w * 0.88f, h * 0.475f + 55f)
        claimButton3.set(w * 0.60f, h * 0.66f, w * 0.88f, h * 0.66f + 55f)
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        val centerX = context.screenWidth / 2f

        drawButton(canvas, mainQuestsTabButton, "ЗАДАНИЯ", textSize = 22f, color = if (questsTab == 0) Color.rgb(55, 145, 235) else Color.rgb(65, 75, 95))
        drawButton(canvas, dailyQuestsTabButton, "ЕЖЕДНЕВНЫЕ", textSize = 20f, color = if (questsTab == 1) Color.rgb(55, 145, 235) else Color.rgb(65, 75, 95))

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.color = Color.WHITE
        textPaint.textSize = 52f
        canvas.drawText(if (questsTab == 0) "ЗАДАНИЯ" else "ЕЖЕДНЕВНЫЕ", centerX, context.screenHeight * 0.10f, textPaint)

        val cardLeft = context.screenWidth * 0.10f
        val cardRight = context.screenWidth * 0.90f
        val cardHeight = context.screenHeight * 0.17f

        if (questsTab == 0) {
            val quests = context.questManager.getQuests()
            quests.forEachIndexed { index, quest ->
                val top = context.screenHeight * 0.21f + index * (cardHeight + context.screenHeight * 0.025f)
                tempRect.set(cardLeft, top, cardRight, top + cardHeight)

                cardFillPaint.color = when {
                    quest.isClaimed -> Color.rgb(45, 115, 65)
                    quest.isCompleted -> Color.rgb(180, 125, 25)
                    else -> Color.rgb(65, 70, 85)
                }
                canvas.drawRoundRect(tempRect, 20f, 20f, cardFillPaint)

                textPaint.color = Color.WHITE
                textPaint.textSize = 27f
                canvas.drawText(quest.title, tempRect.centerX(), tempRect.top + 35f, textPaint)

                textPaint.typeface = Typeface.DEFAULT
                textPaint.textSize = 20f
                canvas.drawText(quest.description, tempRect.centerX(), tempRect.top + 65f, textPaint)

                val progress = (quest.progress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f)
                drawProgressBar(canvas, tempRect, progress)

                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.textSize = 18f
                canvas.drawText("${quest.progress} / ${quest.target}", tempRect.centerX(), top + cardHeight - 12f, textPaint)

                textPaint.textSize = 19f
                canvas.drawText("🪙 ${quest.reward}", cardRight - 65f, top + 35f, textPaint)
            }

            val q1 = quests.getOrNull(0)
            val q2 = quests.getOrNull(1)
            val q3 = quests.getOrNull(2)
            if (q1 != null && q1.isCompleted && !q1.isClaimed) drawButton(canvas, claimButton1, "ЗАБРАТЬ 10", textSize = 22f)
            if (q2 != null && q2.isCompleted && !q2.isClaimed) drawButton(canvas, claimButton2, "ЗАБРАТЬ 20", textSize = 22f)
            if (q3 != null && q3.isCompleted && !q3.isClaimed) drawButton(canvas, claimButton3, "ЗАБРАТЬ 15", textSize = 22f)
        } else {
            val dailyQuests = context.dailyQuestManager.getQuests()
            dailyQuests.forEachIndexed { index, quest ->
                val top = context.screenHeight * 0.21f + index * (cardHeight + context.screenHeight * 0.025f)
                tempRect.set(cardLeft, top, cardRight, top + cardHeight)

                cardFillPaint.color = when {
                    quest.isClaimed -> Color.rgb(45, 115, 65)
                    quest.isCompleted -> Color.rgb(180, 125, 25)
                    else -> Color.rgb(65, 70, 85)
                }
                canvas.drawRoundRect(tempRect, 20f, 20f, cardFillPaint)

                textPaint.color = Color.WHITE
                textPaint.textSize = 27f
                canvas.drawText(quest.title, tempRect.centerX(), tempRect.top + 35f, textPaint)

                textPaint.typeface = Typeface.DEFAULT
                textPaint.textSize = 20f
                canvas.drawText(quest.description, tempRect.centerX(), tempRect.top + 65f, textPaint)

                val progress = (quest.progress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f)
                drawProgressBar(canvas, tempRect, progress)

                textPaint.typeface = Typeface.DEFAULT_BOLD
                textPaint.textSize = 18f
                canvas.drawText("${quest.progress} / ${quest.target}", tempRect.centerX(), top + cardHeight - 12f, textPaint)

                textPaint.textSize = 19f
                canvas.drawText("🪙 ${quest.reward}", cardRight - 65f, top + 35f, textPaint)
            }

            val d1 = dailyQuests.getOrNull(0)
            val d2 = dailyQuests.getOrNull(1)
            val d3 = dailyQuests.getOrNull(2)
            if (d1 != null && d1.isCompleted && !d1.isClaimed) drawButton(canvas, claimButton1, "ЗАБРАТЬ ${d1.reward}", textSize = 22f)
            if (d2 != null && d2.isCompleted && !d2.isClaimed) drawButton(canvas, claimButton2, "ЗАБРАТЬ ${d2.reward}", textSize = 22f)
            if (d3 != null && d3.isCompleted && !d3.isClaimed) drawButton(canvas, claimButton3, "ЗАБРАТЬ ${d3.reward}", textSize = 22f)
        }

        drawButton(canvas, backButton, "НАЗАД", textSize = 26f)
    }

    private fun drawProgressBar(canvas: Canvas, card: RectF, progress: Float) {
        val pLeft = card.left + 30f
        val pRight = card.right - 30f
        val pTop = card.top + 82f
        val pBottom = pTop + 14f

        cardFillPaint.color = Color.rgb(40, 45, 55)
        tempRect.set(pLeft, pTop, pRight, pBottom)
        canvas.drawRoundRect(tempRect, 7f, 7f, cardFillPaint)

        cardFillPaint.color = Color.rgb(100, 210, 110)
        tempRect.set(pLeft, pTop, pLeft + (pRight - pLeft) * progress, pBottom)
        canvas.drawRoundRect(tempRect, 7f, 7f, cardFillPaint)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        when {
            mainQuestsTabButton.contains(x, y) -> questsTab = 0
            dailyQuestsTabButton.contains(x, y) -> questsTab = 1
            backButton.contains(x, y) -> {
                context.switchScreen(MenuScreen(context, onStartGame = { context.switchScreen(PlayingScreen(context)) }))
            }
            claimButton1.contains(x, y) -> {
                if (questsTab == 0) {
                    if (context.questManager.claimReward("novice") > 0) context.addPlayerXp(30)
                } else {
                    if (context.dailyQuestManager.claimReward("daily_obstacles") > 0) context.addPlayerXp(50)
                }
            }
            claimButton2.contains(x, y) -> {
                if (questsTab == 0) {
                    if (context.questManager.claimReward("pilot") > 0) context.addPlayerXp(30)
                } else {
                    if (context.dailyQuestManager.claimReward("daily_rounds") > 0) context.addPlayerXp(50)
                }
            }
            claimButton3.contains(x, y) -> {
                if (questsTab == 0) {
                    if (context.questManager.claimReward("persistent") > 0) context.addPlayerXp(30)
                } else {
                    if (context.dailyQuestManager.claimReward("daily_score") > 0) context.addPlayerXp(50)
                }
            }
        }
        return true
    }
}