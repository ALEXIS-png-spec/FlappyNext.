package com.example.flappynext.game

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DailyQuestInfo(
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

class DailyQuestManager(
    private val preferences: SharedPreferences,
    private val coinManager: CoinManager
) {

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    )

    private fun getToday(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun prepareToday() {
        val savedDate = preferences.getString(
            "daily_quests_date",
            ""
        )

        if (savedDate != getToday()) {
            preferences.edit()
                .putString("daily_quests_date", getToday())
                .putInt("daily_quest_obstacles", 0)
                .putInt("daily_quest_rounds", 0)
                .putInt("daily_quest_best_score", 0)
                // Исправлены имена ключей сброса на актуальные идентификаторы квестов
                .putBoolean("daily_quest_claimed_daily_obstacles", false)
                .putBoolean("daily_quest_claimed_daily_rounds", false)
                .putBoolean("daily_quest_claimed_daily_score", false)
                .apply()
        }
    }

    fun getQuests(): List<DailyQuestInfo> {
        prepareToday()

        return listOf(
            DailyQuestInfo(
                id = "daily_obstacles",
                title = "Воздушный маршрут",
                description = "Пройди 10 препятствий",
                target = 10,
                progress = preferences.getInt(
                    "daily_quest_obstacles",
                    0
                ).coerceAtMost(10),
                reward = 15,
                isClaimed = isClaimed("daily_obstacles")
            ),

            DailyQuestInfo(
                id = "daily_rounds",
                title = "Не сдавайся",
                description = "Заверши 3 раунда",
                target = 3,
                progress = preferences.getInt(
                    "daily_quest_rounds",
                    0
                ).coerceAtMost(3),
                reward = 20,
                isClaimed = isClaimed("daily_rounds")
            ),

            DailyQuestInfo(
                id = "daily_score",
                title = "Сегодняшний рекорд",
                description = "Набери 10 очков за один раунд",
                target = 10,
                progress = preferences.getInt(
                    "daily_quest_best_score",
                    0
                ).coerceAtMost(10),
                reward = 25,
                isClaimed = isClaimed("daily_score")
            )
        )
    }

    fun onObstaclePassed() {
        prepareToday()

        val current = preferences.getInt(
            "daily_quest_obstacles",
            0
        )

        preferences.edit()
            .putInt(
                "daily_quest_obstacles",
                (current + 1).coerceAtMost(10)
            )
            .apply()
    }

    fun onRoundFinished(score: Int) {
        prepareToday()

        val rounds = preferences.getInt(
            "daily_quest_rounds",
            0
        )

        val bestScore = preferences.getInt(
            "daily_quest_best_score",
            0
        )

        preferences.edit()
            .putInt(
                "daily_quest_rounds",
                (rounds + 1).coerceAtMost(3)
            )
            .putInt(
                "daily_quest_best_score",
                maxOf(bestScore, score).coerceAtMost(10)
            )
            .apply()
    }

    fun claimReward(questId: String): Int {
        prepareToday()

        val quest = getQuests().find {
            it.id == questId
        } ?: return 0

        if (!quest.isCompleted || quest.isClaimed) {
            return 0
        }

        preferences.edit()
            .putBoolean(
                "daily_quest_claimed_$questId",
                true
            )
            .apply()

        coinManager.addCoins(quest.reward)

        return quest.reward
    }

    private fun isClaimed(questId: String): Boolean {
        return preferences.getBoolean(
            "daily_quest_claimed_$questId",
            false
        )
    }
}