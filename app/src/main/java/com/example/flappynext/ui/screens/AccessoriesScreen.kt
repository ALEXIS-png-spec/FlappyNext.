package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

class AccessoriesScreen(context: GameContext) : BaseScreen(context) {

    private val previewRedButton = RectF()
    private val previewYellowButton = RectF()
    private val previewBlueButton = RectF()
    private val accessoryButtons = List(3) { RectF() }
    private val backButton = RectF()

    private var previewCharIndex = 1 // 0=red, 1=classic, 2=blue
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val helperPath = Path()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
    }

    private fun setupButtons(w: Int, h: Int) {
        val top = h * 0.24f
        val bot = top + 55f
        val bW = w * 0.25f

        previewRedButton.set(w * 0.08f, top, w * 0.08f + bW, bot)
        previewYellowButton.set(w * 0.375f, top, w * 0.375f + bW, bot)
        previewBlueButton.set(w * 0.67f, top, w * 0.67f + bW, bot)

        val margin = w * 0.08f
        val startY = h * 0.32f
        val cardH = h * 0.16f
        val gap = h * 0.025f

        for (i in 0 until 3) {
            val cY = startY + i * (cardH + gap)
            accessoryButtons[i].set(margin, cY + cardH - 65f, w - margin, cY + cardH - 10f)
        }

        backButton.set(w * 0.25f, h * 0.88f, w * 0.75f, h * 0.88f + 55f)
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 42f
        textPaint.color = Color.WHITE
        canvas.drawText("АКСЕССУАРЫ", context.screenWidth / 2f, context.screenHeight * 0.10f, textPaint)

        textPaint.textSize = 25f
        canvas.drawText("Монеты: ${context.coinManager.coins}", context.screenWidth / 2f, context.screenHeight * 0.17f, textPaint)

        drawButton(canvas, previewRedButton, "КРАСНЫЙ", textSize = 18f)
        drawButton(canvas, previewYellowButton, "ЖЁЛТЫЙ", textSize = 18f)
        drawButton(canvas, previewBlueButton, "СИНИЙ", textSize = 18f)

        val charId = when (previewCharIndex) {
            0 -> "red"
            1 -> "classic"
            else -> "blue"
        }
        val eqId = context.accessoryManager.getEquippedAccessory(charId)
        val eqName = context.accessoryManager.accessories.find { it.id == eqId }?.name

        textPaint.textSize = 21f
        textPaint.color = Color.rgb(255, 220, 100)
        canvas.drawText(
            if (eqName != null) "Надето: $eqName" else "Аксессуары не выбраны",
            context.screenWidth / 2f, context.screenHeight * 0.215f, textPaint
        )

        val items = context.accessoryManager.accessories
        for (i in items.indices) {
            val item = items[i]
            val btn = accessoryButtons[i]

            tempRect.set(btn.left, btn.top - 65f, btn.right, btn.bottom + 10f)
            cardFillPaint.color = Color.rgb(35, 48, 75)
            canvas.drawRoundRect(tempRect, 20f, 20f, cardFillPaint)
            cardBorderPaint.color = Color.rgb(85, 170, 255)
            canvas.drawRoundRect(tempRect, 20f, 20f, cardBorderPaint)

            drawAccessoryPreview(canvas, item.id, tempRect.right - 65f, tempRect.top + 45f)

            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 28f
            textPaint.color = Color.WHITE
            canvas.drawText(item.name, tempRect.left + 18f, tempRect.top + 35f, textPaint)

            textPaint.textSize = 22f
            canvas.drawText(
                if (context.accessoryManager.isPurchased(item.id)) "Куплено" else "Цена: ${item.price}",
                tempRect.left + 18f, tempRect.top + 65f, textPaint
            )

            val btnText = if (!context.accessoryManager.isPurchased(item.id)) {
                "КУПИТЬ"
            } else if (context.accessoryManager.getEquippedAccessory(charId) == item.id) {
                "СНЯТЬ"
            } else {
                "НАДЕТЬ"
            }
            drawButton(canvas, btn, btnText, textSize = 22f)
        }

        drawButton(canvas, backButton, "НАЗАД", textSize = 26f)
    }

    private fun drawAccessoryPreview(canvas: Canvas, accId: String, cx: Float, cy: Float) {
        val bColor = when (previewCharIndex) {
            0 -> Color.rgb(244, 67, 54)
            1 -> Color.rgb(255, 214, 0)
            else -> Color.rgb(33, 150, 243)
        }

        drawPaint.style = Paint.Style.FILL
        drawPaint.color = bColor
        canvas.drawCircle(cx, cy + 15f, 38f, drawPaint)

        drawPaint.color = Color.WHITE
        canvas.drawCircle(cx + 13f, cy + 5f, 11f, drawPaint)
        drawPaint.color = Color.BLACK
        canvas.drawCircle(cx + 16f, cy + 5f, 5f, drawPaint)

        drawPaint.color = Color.rgb(255, 152, 0)
        helperPath.reset()
        helperPath.moveTo(cx + 28f, cy + 17f)
        helperPath.lineTo(cx + 49f, cy + 24f)
        helperPath.lineTo(cx + 28f, cy + 31f)
        helperPath.close()
        canvas.drawPath(helperPath, drawPaint)

        when (accId) {
            "crown" -> {
                drawPaint.color = Color.rgb(255, 193, 7)
                helperPath.reset()
                helperPath.moveTo(cx - 28f, cy - 15f)
                helperPath.lineTo(cx - 25f, cy - 45f)
                helperPath.lineTo(cx - 10f, cy - 30f)
                helperPath.lineTo(cx, cy - 52f)
                helperPath.lineTo(cx + 10f, cy - 30f)
                helperPath.lineTo(cx + 25f, cy - 45f)
                helperPath.lineTo(cx + 28f, cy - 15f)
                helperPath.close()
                canvas.drawPath(helperPath, drawPaint)
            }
            "glasses" -> {
                drawPaint.color = Color.BLACK
                drawPaint.style = Paint.Style.STROKE
                drawPaint.strokeWidth = 5f
                tempRect.set(cx - 5f, cy - 3f, cx + 20f, cy + 15f)
                canvas.drawRoundRect(tempRect, 5f, 5f, drawPaint)
                canvas.drawLine(cx - 5f, cy + 3f, cx - 15f, cy + 1f, drawPaint)
            }
            "cap" -> {
                drawPaint.color = Color.rgb(244, 67, 54)
                helperPath.reset()
                helperPath.moveTo(cx - 32f, cy - 12f)
                helperPath.quadTo(cx - 22f, cy - 48f, cx + 12f, cy - 35f)
                helperPath.quadTo(cx + 27f, cy - 28f, cx + 29f, cy - 12f)
                helperPath.close()
                canvas.drawPath(helperPath, drawPaint)
                tempRect.set(cx + 5f, cy - 16f, cx + 38f, cy - 7f)
                canvas.drawOval(tempRect, drawPaint)
            }
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        when {
            previewRedButton.contains(x, y) -> previewCharIndex = 0
            previewYellowButton.contains(x, y) -> previewCharIndex = 1
            previewBlueButton.contains(x, y) -> previewCharIndex = 2
            backButton.contains(x, y) -> context.switchScreen(StoreScreen(context))
        }

        val charId = when (previewCharIndex) {
            0 -> "red"
            1 -> "classic"
            else -> "blue"
        }
        val items = context.accessoryManager.accessories
        accessoryButtons.forEachIndexed { i, btn ->
            if (btn.contains(x, y)) {
                val accId = items[i].id
                if (!context.accessoryManager.isPurchased(accId)) {
                    context.accessoryManager.buyAccessory(accId)
                } else {
                    val eq = context.accessoryManager.getEquippedAccessory(charId)
                    if (eq == accId) context.accessoryManager.removeAccessory(charId)
                    else context.accessoryManager.equipAccessory(charId, accId)
                }
            }
        }
        return true
    }
}