package com.example.flappynext.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin

class Player(
    var x: Float,
    var y: Float
) {
    // Текущий выбранный персонаж/скин из PlayerCharacter
    var character: PlayerCharacter = PlayerCharacter.CLASSIC
    var currentAccessoryId: String = ""

    // Вторичные конструкторы под вызовы из PlayingScreen, CharacterSelectScreen и SkinsScreen
    constructor(screenWidth: Int, screenHeight: Int) : this(
        screenWidth * 0.28f,
        screenHeight * 0.45f
    )

    constructor(screenWidth: Int, screenHeight: Int, characterOrSkin: Any?) : this(
        screenWidth.toFloat() * 0.28f,
        screenHeight.toFloat() * 0.45f
    ) {
        when (characterOrSkin) {
            is PlayerCharacter -> this.character = characterOrSkin
            is String -> applyCharacterById(characterOrSkin)
        }
    }

    val visualRadius = 34f
    val collisionRadius = 24f

    private var velocityY = 0f
    private val gravity = 0.52f
    private val maxVelocityY = 11.5f

    var rotation = 0f
        private set
    private var targetRotation = 0f

    private var wingFlapTime = 0f
    private var isFlapping = false

    // Эффекты способностей
    var hasShieldVisual: Boolean = false
    var isBlinking: Boolean = false
    private var shieldPulseTime = 0f

    // Кисти персонажа
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val wingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bellyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val beakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val accessoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val eyePupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.rgb(25, 25, 35)
    }
    private val eyeGlintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // Кисти щита
    private val shieldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.argb(190, 80, 220, 255)
    }
    private val shieldGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(45, 80, 220, 255)
    }

    private val pathHelper = Path()
    private val rectHelper = RectF()

    fun flap() {
        velocityY = -10.5f
        targetRotation = -22f
        wingFlapTime = 0f
        isFlapping = true
    }

    fun update(groundTop: Float, timeScale: Float = 1.0f) {
        velocityY = (velocityY + gravity * timeScale).coerceAtMost(maxVelocityY)
        y += velocityY * timeScale

        if (velocityY > 2f) {
            targetRotation = (velocityY * 3.8f).coerceAtMost(65f)
        }
        rotation += (targetRotation - rotation) * 0.12f * timeScale

        wingFlapTime += 0.28f * timeScale
        if (wingFlapTime > Math.PI * 2) {
            isFlapping = false
        }

        shieldPulseTime += 0.08f

        if (y - visualRadius < 0f) {
            y = visualRadius
            velocityY = 0f
        }
    }

    fun applyCharacterById(charId: String) {
        character = when (charId.lowercase()) {
            "blue" -> PlayerCharacter.BLUE
            "red" -> PlayerCharacter.RED
            "fire" -> PlayerCharacter.FIRE
            "diamond" -> PlayerCharacter.DIAMOND
            "royal" -> PlayerCharacter.ROYAL
            else -> PlayerCharacter.CLASSIC
        }
    }

    fun draw(canvas: Canvas) {
        if (isBlinking) return

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)

        strokePaint.color = character.outlineColor

        // 1. Хвост птицы
        pathHelper.reset()
        pathHelper.moveTo(-visualRadius * 0.8f, -2f)
        pathHelper.lineTo(-visualRadius * 1.45f, -14f)
        pathHelper.lineTo(-visualRadius * 1.35f, 4f)
        pathHelper.lineTo(-visualRadius * 1.55f, 16f)
        pathHelper.lineTo(-visualRadius * 0.7f, 12f)
        pathHelper.close()

        bodyPaint.color = character.wingColor
        canvas.drawPath(pathHelper, bodyPaint)
        canvas.drawPath(pathHelper, strokePaint)

        // 2. Тело птицы (градиент выбранного персонажа)
        bodyPaint.shader = LinearGradient(
            0f, -visualRadius, 0f, visualRadius,
            character.highlightColor, character.bodyColor,
            Shader.TileMode.CLAMP
        )
        rectHelper.set(-visualRadius, -visualRadius * 0.9f, visualRadius, visualRadius * 0.9f)
        canvas.drawOval(rectHelper, bodyPaint)
        canvas.drawOval(rectHelper, strokePaint)
        bodyPaint.shader = null

        // 3. Брюшко
        bellyPaint.color = character.highlightColor
        rectHelper.set(-visualRadius * 0.35f, 2f, visualRadius * 0.75f, visualRadius * 0.85f)
        canvas.drawOval(rectHelper, bellyPaint)

        // 4. Хохолок на макушке
        pathHelper.reset()
        pathHelper.moveTo(-6f, -visualRadius * 0.85f)
        pathHelper.lineTo(-12f, -visualRadius * 1.38f)
        pathHelper.lineTo(4f, -visualRadius * 1.15f)
        pathHelper.lineTo(10f, -visualRadius * 1.42f)
        pathHelper.lineTo(14f, -visualRadius * 0.85f)
        pathHelper.close()
        bodyPaint.color = character.bodyColor
        canvas.drawPath(pathHelper, bodyPaint)
        canvas.drawPath(pathHelper, strokePaint)

        // 5. Клюв
        pathHelper.reset()
        pathHelper.moveTo(visualRadius * 0.65f, -8f)
        pathHelper.lineTo(visualRadius * 1.45f, 2f)
        pathHelper.lineTo(visualRadius * 0.65f, 14f)
        pathHelper.close()
        beakPaint.color = character.beakColor
        canvas.drawPath(pathHelper, beakPaint)
        canvas.drawPath(pathHelper, strokePaint)
        canvas.drawLine(visualRadius * 0.7f, 3f, visualRadius * 1.25f, 2f, strokePaint)

        // 6. Глаз
        val eyeCenterX = visualRadius * 0.36f
        val eyeCenterY = -visualRadius * 0.28f
        rectHelper.set(eyeCenterX - 11f, eyeCenterY - 15f, eyeCenterX + 11f, eyeCenterY + 15f)
        canvas.drawOval(rectHelper, eyeWhitePaint)
        canvas.drawOval(rectHelper, strokePaint)
        canvas.drawCircle(eyeCenterX + 3.5f, eyeCenterY, 6.5f, eyePupilPaint)
        canvas.drawCircle(eyeCenterX + 5f, eyeCenterY - 3.5f, 3f, eyeGlintPaint)
        canvas.drawCircle(eyeCenterX + 1f, eyeCenterY + 3f, 1.6f, eyeGlintPaint)

        // 7. Машущее крыло
        val wingWave = sin(wingFlapTime.toDouble()).toFloat()
        val wingAngle = if (isFlapping) wingWave * 32f - 10f else (velocityY * 1.8f).coerceIn(-18f, 28f)

        canvas.save()
        canvas.translate(-visualRadius * 0.15f, visualRadius * 0.05f)
        canvas.rotate(wingAngle)

        pathHelper.reset()
        pathHelper.moveTo(-16f, -12f)
        pathHelper.quadTo(18f, -16f, 16f, 4f)
        pathHelper.lineTo(6f, 20f)
        pathHelper.lineTo(-8f, 18f)
        pathHelper.lineTo(-20f, 6f)
        pathHelper.close()

        wingPaint.color = character.wingColor
        canvas.drawPath(pathHelper, wingPaint)
        canvas.drawPath(pathHelper, strokePaint)
        canvas.drawLine(-2f, 2f, 6f, 18f, strokePaint)
        canvas.drawLine(-10f, 0f, -6f, 16f, strokePaint)
        canvas.restore()

        // 8. Отрисовка аксессуара из AccessoryManager ("crown", "glasses", "cap")
        drawEquippedAccessory(canvas, eyeCenterX, eyeCenterY)

        // 9. Аура щита
        if (hasShieldVisual) {
            val pulse = sin(shieldPulseTime.toDouble()).toFloat() * 3.5f
            val shieldR = visualRadius * 1.45f + pulse
            canvas.drawCircle(0f, 0f, shieldR, shieldGlowPaint)
            canvas.drawCircle(0f, 0f, shieldR, shieldPaint)
        }

        canvas.restore()
    }

    private fun drawEquippedAccessory(canvas: Canvas, eyeX: Float, eyeY: Float) {
        when (currentAccessoryId.lowercase()) {
            "crown" -> { // Корона
                accessoryPaint.color = Color.rgb(255, 215, 0)
                pathHelper.reset()
                pathHelper.moveTo(-18f, -visualRadius * 0.82f)
                pathHelper.lineTo(-24f, -visualRadius * 1.35f)
                pathHelper.lineTo(-8f, -visualRadius * 1.05f)
                pathHelper.lineTo(4f, -visualRadius * 1.45f)
                pathHelper.lineTo(16f, -visualRadius * 1.05f)
                pathHelper.lineTo(24f, -visualRadius * 1.35f)
                pathHelper.lineTo(18f, -visualRadius * 0.82f)
                pathHelper.close()
                canvas.drawPath(pathHelper, accessoryPaint)
                canvas.drawPath(pathHelper, strokePaint)

                // Рубин по центру короны
                accessoryPaint.color = Color.rgb(230, 35, 45)
                canvas.drawCircle(4f, -visualRadius * 0.95f, 3.5f, accessoryPaint)
            }
            "glasses" -> { // Очки
                accessoryPaint.color = Color.rgb(25, 25, 30)
                rectHelper.set(eyeX - 14f, eyeY - 12f, eyeX + 14f, eyeY + 12f)
                canvas.drawRoundRect(rectHelper, 6f, 6f, accessoryPaint)
                canvas.drawRoundRect(rectHelper, 6f, 6f, strokePaint)
                // Дужка очков
                canvas.drawLine(eyeX - 14f, eyeY - 2f, -visualRadius * 0.6f, eyeY - 5f, strokePaint)
                // Блик на стекле
                accessoryPaint.color = Color.argb(180, 255, 255, 255)
                canvas.drawLine(eyeX - 6f, eyeY - 6f, eyeX + 6f, eyeY + 6f, accessoryPaint)
            }
            "cap" -> { // Кепка
                accessoryPaint.color = Color.rgb(220, 45, 45)
                rectHelper.set(-visualRadius * 0.55f, -visualRadius * 1.15f, visualRadius * 0.65f, -visualRadius * 0.65f)
                canvas.drawRoundRect(rectHelper, 12f, 12f, accessoryPaint)
                canvas.drawRoundRect(rectHelper, 12f, 12f, strokePaint)
                // Козырек
                pathHelper.reset()
                pathHelper.moveTo(visualRadius * 0.35f, -visualRadius * 0.72f)
                pathHelper.lineTo(visualRadius * 1.25f, -visualRadius * 0.60f)
                pathHelper.lineTo(visualRadius * 0.45f, -visualRadius * 0.85f)
                pathHelper.close()
                canvas.drawPath(pathHelper, accessoryPaint)
                canvas.drawPath(pathHelper, strokePaint)
            }
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        draw(canvas)
    }

    fun reset(startX: Float, startY: Float) {
        x = startX
        y = startY
        velocityY = 0f
        rotation = 0f
        targetRotation = 0f
        wingFlapTime = 0f
        isFlapping = false
        hasShieldVisual = false
        isBlinking = false
    }
}