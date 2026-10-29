
package com.example.flappynext.game

import android.content.SharedPreferences

class LevelManager(
    private val preferences: SharedPreferences
) {
    companion object {
        private const val KEY_LEVEL = "player_level"
        private const val KEY_XP = "player_xp"
    }

    var level: Int = preferences.getInt(KEY_LEVEL, 1)
        private set

    var xp: Int = preferences.getInt(KEY_XP, 0)
        private set

    // Сколько опыта требуется для следующего уровня.
    fun xpForNextLevel(): Int {
        return 100 + (level - 1) * 50
    }

    // Опыт, полученный в пределах текущего уровня.
    fun currentLevelXp(): Int {
        return xp
    }

    // Начисление опыта с автоматическим повышением уровня.
    fun addXp(amount: Int): Int {
        if (amount <= 0) return 0

        val oldLevel = level
        xp += amount

        while (xp >= xpForNextLevel()) {
            xp -= xpForNextLevel()
            level++
        }

        saveProgress()

        return level - oldLevel
    }

    private fun saveProgress() {
        preferences.edit()
            .putInt(KEY_LEVEL, level)
            .putInt(KEY_XP, xp)
            .apply()
    }

    // Прогресс от 0 до 1 для полоски опыта.
    fun levelProgress(): Float {
        return (xp.toFloat() / xpForNextLevel())
            .coerceIn(0f, 1f)
    }
}
