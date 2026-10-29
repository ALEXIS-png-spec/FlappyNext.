package com.example.flappynext.world

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import kotlin.math.sin

class Background(
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    val groundTop: Float = screenHeight * 0.86f

    var dayNightPhase: Float = 0f
        private set
    var nightFactor: Float = 0f
        private set

    var biomeIndex: Int = 0
        private set

    private var nextBiomeIndex: Int = 1
    private var biomeTransitionProgress = 0f

    private var farOffset = 0f
    private var nearOffset = 0f
    private var groundOffset = 0f

    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val celestialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paintShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val paintDetail = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val kanjiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val groundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val groundBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val pathA = Path()
    private val pathB = Path()
    private val moonCutoutPath = Path()
    private val moonCutPath = Path()
    private val rectHelper = RectF()

    private var starTwinkleTime = 0f
    private var lastSkyTopColor = 0
    private var lastSkyBottomColor = 0

    private class Star(val x: Float, val y: Float, val size: Float, val phaseOffset: Float)
    private val stars = List(45) {
        Star(
            x = (Math.random() * screenWidth).toFloat(),
            y = (Math.random() * (groundTop * 0.7f)).toFloat(),
            size = (Math.random() * 3f + 1.5f).toFloat(),
            phaseOffset = (Math.random() * Math.PI * 2).toFloat()
        )
    }

    private class Cloud(var x: Float, val y: Float, val scale: Float, val speed: Float)
    private val clouds = listOf(
        Cloud(screenWidth * 0.15f, screenHeight * 0.14f, 1.1f, 0.35f),
        Cloud(screenWidth * 0.65f, screenHeight * 0.26f, 0.85f, 0.22f),
        Cloud(screenWidth * 0.40f, screenHeight * 0.08f, 0.60f, 0.15f),
        Cloud(screenWidth * 0.95f, screenHeight * 0.19f, 0.95f, 0.28f)
    )

    fun update(gameSpeed: Float, isPlaying: Boolean, currentScore: Int = 0) {
        val cycleProgress = (currentScore % 72) / 18f
        dayNightPhase = cycleProgress

        nightFactor = when {
            dayNightPhase < 1f -> 0f
            dayNightPhase < 2f -> dayNightPhase - 1f
            dayNightPhase < 3f -> 1f - (dayNightPhase - 2f)
            else -> 0f
        }.coerceIn(0f, 1f)

        val biomeCycleLength = 20
        val totalBiomeIndex = (currentScore / biomeCycleLength) % 3
        val scoreInCurrentBiome = currentScore % biomeCycleLength

        biomeIndex = totalBiomeIndex
        nextBiomeIndex = (totalBiomeIndex + 1) % 3

        biomeTransitionProgress = if (scoreInCurrentBiome >= biomeCycleLength - 5) {
            (scoreInCurrentBiome - (biomeCycleLength - 5)) / 5f
        } else {
            0f
        }

        starTwinkleTime += 0.05f

        clouds.forEach { cloud ->
            cloud.x -= cloud.speed + (if (isPlaying) gameSpeed * 0.07f else 0f)
            val cloudW = 90f * cloud.scale
            if (cloud.x + cloudW < 0f) {
                cloud.x = screenWidth + cloudW
            }
        }

        if (isPlaying) {
            farOffset = (farOffset + gameSpeed * 0.15f) % screenWidth
            nearOffset = (nearOffset + gameSpeed * 0.60f) % screenWidth
            groundOffset = (groundOffset + gameSpeed) % 50f
        }
    }

    fun draw(canvas: Canvas) {
        applyDayNightSky()

        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), skyPaint)
        if (nightFactor > 0.05f) drawStars(canvas)
        drawCelestialBody(canvas)

        clouds.forEach { drawCloud(canvas, it.x, it.y, it.scale) }

        val currentAlpha = ((1f - biomeTransitionProgress) * 255).toInt().coerceIn(0, 255)
        drawFullBiome(canvas, biomeIndex, currentAlpha)

        if (biomeTransitionProgress > 0f) {
            val nextAlpha = (biomeTransitionProgress * 255).toInt().coerceIn(0, 255)
            drawFullBiome(canvas, nextBiomeIndex, nextAlpha)
        }

        drawBiomeGround(canvas)
    }

    private fun drawFullBiome(canvas: Canvas, biome: Int, alpha: Int) {
        if (alpha <= 0) return

        when (biome) {
            0 -> {
                drawHillsLayer(canvas, farOffset, screenHeight * 0.62f, screenHeight * 0.13f, getShadedColor(Color.rgb(100, 195, 135)), alpha)
                drawHillsLayer(canvas, nearOffset, screenHeight * 0.72f, screenHeight * 0.08f, getShadedColor(Color.rgb(65, 170, 100)), alpha)
            }
            1 -> {
                drawKonohaHokageRock(canvas, farOffset, alpha)
                drawKonohaVillage(canvas, nearOffset, alpha)
            }
            2 -> {
                drawSoulSocietySenzaikyu(canvas, farOffset, alpha)
                drawSoulSocietySeireitei(canvas, nearOffset, alpha)
            }
        }
    }

    private fun drawHillsLayer(canvas: Canvas, offset: Float, baseHeight: Float, waveHeight: Float, color: Int, alpha: Int) {
        paintFill.color = color
        paintFill.alpha = alpha

        pathA.reset()
        val w = screenWidth.toFloat()
        val startX = -offset

        pathA.moveTo(startX, groundTop)
        pathA.lineTo(startX, baseHeight)

        for (i in 0..1) {
            val sX = startX + i * w
            pathA.quadTo(sX + w * 0.25f, baseHeight - waveHeight, sX + w * 0.5f, baseHeight)
            pathA.quadTo(sX + w * 0.75f, baseHeight + waveHeight * 0.6f, sX + w, baseHeight)
        }

        pathA.lineTo(startX + w * 2f, groundTop)
        pathA.close()
        canvas.drawPath(pathA, paintFill)
    }

    private fun drawKonohaHokageRock(canvas: Canvas, offset: Float, alpha: Int) {
        val w = screenWidth.toFloat()
        val startX = -offset
        val rockBaseY = screenHeight * 0.50f

        paintFill.color = getShadedColor(Color.rgb(188, 150, 115))
        paintFill.alpha = alpha
        paintShadow.color = getShadedColor(Color.rgb(138, 105, 78))
        paintShadow.alpha = alpha
        paintStroke.color = getShadedColor(Color.rgb(65, 45, 32))
        paintStroke.alpha = alpha

        pathA.reset()
        pathA.moveTo(startX, groundTop)
        pathA.lineTo(startX, rockBaseY + 60f)

        for (i in 0..1) {
            val sX = startX + i * w
            pathA.lineTo(sX + w * 0.05f, rockBaseY - 20f)
            pathA.lineTo(sX + w * 0.95f, rockBaseY - 20f)
            pathA.lineTo(sX + w, rockBaseY + 50f)
        }
        pathA.lineTo(startX + w * 2f, groundTop)
        pathA.close()
        canvas.drawPath(pathA, paintFill)

        for (i in 0..1) {
            val sX = startX + i * w
            drawHokageFace(canvas, sX + w * 0.12f, rockBaseY + 30f, 1, alpha)
            drawHokageFace(canvas, sX + w * 0.35f, rockBaseY + 20f, 2, alpha)
            drawHokageFace(canvas, sX + w * 0.58f, rockBaseY + 25f, 3, alpha)
            drawHokageFace(canvas, sX + w * 0.80f, rockBaseY + 15f, 4, alpha)
        }
    }

    private fun drawHokageFace(canvas: Canvas, x: Float, y: Float, index: Int, alpha: Int) {
        rectHelper.set(x - 34f, y - 48f, x + 34f, y + 42f)
        canvas.drawRoundRect(rectHelper, 16f, 16f, paintShadow)

        canvas.drawRect(x - 24f, y - 14f, x - 6f, y - 6f, paintStroke)
        canvas.drawRect(x + 6f, y - 14f, x + 24f, y - 6f, paintStroke)

        pathB.reset()
        pathB.moveTo(x, y - 8f)
        pathB.lineTo(x + 7f, y + 10f)
        pathB.lineTo(x - 4f, y + 12f)
        canvas.drawPath(pathB, paintStroke)

        canvas.drawLine(x - 12f, y + 24f, x + 12f, y + 24f, paintStroke)

        when (index) {
            1 -> {
                canvas.drawLine(x - 30f, y - 35f, x - 32f, y + 30f, paintStroke)
                canvas.drawLine(x + 30f, y - 35f, x + 32f, y + 30f, paintStroke)
            }
            2 -> {
                canvas.drawLine(x - 26f, y - 28f, x - 18f, y + 15f, paintStroke)
                canvas.drawLine(x + 26f, y - 28f, x + 18f, y + 15f, paintStroke)
                canvas.drawLine(x - 18f, y + 15f, x, y + 32f, paintStroke)
            }
            3 -> {
                canvas.drawArc(x - 26f, y - 48f, x + 26f, y - 20f, 180f, 180f, false, paintStroke)
                canvas.drawLine(x - 16f, y + 4f, x - 26f, y + 14f, paintStroke)
                canvas.drawLine(x + 16f, y + 4f, x + 26f, y + 14f, paintStroke)
            }
            4 -> {
                pathB.reset()
                pathB.moveTo(x - 36f, y - 25f)
                pathB.lineTo(x - 45f, y - 48f)
                pathB.lineTo(x - 22f, y - 40f)
                pathB.lineTo(x, y - 62f)
                pathB.lineTo(x + 22f, y - 40f)
                pathB.lineTo(x + 45f, y - 48f)
                pathB.lineTo(x + 36f, y - 25f)
                canvas.drawPath(pathB, paintStroke)
            }
        }
    }

    private fun drawKonohaVillage(canvas: Canvas, offset: Float, alpha: Int) {
        val w = screenWidth.toFloat()
        val startX = -offset
        val baseVillageY = screenHeight * 0.72f

        for (i in 0..1) {
            val sX = startX + i * w

            paintFill.color = getShadedColor(Color.rgb(215, 175, 130)); paintFill.alpha = alpha
            canvas.drawRect(sX + w * 0.05f, baseVillageY + 30f, sX + w * 0.22f, groundTop, paintFill)
            paintFill.color = getShadedColor(Color.rgb(85, 125, 100)); paintFill.alpha = alpha
            canvas.drawRect(sX + w * 0.03f, baseVillageY + 20f, sX + w * 0.24f, baseVillageY + 35f, paintFill)

            val hx = sX + w * 0.45f
            paintFill.color = getShadedColor(Color.rgb(215, 175, 130)); paintFill.alpha = alpha
            canvas.drawRect(hx - 70f, baseVillageY - 20f, hx + 70f, groundTop, paintFill)

            paintFill.color = getShadedColor(Color.rgb(205, 50, 45)); paintFill.alpha = alpha
            rectHelper.set(hx - 90f, baseVillageY - 45f, hx + 90f, baseVillageY - 15f)
            canvas.drawRoundRect(rectHelper, 12f, 12f, paintFill)
            rectHelper.set(hx - 60f, baseVillageY - 75f, hx + 60f, baseVillageY - 45f)
            canvas.drawRoundRect(rectHelper, 10f, 10f, paintFill)

            paintDetail.color = Color.WHITE; paintDetail.alpha = alpha
            canvas.drawCircle(hx, baseVillageY - 10f, 22f, paintDetail)

            kanjiPaint.color = Color.rgb(200, 30, 30); kanjiPaint.alpha = alpha
            kanjiPaint.textSize = 28f
            canvas.drawText("火", hx, baseVillageY, kanjiPaint)

            val tx = sX + w * 0.78f
            paintFill.color = getShadedColor(Color.rgb(110, 115, 125)); paintFill.alpha = alpha
            canvas.drawCircle(tx, baseVillageY + 10f, 32f, paintFill)
            canvas.drawLine(tx - 20f, baseVillageY + 10f, tx - 20f, groundTop, paintStroke)
            canvas.drawLine(tx + 20f, baseVillageY + 10f, tx + 20f, groundTop, paintStroke)
            canvas.drawLine(tx - 20f, groundTop - 20f, tx + 20f, baseVillageY + 20f, paintStroke)
        }
    }

    private fun drawSoulSocietySenzaikyu(canvas: Canvas, offset: Float, alpha: Int) {
        val w = screenWidth.toFloat()
        val startX = -offset
        val wallBaseY = screenHeight * 0.58f

        for (i in 0..1) {
            val sX = startX + i * w

            paintDetail.color = Color.argb((alpha * 0.35f).toInt(), 120, 210, 255)
            canvas.drawCircle(sX + w * 0.5f, wallBaseY - 120f, w * 0.45f, paintDetail)

            paintFill.color = getShadedColor(Color.rgb(130, 140, 160)); paintFill.alpha = alpha
            canvas.drawRect(sX, wallBaseY + 40f, sX + w, groundTop, paintFill)

            val bx = sX + w * 0.5f
            paintFill.color = getShadedColor(Color.rgb(230, 235, 245)); paintFill.alpha = alpha
            paintStroke.color = getShadedColor(Color.rgb(130, 140, 160)); paintStroke.alpha = alpha

            pathA.reset()
            pathA.moveTo(bx - 55f, wallBaseY + 40f)
            pathA.lineTo(bx - 35f, wallBaseY - 60f)
            pathA.lineTo(bx + 35f, wallBaseY - 60f)
            pathA.lineTo(bx + 55f, wallBaseY + 40f)
            canvas.drawPath(pathA, paintFill)

            pathB.reset()
            pathB.moveTo(bx - 22f, wallBaseY - 60f)
            pathB.lineTo(bx - 6f, screenHeight * 0.26f)
            pathB.lineTo(bx, screenHeight * 0.22f)
            pathB.lineTo(bx + 6f, screenHeight * 0.26f)
            pathB.lineTo(bx + 22f, wallBaseY - 60f)
            pathB.close()
            canvas.drawPath(pathB, paintFill)
            canvas.drawPath(pathB, paintStroke)

            canvas.drawLine(bx - 110f, wallBaseY - 30f, bx - 30f, wallBaseY - 55f, paintStroke)
        }
    }

    private fun drawSoulSocietySeireitei(canvas: Canvas, offset: Float, alpha: Int) {
        val w = screenWidth.toFloat()
        val startX = -offset
        val baseSeireiY = screenHeight * 0.73f

        val pagodaTile = getShadedColor(Color.rgb(45, 55, 75))
        val shojiWhite = getShadedColor(Color.rgb(240, 240, 235))
        val woodDark = getShadedColor(Color.rgb(75, 45, 35))

        for (i in 0..1) {
            val sX = startX + i * w

            drawJapanesePagoda(canvas, sX + w * 0.18f, baseSeireiY, pagodaTile, shojiWhite, woodDark, alpha)
            drawJapanesePagoda(canvas, sX + w * 0.68f, baseSeireiY + 15f, pagodaTile, shojiWhite, woodDark, alpha)

            val toriiX = sX + w * 0.44f
            val toriiY = baseSeireiY + 20f
            paintStroke.color = getShadedColor(Color.rgb(180, 40, 35)); paintStroke.alpha = alpha
            paintStroke.strokeWidth = 6f
            canvas.drawLine(toriiX - 25f, toriiY, toriiX - 25f, groundTop, paintStroke)
            canvas.drawLine(toriiX + 25f, toriiY, toriiX + 25f, groundTop, paintStroke)

            pathA.reset()
            pathA.moveTo(toriiX - 38f, toriiY - 14f)
            pathA.quadTo(toriiX, toriiY - 8f, toriiX + 38f, toriiY - 14f)
            canvas.drawPath(pathA, paintStroke)
            canvas.drawLine(toriiX - 30f, toriiY - 2f, toriiX + 30f, toriiY - 2f, paintStroke)
            paintStroke.strokeWidth = 3.5f
        }
    }

    private fun drawJapanesePagoda(canvas: Canvas, cx: Float, baseY: Float, tileCol: Int, wallCol: Int, woodCol: Int, alpha: Int) {
        paintFill.color = wallCol; paintFill.alpha = alpha
        canvas.drawRect(cx - 45f, baseY - 40f, cx + 45f, groundTop, paintFill)

        paintStroke.color = woodCol; paintStroke.alpha = alpha
        canvas.drawRect(cx - 45f, baseY - 40f, cx + 45f, groundTop, paintStroke)
        canvas.drawLine(cx, baseY - 40f, cx, groundTop, paintStroke)

        paintFill.color = tileCol; paintFill.alpha = alpha
        pathA.reset()
        pathA.moveTo(cx - 70f, baseY - 35f)
        pathA.quadTo(cx, baseY - 52f, cx + 70f, baseY - 35f)
        pathA.lineTo(cx + 50f, baseY - 60f)
        pathA.quadTo(cx, baseY - 68f, cx - 50f, baseY - 60f)
        pathA.close()
        canvas.drawPath(pathA, paintFill)

        pathB.reset()
        pathB.moveTo(cx - 50f, baseY - 70f)
        pathB.quadTo(cx, baseY - 88f, cx + 50f, baseY - 70f)
        pathB.lineTo(cx + 30f, baseY - 98f)
        pathB.lineTo(cx, baseY - 120f)
        pathB.lineTo(cx - 30f, baseY - 98f)
        pathB.close()
        canvas.drawPath(pathB, paintFill)
    }

    private fun drawBiomeGround(canvas: Canvas) {
        val w = screenWidth.toFloat()
        val h = screenHeight.toFloat()

        when (biomeIndex) {
            0 -> {
                groundPaint.color = getShadedColor(Color.rgb(180, 135, 75))
                groundBorderPaint.color = getShadedColor(Color.rgb(65, 185, 80))
            }
            1 -> {
                groundPaint.color = getShadedColor(Color.rgb(195, 125, 70))
                groundBorderPaint.color = getShadedColor(Color.rgb(140, 85, 45))
            }
            2 -> {
                groundPaint.color = getShadedColor(Color.rgb(155, 165, 180))
                groundBorderPaint.color = getShadedColor(Color.rgb(215, 225, 235))
            }
        }

        canvas.drawRect(0f, groundTop, w, h, groundPaint)
        canvas.drawRect(0f, groundTop, w, groundTop + 16f, groundBorderPaint)

        val spacing = 46f
        var x = -groundOffset
        paintDetail.color = getShadedColor(Color.argb(80, 0, 0, 0))
        while (x < w + spacing) {
            if (biomeIndex == 2) {
                canvas.drawLine(x, groundTop + 16f, x, h, paintDetail)
                canvas.drawLine(x, groundTop + 38f, x + spacing, groundTop + 38f, paintDetail)
            } else {
                canvas.drawLine(x, groundTop + 16f, x + 6f, groundTop + 6f, paintDetail)
            }
            x += spacing
        }
    }

    private fun getShadedColor(dayColor: Int): Int {
        if (nightFactor <= 0.02f) return dayColor
        val nightBlue = Color.rgb(15, 20, 38)
        return ColorUtils.blendARGB(dayColor, nightBlue, nightFactor * 0.78f)
    }

    private fun applyDayNightSky() {
        val p = dayNightPhase
        val seg = p.toInt() % 4
        val fraction = p - seg

        val skyTopColors = intArrayOf(
            Color.rgb(70, 175, 250),
            Color.rgb(245, 115, 60),
            Color.rgb(8, 14, 30),
            Color.rgb(85, 75, 140)
        )
        val skyBottomColors = intArrayOf(
            Color.rgb(205, 240, 255),
            Color.rgb(255, 205, 120),
            Color.rgb(22, 32, 60),
            Color.rgb(245, 180, 160)
        )

        val nextSeg = (seg + 1) % 4
        val currentSkyTop = ColorUtils.blendARGB(skyTopColors[seg], skyTopColors[nextSeg], fraction)
        val currentSkyBottom = ColorUtils.blendARGB(skyBottomColors[seg], skyBottomColors[nextSeg], fraction)

        if (currentSkyTop != lastSkyTopColor || currentSkyBottom != lastSkyBottomColor) {
            lastSkyTopColor = currentSkyTop
            lastSkyBottomColor = currentSkyBottom
            skyPaint.shader = LinearGradient(
                0f, 0f, 0f, groundTop,
                currentSkyTop, currentSkyBottom, Shader.TileMode.CLAMP
            )
        }

        val cloudAlpha = ((1f - nightFactor * 0.6f) * 255).toInt().coerceIn(60, 255)
        val cloudTint = ((1f - nightFactor * 0.7f) * 255).toInt().coerceIn(75, 255)
        cloudPaint.color = Color.argb(cloudAlpha, cloudTint, cloudTint, cloudTint)
    }

    private fun drawStars(canvas: Canvas) {
        stars.forEach { star ->
            val alpha = ((sin(starTwinkleTime + star.phaseOffset) * 0.35f + 0.65f) * nightFactor * 255).toInt().coerceIn(0, 255)
            starPaint.color = Color.argb(alpha, 255, 255, 240)
            canvas.drawCircle(star.x, star.y, star.size, starPaint)
        }
    }

    private fun drawCelestialBody(canvas: Canvas) {
        val cx = screenWidth * 0.80f
        val cy = screenHeight * 0.12f
        val baseRadius = screenWidth * 0.075f

        if (nightFactor > 0.45f) {
            val moonAlpha = ((nightFactor - 0.45f) / 0.55f * 255).toInt().coerceIn(0, 255)
            celestialPaint.color = Color.argb(moonAlpha, 255, 250, 220)

            moonCutoutPath.reset()
            moonCutoutPath.addCircle(cx, cy, baseRadius * 0.85f, Path.Direction.CW)
            moonCutPath.reset()
            moonCutPath.addCircle(cx + baseRadius * 0.45f, cy - baseRadius * 0.25f, baseRadius * 0.75f, Path.Direction.CW)
            moonCutoutPath.op(moonCutPath, Path.Op.DIFFERENCE)

            celestialPaint.alpha = moonAlpha / 5
            canvas.drawCircle(cx, cy, baseRadius * 1.25f, celestialPaint)
            celestialPaint.alpha = moonAlpha
            canvas.drawPath(moonCutoutPath, celestialPaint)
        } else {
            val sunAlpha = ((1f - nightFactor * 2f).coerceAtLeast(0f) * 255).toInt()
            val sunColor = if (dayNightPhase in 0.6f..1.6f) Color.rgb(255, 135, 45) else Color.rgb(255, 235, 130)
            celestialPaint.color = Color.argb(sunAlpha, Color.red(sunColor), Color.green(sunColor), Color.blue(sunColor))
            canvas.drawCircle(cx, cy, baseRadius, celestialPaint)
        }
    }

    private fun drawCloud(canvas: Canvas, x: Float, y: Float, scale: Float) {
        val r = 24f * scale
        canvas.drawCircle(x, y, r, cloudPaint)
        canvas.drawCircle(x + r, y - r * 0.45f, r * 1.15f, cloudPaint)
        canvas.drawCircle(x + r * 2.1f, y, r * 0.9f, cloudPaint)

        rectHelper.set(x - r * 0.6f, y, x + r * 2.7f, y + r * 0.85f)
        canvas.drawRoundRect(rectHelper, r * 0.4f, r * 0.4f, cloudPaint)
    }
}