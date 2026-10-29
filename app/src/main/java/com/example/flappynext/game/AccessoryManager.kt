
package com.example.flappynext.game

import android.content.SharedPreferences

data class AccessoryInfo(
    val id: String,
    val name: String,
    val price: Int
)

class AccessoryManager(
    private val preferences: SharedPreferences,
    private val coinManager: CoinManager
) {

    val accessories = listOf(
        AccessoryInfo(
            id = "crown",
            name = "Корона",
            price = 100
        ),
        AccessoryInfo(
            id = "glasses",
            name = "Очки",
            price = 75
        ),
        AccessoryInfo(
            id = "cap",
            name = "Кепка",
            price = 50
        )
    )

    fun isPurchased(accessoryId: String): Boolean {
        return preferences.getBoolean(
            "accessory_purchased_$accessoryId",
            false
        )
    }

    fun buyAccessory(accessoryId: String): Boolean {
        val accessory = accessories.find {
            it.id == accessoryId
        } ?: return false

        // Уже купленный предмет повторно не покупаем.
        if (isPurchased(accessoryId)) {
            return false
        }

        // Проверяем, хватает ли монет.
        if (coinManager.coins < accessory.price) {
            return false
        }

        // Списываем монеты.
        coinManager.addCoins(-accessory.price)

        // Сохраняем покупку.
        preferences.edit()
            .putBoolean(
                "accessory_purchased_$accessoryId",
                true
            )
            .apply()

        return true
    }

    fun getEquippedAccessory(characterId: String): String {
        return preferences.getString(
            "equipped_accessory_$characterId",
            ""
        ) ?: ""
    }

    fun equipAccessory(
        characterId: String,
        accessoryId: String
    ): Boolean {
        if (!isPurchased(accessoryId)) {
            return false
        }

        preferences.edit()
            .putString(
                "equipped_accessory_$characterId",
                accessoryId
            )
            .apply()

        return true
    }

    fun removeAccessory(characterId: String) {
        preferences.edit()
            .remove("equipped_accessory_$characterId")
            .apply()
    }

}
