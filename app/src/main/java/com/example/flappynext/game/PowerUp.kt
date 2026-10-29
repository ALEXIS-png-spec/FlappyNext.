package com.example.flappynext.game

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.hypot

enum class PowerUpType {
    SHIELD,     // Синий щит
    MAGNET,     // Красный подковообразный магнит
    SLOW_MO     // Желто-оранжевые песочные часы
}

class CollectibleCoin(
    var x: Float,
    var y: Float
) {
    val radius = 18f
    var isCollected = false

    fun update(speed: Float) {
        x -= speed
    }

    fun attractTo(targetX: Float, targetY: Float, force: Float = 14f) {
        val dx = targetX - x
        val dy = targetY - y
        val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (dist > 1f) {
            x += (dx / dist) * force
            y += (dy / dist) * force
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        // Тело монеты
        paint.color = Color.rgb(255, 215, 0)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x, y, radius, paint)

        // Внутренний ободок
        paint.color = Color.rgb(218, 165, 32)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(x, y, radius * 0.72f, paint)

        paint.style = Paint.Style.FILL
    }
}

class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType
) {
    val radius = 24f
    var isCollected = false

    private var animAngle = 0f
    private val iconPath = Path()
    private val tempRect = RectF()

    fun update(speed: Float) {
        x -= speed
        animAngle += 0.08f
    }

    fun draw(canvas: Canvas, paint: Paint) {
        val floatY = y + kotlin.math.sin(animAngle.toDouble()).toFloat() * 6f

        // Фоновый круг-пузырь бонуса
        val bubbleColor = when (type) {
            PowerUpType.SHIELD -> Color.rgb(40, 140, 255)
            PowerUpType.MAGNET -> Color.rgb(240, 60, 60)
            PowerUpType.SLOW_MO -> Color.rgb(255, 170, 0)
        }

        // Внешнее свечение пузыря
        paint.color = Color.argb(80, Color.red(bubbleColor), Color.green(bubbleColor), Color.blue(bubbleColor))
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x, floatY, radius + 6f, paint)

        // Сам пузырь
        paint.color = bubbleColor
        canvas.drawCircle(x, floatY, radius, paint)

        // Белая обводка
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(x, floatY, radius, paint)

        // Рисуем иконку внутри пузыря
        paint.style = Paint.Style.FILL
        when (type) {
            PowerUpType.SHIELD -> drawShieldIcon(canvas, x, floatY, paint)
            PowerUpType.MAGNET -> drawMagnetIcon(canvas, x, floatY, paint)
            PowerUpType.SLOW_MO -> drawHourglassIcon(canvas, x, floatY, paint)
        }
    }

    private fun drawShieldIcon(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        paint.color = Color.WHITE
        iconPath.reset()
        iconPath.moveTo(cx, cy - 12f)
        iconPath.lineTo(cx + 10f, cy - 7f)
        iconPath.lineTo(cx + 8f, cy + 6f)
        iconPath.lineTo(cx, cy + 13f)
        iconPath.lineTo(cx - 8f, cy + 6f)
        iconPath.lineTo(cx - 10f, cy - 7f)
        iconPath.close()
        canvas.drawPath(iconPath, paint)
    }

    private fun drawMagnetIcon(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        tempRect.set(cx - 9f, cy - 8f, cx + 9f, cy + 10f)
        canvas.drawArc(tempRect, 0f, 180f, false, paint)

        // Полюса магнита
        canvas.drawLine(cx - 9f, cy - 8f, cx - 9f, cy, paint)
        canvas.drawLine(cx + 9f, cy - 8f, cx + 9f, cy, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawHourglassIcon(canvas: Canvas, cx: Float, cy: Float, paint: Paint) {
        paint.color = Color.WHITE
        iconPath.reset()
        iconPath.moveTo(cx - 8f, cy - 10f)
        iconPath.lineTo(cx + 8f, cy - 10f)
        iconPath.lineTo(cx, cy)
        iconPath.lineTo(cx + 8f, cy + 10f)
        iconPath.lineTo(cx - 8f, cy + 10f)
        iconPath.lineTo(cx, cy)
        iconPath.close()
        canvas.drawPath(iconPath, paint)
    }
}