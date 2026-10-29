package com.example.flappynext.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.flappynext.player.Player
import com.example.flappynext.player.PlayerCharacter

class CharacterSelectScreen(context: GameContext) : BaseScreen(context) {

    private val prevButton = RectF()
    private val nextButton = RectF()
    private val backButton = RectF()
    private var previewPlayer: Player? = null

    init {
        setupButtons(context.screenWidth, context.screenHeight)
        updatePreview()
    }

    override fun onResize(width: Int, height: Int) {
        setupButtons(width, height)
        updatePreview()
    }

    private fun setupButtons(w: Int, h: Int) {
        val cx = w / 2f
        val cy = h / 2f
        val btnW = w * 0.65f
        val btnH = 80f

        prevButton.set(cx - 180f, cy - 20f, cx - 80f, cy + 80f)
        nextButton.set(cx + 80f, cy - 20f, cx + 180f, cy + 80f)
        backButton.set(cx - btnW / 2f, cy + 220f, cx + btnW / 2f, cy + 220f + btnH)
    }

    private fun updatePreview() {
        previewPlayer = Player(context.screenWidth, context.screenHeight, context.selectedCharacter)
    }

    private fun getAvailableCharacters(): List<PlayerCharacter> {
        val list = mutableListOf(
            PlayerCharacter.CLASSIC,
            PlayerCharacter.BLUE,
            PlayerCharacter.RED
        )
        if (context.skinManager.isPurchased("fire")) list.add(PlayerCharacter.FIRE)
        if (context.skinManager.isPurchased("diamond")) list.add(PlayerCharacter.DIAMOND)
        if (context.skinManager.isPurchased("royal")) list.add(PlayerCharacter.ROYAL)
        return list
    }

    override fun update() {
        context.background.update(0f, isPlaying = false, currentScore = 0)
    }

    override fun draw(canvas: Canvas) {
        context.background.draw(canvas)
        drawOverlay(canvas)

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        textPaint.textSize = 54f
        canvas.drawText("ВЫБОР ПЕРСОНАЖА", context.screenWidth / 2f, context.screenHeight * 0.22f, textPaint)

        previewPlayer?.let { p ->
            p.x = context.screenWidth / 2f
            p.y = context.screenHeight * 0.43f
            p.draw(canvas)
        }

        textPaint.textSize = 40f
        canvas.drawText(context.selectedCharacter.name, context.screenWidth / 2f, context.screenHeight * 0.58f, textPaint)

        val avail = getAvailableCharacters()
        textPaint.textSize = 28f
        canvas.drawText("${context.selectedCharacterIndex + 1} / ${avail.size}", context.screenWidth / 2f, context.screenHeight * 0.64f, textPaint)

        drawButton(canvas, prevButton, "<", textSize = 42f)
        drawButton(canvas, nextButton, ">", textSize = 42f)
        drawButton(canvas, backButton, "НАЗАД", textSize = 30f)
    }

    override fun onTouch(x: Float, y: Float): Boolean {
        val avail = getAvailableCharacters()
        when {
            prevButton.contains(x, y) -> {
                context.selectedCharacterIndex = (context.selectedCharacterIndex - 1 + avail.size) % avail.size
                selectChar(avail[context.selectedCharacterIndex])
            }
            nextButton.contains(x, y) -> {
                context.selectedCharacterIndex = (context.selectedCharacterIndex + 1) % avail.size
                selectChar(avail[context.selectedCharacterIndex])
            }
            backButton.contains(x, y) -> {
                context.switchScreen(MenuScreen(context, onStartGame = { context.switchScreen(PlayingScreen(context)) }))
            }
        }
        return true
    }

    private fun selectChar(char: PlayerCharacter) {
        context.selectedCharacter = char
        context.preferences.edit().putString("selected_character", char.id).apply()
        updatePreview()
    }
}