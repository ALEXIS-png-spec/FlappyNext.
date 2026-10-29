package com.example.flappynext.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max

class TrailParticle(
    var x: Float,
    var y: Float,
    private var size: Float,
    private var life: Float = 1f
) {
    companion object {
        // Один общий Paint на все частицы
        private val sharedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val speed = 1.8f

    fun update() {
        x -= speed
        y += 0.25f
        life -= 0.035f
        size *= 0.97f
    }

    fun draw(canvas: Canvas) {
        if (life <= 0f) return

        sharedPaint.color = Color.argb(
            (255 * life).toInt().coerceIn(0, 255),
            255,
            255,
            255
        )

        canvas.drawCircle(
            x,
            y,
            max(1f, size),
            sharedPaint
        )
    }

    fun isDead(): Boolean {
        return life <= 0f
    }
}