package com.example.flappynext.obstacles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin

class Obstacle(
    var x: Float,
    val gapY: Float,
    val gapSize: Float,
    val width: Float = 125f,
    private val screenHeight: Int,
    var biomeIndex: Int = 0
) {
    private var speed = 8f
    var nightGlowFactor: Float = 0f
    private var isPassed = false

    val topPipeBottom: Float get() = gapY - gapSize / 2f
    val bottomPipeTop: Float get() = gapY + gapSize / 2f

    private val pipePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val capPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.rgb(25, 30, 25)
    }
    private val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rectHelper = RectF()
    private val pathHelper = Path()
    private var animTime = 0f

    fun setSpeed(newSpeed: Float) {
        speed = newSpeed
    }

    fun update() {
        x -= speed
        animTime += 0.06f
    }

    fun checkPassed(birdX: Float): Boolean {
        if (!isPassed && x + width < birdX) {
            isPassed = true
            return true
        }
        return false
    }

    fun collidesWithCircle(cx: Float, cy: Float, radius: Float): Boolean {
        if (circleIntersectsRect(cx, cy, radius, x, 0f, x + width, topPipeBottom)) return true
        if (circleIntersectsRect(cx, cy, radius, x, bottomPipeTop, x + width, screenHeight.toFloat())) return true
        return false
    }

    private fun circleIntersectsRect(
        cx: Float, cy: Float, r: Float,
        rx1: Float, ry1: Float, rx2: Float, ry2: Float
    ): Boolean {
        val closestX = cx.coerceIn(rx1, rx2)
        val closestY = cy.coerceIn(ry1, ry2)
        val dx = cx - closestX
        val dy = cy - closestY
        return (dx * dx + dy * dy) < (r * r)
    }

    fun draw(canvas: Canvas) {
        when (biomeIndex) {
            1 -> drawKonohaPost(canvas)
            2 -> drawSoulSocietyMonolith(canvas)
            else -> drawClassicPipe(canvas)
        }
    }

    private fun drawClassicPipe(canvas: Canvas) {
        val capHeight = 44f
        val capOverhang = 8f

        pipePaint.shader = LinearGradient(
            x, 0f, x + width, 0f,
            intArrayOf(Color.rgb(105, 195, 55), Color.rgb(140, 230, 80), Color.rgb(45, 125, 30)),
            floatArrayOf(0.0f, 0.35f, 1.0f),
            Shader.TileMode.CLAMP
        )
        capPaint.shader = LinearGradient(
            x - capOverhang, 0f, x + width + capOverhang, 0f,
            intArrayOf(Color.rgb(120, 210, 65), Color.rgb(160, 245, 95), Color.rgb(35, 110, 25)),
            floatArrayOf(0.0f, 0.35f, 1.0f),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(x, 0f, x + width, topPipeBottom - capHeight, pipePaint)
        canvas.drawRect(x, 0f, x + width, topPipeBottom - capHeight, outlinePaint)

        rectHelper.set(x - capOverhang, topPipeBottom - capHeight, x + width + capOverhang, topPipeBottom)
        canvas.drawRoundRect(rectHelper, 6f, 6f, capPaint)
        canvas.drawRoundRect(rectHelper, 6f, 6f, outlinePaint)

        canvas.drawRect(x, bottomPipeTop + capHeight, x + width, screenHeight.toFloat(), pipePaint)
        canvas.drawRect(x, bottomPipeTop + capHeight, x + width, screenHeight.toFloat(), outlinePaint)

        rectHelper.set(x - capOverhang, bottomPipeTop, x + width + capOverhang, bottomPipeTop + capHeight)
        canvas.drawRoundRect(rectHelper, 6f, 6f, capPaint)
        canvas.drawRoundRect(rectHelper, 6f, 6f, outlinePaint)

        detailPaint.style = Paint.Style.FILL
        detailPaint.color = Color.rgb(20, 60, 15)
        for (i in 1..4) {
            val rivetX = x - capOverhang + i * ((width + capOverhang * 2) / 5f)
            canvas.drawCircle(rivetX, topPipeBottom - capHeight / 2f, 3.5f, detailPaint)
            canvas.drawCircle(rivetX, bottomPipeTop + capHeight / 2f, 3.5f, detailPaint)
        }
    }

    private fun drawKonohaPost(canvas: Canvas) {
        pipePaint.shader = LinearGradient(
            x, 0f, x + width, 0f,
            intArrayOf(Color.rgb(175, 120, 75), Color.rgb(205, 150, 95), Color.rgb(115, 75, 45)),
            floatArrayOf(0.0f, 0.4f, 1.0f),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(x, 0f, x + width, topPipeBottom, pipePaint)
        canvas.drawRect(x, 0f, x + width, topPipeBottom, outlinePaint)
        canvas.drawRect(x, bottomPipeTop, x + width, screenHeight.toFloat(), pipePaint)
        canvas.drawRect(x, bottomPipeTop, x + width, screenHeight.toFloat(), outlinePaint)

        detailPaint.style = Paint.Style.STROKE
        detailPaint.strokeWidth = 2.5f
        detailPaint.color = Color.argb(90, 70, 40, 20)
        canvas.drawLine(x + width * 0.3f, 0f, x + width * 0.3f, topPipeBottom, detailPaint)
        canvas.drawLine(x + width * 0.7f, 0f, x + width * 0.7f, topPipeBottom, detailPaint)
        canvas.drawLine(x + width * 0.3f, bottomPipeTop, x + width * 0.3f, screenHeight.toFloat(), detailPaint)
        canvas.drawLine(x + width * 0.7f, bottomPipeTop, x + width * 0.7f, screenHeight.toFloat(), detailPaint)

        detailPaint.style = Paint.Style.FILL
        detailPaint.color = Color.rgb(240, 235, 230)
        canvas.drawRect(x, topPipeBottom - 70f, x + width, topPipeBottom - 40f, detailPaint)
        canvas.drawRect(x, bottomPipeTop + 40f, x + width, bottomPipeTop + 70f, detailPaint)

        detailPaint.color = Color.rgb(235, 215, 175)
        rectHelper.set(x - 5f, topPipeBottom - 16f, x + width + 5f, topPipeBottom)
        canvas.drawRoundRect(rectHelper, 6f, 6f, detailPaint)
        canvas.drawRoundRect(rectHelper, 6f, 6f, outlinePaint)

        rectHelper.set(x - 5f, bottomPipeTop, x + width + 5f, bottomPipeTop + 16f)
        canvas.drawRoundRect(rectHelper, 6f, 6f, detailPaint)
        canvas.drawRoundRect(rectHelper, 6f, 6f, outlinePaint)
    }

    private fun drawSoulSocietyMonolith(canvas: Canvas) {
        pipePaint.shader = LinearGradient(
            x, 0f, x + width, 0f,
            intArrayOf(Color.rgb(225, 230, 240), Color.rgb(245, 248, 255), Color.rgb(155, 165, 185)),
            floatArrayOf(0.0f, 0.4f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val capH = 34f
        canvas.drawRect(x, 0f, x + width, topPipeBottom - capH, pipePaint)
        canvas.drawRect(x, 0f, x + width, topPipeBottom - capH, outlinePaint)
        canvas.drawRect(x, bottomPipeTop + capH, x + width, screenHeight.toFloat(), pipePaint)
        canvas.drawRect(x, bottomPipeTop + capH, x + width, screenHeight.toFloat(), outlinePaint)

        detailPaint.style = Paint.Style.FILL
        detailPaint.color = Color.rgb(35, 40, 55)
        rectHelper.set(x - 8f, topPipeBottom - capH, x + width + 8f, topPipeBottom)
        canvas.drawRoundRect(rectHelper, 6f, 6f, detailPaint)
        canvas.drawRoundRect(rectHelper, 6f, 6f, outlinePaint)

        rectHelper.set(x - 8f, bottomPipeTop, x + width + 8f, bottomPipeTop + capH)
        canvas.drawRoundRect(rectHelper, 6f, 6f, detailPaint)
        canvas.drawRoundRect(rectHelper, 6f, 6f, outlinePaint)

        val pulse = (sin(animTime.toDouble()).toFloat() * 0.25f + 0.75f)
        glowPaint.color = Color.argb((pulse * 230).toInt().coerceIn(0, 255), 80, 225, 255)
        glowPaint.strokeWidth = 3f
        glowPaint.style = Paint.Style.STROKE

        val rx = x + width / 2f
        val ry1 = topPipeBottom - capH - 55f
        val ry2 = bottomPipeTop + capH + 55f

        pathHelper.reset()
        pathHelper.moveTo(rx, ry1 - 18f); pathHelper.lineTo(rx + 18f, ry1); pathHelper.lineTo(rx, ry1 + 18f); pathHelper.lineTo(rx - 18f, ry1); pathHelper.close()
        canvas.drawPath(pathHelper, glowPaint)

        pathHelper.reset()
        pathHelper.moveTo(rx, ry2 - 18f); pathHelper.lineTo(rx + 18f, ry2); pathHelper.lineTo(rx, ry2 + 18f); pathHelper.lineTo(rx - 18f, ry2); pathHelper.close()
        canvas.drawPath(pathHelper, glowPaint)
    }
}