package com.example.flappynext.game

import android.content.SharedPreferences

class CoinManager(
    private val preferences: SharedPreferences
) {
    var coins: Int = preferences.getInt("coins", 0)
        private set

    fun addCoins(amount: Int = 1) {
        // Не разрешаем уходить в минус.
        if (coins + amount < 0) return

        coins += amount

        preferences.edit()
            .putInt("coins", coins)
            .apply()
    }
}