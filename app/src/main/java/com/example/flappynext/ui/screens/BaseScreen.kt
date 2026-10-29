package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

abstract class BaseScreen(protected val context: GameContext) : GameScreen {

    protected val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    protected val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    protected val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    protected val tempRect = RectF()

    protected fun drawOverlay(canvas: Canvas) {
        buttonPaint.color = Color.argb(190, 10, 20, 35)
        canvas.drawRect(0f, 0f, context.screenWidth.toFloat(), context.screenHeight.toFloat(), buttonPaint)
    }

    protected fun drawButton(
        canvas: Canvas,
        bounds: RectF,
        label: String,
        textSize: Float = 32f,
        color: Int = Color.rgb(45, 130, 220)
    ) {
        buttonPaint.color = color
        canvas.drawRoundRect(bounds, 22f, 22f, buttonPaint)

        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize
        val textY = bounds.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText(label, bounds.centerX(), textY, textPaint)
    }
}