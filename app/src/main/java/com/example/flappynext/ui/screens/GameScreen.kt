package com.example.flappynext.ui.screens

import android.content.SharedPreferences
import android.graphics.Canvas
import com.example.flappynext.audio.SoundManager
import com.example.flappynext.game.AccessoryManager
import com.example.flappynext.game.AchievementManager
import com.example.flappynext.game.CoinManager
import com.example.flappynext.game.DailyQuestManager
import com.example.flappynext.game.DailyRewardManager
import com.example.flappynext.game.LevelManager
import com.example.flappynext.game.QuestManager
import com.example.flappynext.game.SkinManager
import com.example.flappynext.player.PlayerCharacter
import com.example.flappynext.world.Background

interface GameContext {
    val screenWidth: Int
    val screenHeight: Int
    val preferences: SharedPreferences
    val soundManager: SoundManager
    val coinManager: CoinManager
    val weatherManager: com.example.flappynext.world.WeatherManager
    val levelManager: LevelManager
    val skinManager: SkinManager
    val accessoryManager: AccessoryManager
    val questManager: QuestManager
    val dailyQuestManager: DailyQuestManager
    val dailyRewardManager: DailyRewardManager
    val achievementManager: AchievementManager
    val background: Background

    var bestScore: Int
    var selectedCharacter: PlayerCharacter
    var selectedCharacterIndex: Int
    fun startPlaying()
    fun switchScreen(screen: GameScreen)
    fun addPlayerXp(amount: Int)
    fun showLevelUpMessage(message: String)
}

interface GameScreen {
    fun onResize(width: Int, height: Int) {}
    fun update() {}
    fun draw(canvas: Canvas)
    fun onTouch(x: Float, y: Float): Boolean = false
}