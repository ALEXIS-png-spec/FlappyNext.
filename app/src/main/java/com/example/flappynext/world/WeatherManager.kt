package com.example.flappynext.world

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class WeatherType {
    CLEAR,
    RAIN,
    WINDY
}

class WeatherManager(
    private var screenWidth: Int,
    private var screenHeight: Int
) {
    var currentWeather: WeatherType = WeatherType.CLEAR
        private set

    var windSpeedModifier: Float = 0f
        private set

    private class RainDrop(
        var x: Float,
        var y: Float,
        var length: Float,
        var speedY: Float,
        var thickness: Float,
        var alpha: Int
    )

    private class Splash(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var alpha: Float
    )

    private val rainPoolSize = 90
    private val rainDrops = Array(rainPoolSize) {
        RainDrop(0f, 0f, 0f, 0f, 0f, 0)
    }

    private val splashPoolSize = 35
    private val splashes = Array(splashPoolSize) {
        Splash(0f, 0f, 0f, 0f, 3f, 0f)
    }

    private class Firefly(
        var x: Float,
        var y: Float,
        var baseRadius: Float,
        var seed: Float,
        var speedX: Float,
        var speedY: Float
    )

    private val fireflyCount = 22
    private val fireflies = Array(fireflyCount) {
        Firefly(0f, 0f, 0f, 0f, 0f, 0f)
    }

    private class WindGust(
        var startX: Float,
        var startY: Float,
        var length: Float,
        var speed: Float,
        var waveAmplitude: Float,
        var active: Boolean
    )

    private val windGusts = Array(4) {
        WindGust(-500f, 0f, 380f, 24f, 40f, false)
    }

    private val rainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    private val splashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val fireflyCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val fireflyGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val windPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(160, 255, 255, 255)
        strokeWidth = 5.5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val windPath = Path()
    private var weatherTimer = 0
    private var windGustTimer = 0

    init {
        distributeEntities()
    }

    fun resize(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return
        screenWidth = w
        screenHeight = h
        distributeEntities()
    }

    private fun distributeEntities() {
        val w = screenWidth.toFloat()
        val h = screenHeight.toFloat()

        for (drop in rainDrops) {
            val isForeground = Random.nextBoolean()
            drop.x = Random.nextFloat() * (w + 400f) - 200f
            drop.y = Random.nextFloat() * h
            drop.length = if (isForeground) Random.nextFloat() * 20f + 35f else Random.nextFloat() * 15f + 20f
            drop.speedY = if (isForeground) Random.nextFloat() * 10f + 30f else Random.nextFloat() * 8f + 22f
            drop.thickness = if (isForeground) 5f else 3f
            drop.alpha = if (isForeground) 230 else 140
        }

        for (f in fireflies) {
            f.x = Random.nextFloat() * w
            f.y = Random.nextFloat() * (h * 0.80f)
            f.baseRadius = Random.nextFloat() * 3f + 3f
            f.seed = Random.nextFloat() * 100f
            f.speedX = (Random.nextFloat() - 0.5f) * 1.3f
            f.speedY = (Random.nextFloat() - 0.5f) * 1.0f
        }
    }

    fun update(score: Int, groundTop: Float, nightFactor: Float) {
        weatherTimer++

        if (weatherTimer > 500) {
            weatherTimer = 0
            val roll = Random.nextFloat()
            currentWeather = when {
                roll < 0.45f -> WeatherType.CLEAR
                roll < 0.75f -> WeatherType.RAIN
                else -> WeatherType.WINDY
            }

            if (currentWeather != WeatherType.WINDY) {
                windSpeedModifier = 0f
                for (g in windGusts) g.active = false
            }
        }

        when (currentWeather) {
            WeatherType.RAIN -> updateRain(groundTop)
            WeatherType.WINDY -> updateWind()
            WeatherType.CLEAR -> {
                windSpeedModifier = 0f
            }
        }

        if (nightFactor > 0.45f) {
            updateFireflies(groundTop)
        }
    }

    private fun updateRain(groundTop: Float) {
        val w = screenWidth.toFloat()
        windSpeedModifier = 0f

        for (drop in rainDrops) {
            drop.x -= 4f
            drop.y += drop.speedY

            if (drop.y >= groundTop) {
                spawnSplash(drop.x, groundTop)
                drop.y = -Random.nextFloat() * 70f
                drop.x = Random.nextFloat() * (w + 300f) - 150f
            }

            if (drop.x < -150f) {
                drop.x = w + 150f
            }
        }

        for (s in splashes) {
            if (s.alpha > 0f) {
                s.x += s.vx
                s.y += s.vy
                s.vy += 0.6f
                s.alpha -= 0.07f
            }
        }
    }

    private fun spawnSplash(x: Float, y: Float) {
        var count = 0
        for (s in splashes) {
            if (s.alpha <= 0f) {
                s.x = x
                s.y = y
                s.vx = (Random.nextFloat() - 0.5f) * 6f
                s.vy = -Random.nextFloat() * 4.5f - 2f
                s.radius = Random.nextFloat() * 2.5f + 2f
                s.alpha = 1f
                count++
                if (count >= 2) break
            }
        }
    }

    private fun updateWind() {
        val w = screenWidth.toFloat()
        val h = screenHeight.toFloat()

        windSpeedModifier = 2.2f
        windGustTimer++

        if (windGustTimer % 40 == 0) {
            val gust = windGusts.firstOrNull { !it.active }
            gust?.let {
                it.active = true
                it.startX = -it.length
                it.startY = Random.nextFloat() * (h * 0.65f) + (h * 0.15f)
                it.speed = Random.nextFloat() * 12f + 24f
                it.waveAmplitude = (Random.nextFloat() * 25f + 20f) * (if (Random.nextBoolean()) 1 else -1)
            }
        }

        for (g in windGusts) {
            if (g.active) {
                g.startX += g.speed
                if (g.startX > w + 200f) {
                    g.active = false
                }
            }
        }
    }

    private fun updateFireflies(groundTop: Float) {
        val w = screenWidth.toFloat()
        val h = screenHeight.toFloat()
        for (f in fireflies) {
            f.seed += 0.05f
            f.x += f.speedX + sin(f.seed.toDouble()).toFloat() * 1.3f
            f.y += f.speedY + cos((f.seed * 0.75f).toDouble()).toFloat() * 1.0f

            if (f.x < -30f) f.x = w + 30f
            if (f.x > w + 30f) f.x = -30f
            if (f.y < h * 0.1f) f.y = h * 0.15f
            if (f.y > groundTop - 20f) f.y = groundTop - 30f
        }
    }

    fun draw(canvas: Canvas, groundTop: Float, nightFactor: Float) {
        if (currentWeather == WeatherType.WINDY) {
            for (g in windGusts) {
                if (g.active) {
                    windPath.reset()
                    windPath.moveTo(g.startX, g.startY)
                    windPath.cubicTo(
                        g.startX + g.length * 0.35f, g.startY - g.waveAmplitude,
                        g.startX + g.length * 0.70f, g.startY + g.waveAmplitude * 0.8f,
                        g.startX + g.length, g.startY
                    )
                    canvas.drawPath(windPath, windPaint)
                }
            }
        }

        if (currentWeather == WeatherType.RAIN) {
            for (drop in rainDrops) {
                rainPaint.strokeWidth = drop.thickness
                rainPaint.color = Color.argb(drop.alpha, 220, 240, 255)
                canvas.drawLine(
                    drop.x, drop.y,
                    drop.x - 8f, drop.y + drop.length,
                    rainPaint
                )
            }

            for (s in splashes) {
                if (s.alpha > 0f) {
                    splashPaint.color = Color.argb((s.alpha * 220).toInt().coerceIn(0, 255), 230, 245, 255)
                    canvas.drawCircle(s.x, s.y, s.radius, splashPaint)
                }
            }
        }

        if (nightFactor > 0.45f) {
            val nightAlphaMultiplier = ((nightFactor - 0.45f) / 0.55f).coerceIn(0f, 1f)

            for (f in fireflies) {
                val pulse = (sin(f.seed.toDouble()).toFloat() * 0.35f + 0.65f) * nightAlphaMultiplier

                val glowAlpha = (pulse * 150).toInt().coerceIn(0, 255)
                val coreAlpha = (pulse * 255).toInt().coerceIn(0, 255)

                fireflyGlowPaint.color = Color.argb(glowAlpha, 190, 255, 70)
                canvas.drawCircle(f.x, f.y, f.baseRadius * 3.5f, fireflyGlowPaint)

                fireflyCorePaint.color = Color.argb(coreAlpha, 250, 255, 190)
                canvas.drawCircle(f.x, f.y, f.baseRadius, fireflyCorePaint)
            }
        }
    }
}