package com.example.flappynext.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.random.Random

class FeatherParticle(
    var x: Float,
    var y: Float,
    private val color: Int
) {
    private var vx = (Random.nextFloat() - 0.5f) * 16f
    private var vy = (Random.nextFloat() - 0.7f) * 18f
    private val gravity = 0.5f

    private var rotation = Random.nextFloat() * 360f
    private val rotSpeed = (Random.nextFloat() - 0.5f) * 20f

    private var life = 1f
    private val decay = Random.nextFloat() * 0.02f + 0.025f // Срок жизни 30-40 кадров

    private val length = Random.nextFloat() * 10f + 14f
    private val width = length * 0.45f
    private val rect = RectF()

    companion object {
        private val sharedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    fun update() {
        x += vx
        y += vy
        vy += gravity
        vx *= 0.96f // Сопротивление воздуха

        rotation += rotSpeed
        life = (life - decay).coerceAtLeast(0f)
    }

    fun draw(canvas: Canvas) {
        if (life <= 0f) return

        val alpha = (life * 255).toInt().coerceIn(0, 255)
        sharedPaint.color = Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)

        // Форма продолговатого овального перышка
        rect.set(-width / 2f, -length / 2f, width / 2f, length / 2f)
        canvas.drawOval(rect, sharedPaint)

        canvas.restore()
    }

    fun isDead(): Boolean = life <= 0f
}