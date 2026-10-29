package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.flappynext.game.SkinInfo
import com.example.flappynext.player.Player
import com.example.flappynext.player.PlayerCharacter

class SkinsScreen(context: GameContext) : BaseScreen(context) {

    private val skinButtons = List(3) { RectF() }
    private val backButton = RectF()
    private val previewPlayers = mutableMapOf<String, Player>()

    init {
        setupButtons(context.screenWidth, context.screenHeight)
        context.skinManager.skins.forEach { s ->
            val char = when (s.id) {
                "fire" -> PlayerCharacter.FIRE
                "diamond" -> PlayerCharacter.DIAMOND
                "royal" -> PlayerCharacter.ROYAL
                else -> PlayerCharacter.CLASSIC
            }
            previewPlayers[s.id] = Player(context.screenWidth, context.screenHeight, char)
        }
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
    }

    private fun setupButtons(w: Int, h: Int) {
        skinButtons[0].set(w * 0.12f, h * 0.30f, w * 0.88f, h * 0.42f)
        skinButtons[1].set(w * 0.12f, h * 0.45f, w * 0.88f, h * 0.57f)
        skinButtons[2].set(w * 0.12f, h * 0.60f, w * 0.88f, h * 0.72f)
        backButton.set(w * 0.25f, h * 0.80f, w * 0.75f, h * 0.90f)
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        textPaint.textSize = 48f
        canvas.drawText("СКИНЫ", context.screenWidth / 2f, context.screenHeight * 0.20f, textPaint)

        context.skinManager.skins.forEachIndexed { i, s ->
            drawSkinCard(canvas, skinButtons[i], s)
        }

        drawButton(canvas, backButton, "НАЗАД", textSize = 28f)
    }

    private fun drawSkinCard(canvas: Canvas, button: RectF, skin: SkinInfo) {
        cardFillPaint.color = Color.rgb(35, 45, 70)
        canvas.drawRoundRect(button, 20f, 20f, cardFillPaint)
        cardBorderPaint.color = Color.rgb(100, 120, 160)
        canvas.drawRoundRect(button, 20f, 20f, cardBorderPaint)

        previewPlayers[skin.id]?.let { p ->
            p.x = button.left + button.width() * 0.20f
            p.y = button.centerY()
            p.draw(canvas)
        }

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.WHITE
        textPaint.textSize = 27f
        canvas.drawText(skin.name, button.left + button.width() * 0.36f, button.top + 38f, textPaint)

        textPaint.textSize = 21f
        if (context.skinManager.isPurchased(skin.id)) {
            textPaint.color = Color.rgb(100, 230, 120)
            canvas.drawText("КУПЛЕНО", button.left + button.width() * 0.36f, button.bottom - 25f, textPaint)
        } else {
            textPaint.color = Color.rgb(255, 210, 70)
            canvas.drawText("${skin.price} 🪙  •  КУПИТЬ", button.left + button.width() * 0.36f, button.bottom - 25f, textPaint)
        }
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        if (backButton.contains(x, y)) {
            context.switchScreen(StoreScreen(context))
            return true
        }

        val ids = listOf("fire", "diamond", "royal")
        skinButtons.forEachIndexed { i, btn ->
            if (btn.contains(x, y)) {
                context.skinManager.buySkin(ids[i])
            }
        }
        return true
    }
}