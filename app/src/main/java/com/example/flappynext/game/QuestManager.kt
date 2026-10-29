
package com.example.flappynext.game

import android.content.SharedPreferences

data class QuestInfo(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val progress: Int,
    val reward: Int,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean
        get() = progress >= target
}

class QuestManager(
    private val preferences: SharedPreferences,
    private val coinManager: CoinManager
) {
    fun getQuests(): List<QuestInfo> {
        val obstacles = preferences.getInt("quest_obstacles", 0)
        val bestRoundScore = preferences.getInt("quest_best_round_score", 0)
        val rounds = preferences.getInt("quest_rounds", 0)

        return listOf(
            QuestInfo(
                id = "novice",
                title = "Новичок",
                description = "Пройди 5 препятствий",
                target = 5,
                progress = obstacles.coerceAtMost(5),
                reward = 10,
                isClaimed = isClaimed("novice")
            ),
            QuestInfo(
                id = "pilot",
                title = "Меткий пилот",
                description = "Набери 15 очков за один раунд",
                target = 15,
                progress = bestRoundScore.coerceAtMost(15),
                reward = 20,
                isClaimed = isClaimed("pilot")
            ),
            QuestInfo(
                id = "persistent",
                title = "Упорный",
                description = "Заверши 3 раунда",
                target = 3,
                progress = rounds.coerceAtMost(3),
                reward = 15,
                isClaimed = isClaimed("persistent")
            )
        )
    }

    fun onObstaclePassed(scoreInCurrentRound: Int) {
        val obstacles = preferences.getInt("quest_obstacles", 0)

        preferences.edit()
            .putInt(
                "quest_obstacles",
                (obstacles + 1).coerceAtMost(5)
            )
            .putInt(
                "quest_best_round_score",
                maxOf(
                    preferences.getInt("quest_best_round_score", 0),
                    scoreInCurrentRound
                ).coerceAtMost(15)
            )
            .apply()
    }

    fun onRoundFinished() {
        val rounds = preferences.getInt("quest_rounds", 0)

        preferences.edit()
            .putInt("quest_rounds", (rounds + 1).coerceAtMost(3))
            .apply()
    }

    fun claimReward(questId: String): Int {
        val quest = getQuests().find { it.id == questId }
            ?: return 0

        if (!quest.isCompleted || quest.isClaimed) {
            return 0
        }

        // Сохраняем получение до начисления валюты,
        // чтобы нельзя было повторно забрать награду.
        preferences.edit()
            .putBoolean("quest_claimed_$questId", true)
            .apply()

        coinManager.addCoins(quest.reward)

        return quest.reward
    }

    private fun isClaimed(questId: String): Boolean {
        return preferences.getBoolean(
            "quest_claimed_$questId",
            false
        )
    }
}
