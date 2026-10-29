package com.example.flappynext.game

import android.content.SharedPreferences

data class SkinInfo(
    val id: String,
    val name: String,
    val price: Int
)

class SkinManager(
    private val preferences: SharedPreferences,
    private val coinManager: CoinManager
) {

    val skins = listOf(
        SkinInfo("fire", "Огненный", 150),
        SkinInfo("diamond", "Алмазный", 250),
        SkinInfo("royal", "Королевский", 400)
    )

    fun isPurchased(skinId: String): Boolean {
        return preferences.getBoolean(
            "skin_purchased_$skinId",
            false
        )
    }

    fun buySkin(skinId: String): Boolean {
        if (isPurchased(skinId)) {
            return false
        }

        val skin = skins.find { it.id == skinId }
            ?: return false

        if (coinManager.coins < skin.price) {
            return false
        }

        coinManager.addCoins(-skin.price)

        preferences.edit()
            .putBoolean("skin_purchased_$skinId", true)
            .apply()

        return true
    }
}