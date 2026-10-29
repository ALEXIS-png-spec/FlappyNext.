package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class StoreScreen(context: GameContext) : BaseScreen(context) {

    private val accessoriesButton = RectF()
    private val skinsButton = RectF()
    private val backButton = RectF()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
    }

    private fun setupButtons(w: Int, h: Int) {
        accessoriesButton.set(w * 0.10f, h * 0.32f, w * 0.90f, h * 0.32f + 150f)
        skinsButton.set(w * 0.10f, h * 0.50f, w * 0.90f, h * 0.50f + 150f)
        backButton.set(w * 0.25f, h * 0.82f, w * 0.75f, h * 0.82f + 65f)
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 48f
        textPaint.color = Color.WHITE
        canvas.drawText("МАГАЗИН", context.screenWidth / 2f, context.screenHeight * 0.20f, textPaint)

        drawButton(canvas, accessoriesButton, "АКСЕССУАРЫ", textSize = 28f)
        drawButton(canvas, skinsButton, "СКИНЫ", textSize = 28f)
        drawButton(canvas, backButton, "НАЗАД", textSize = 28f)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        when {
            accessoriesButton.contains(x, y) -> context.switchScreen(AccessoriesScreen(context))
            skinsButton.contains(x, y) -> context.switchScreen(SkinsScreen(context))
            backButton.contains(x, y) -> {
                context.switchScreen(MenuScreen(context, onStartGame = { context.switchScreen(PlayingScreen(context)) }))
            }
        }
        return true
    }
}