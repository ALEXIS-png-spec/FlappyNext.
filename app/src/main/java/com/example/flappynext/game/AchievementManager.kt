package com.example.flappynext.game

import android.content.SharedPreferences

data class AchievementInfo(
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

class AchievementManager(
    private val preferences: SharedPreferences,
    private val coinManager: CoinManager
) {

    fun getAchievements(): List<AchievementInfo> {

        val totalObstacles = preferences.getInt(
            "achievement_total_obstacles",
            0
        )

        val totalScore = preferences.getInt(
            "achievement_total_score",
            0
        )

        val totalRounds = preferences.getInt(
            "achievement_total_rounds",
            0
        )

        val maxCoins = coinManager.coins

        return listOf(
            AchievementInfo(
                id = "first_flight",
                title = "Первый полёт",
                description = "Пройди первое препятствие",
                target = 1,
                progress = totalObstacles.coerceAtMost(1),
                reward = 10,
                isClaimed = isClaimed("first_flight")
            ),

            AchievementInfo(
                id = "experienced_pilot",
                title = "Опытный пилот",
                description = "Набери 25 очков за всё время",
                target = 25,
                progress = totalScore.coerceAtMost(25),
                reward = 20,
                isClaimed = isClaimed("experienced_pilot")
            ),

            AchievementInfo(
                id = "ace",
                title = "Ас",
                description = "Набери 100 очков за всё время",
                target = 100,
                progress = totalScore.coerceAtMost(100),
                reward = 50,
                isClaimed = isClaimed("ace")
            ),

            AchievementInfo(
                id = "wealth",
                title = "Богатство",
                description = "Накопи 100 монет",
                target = 100,
                progress = maxCoins.coerceAtMost(100),
                reward = 30,
                isClaimed = isClaimed("wealth")
            ),

            AchievementInfo(
                id = "veteran",
                title = "Ветеран",
                description = "Заверши 25 раундов",
                target = 25,
                progress = totalRounds.coerceAtMost(25),
                reward = 40,
                isClaimed = isClaimed("veteran")
            ),

            AchievementInfo(
                id = "legend",
                title = "Легенда",
                description = "Набери 500 очков за всё время",
                target = 500,
                progress = totalScore.coerceAtMost(500),
                reward = 100,
                isClaimed = isClaimed("legend")
            )
        )
    }

    fun onObstaclePassed() {
        val obstacles = preferences.getInt(
            "achievement_total_obstacles",
            0
        )

        preferences.edit()
            .putInt(
                "achievement_total_obstacles",
                obstacles + 1
            )
            .apply()
    }

    fun onScoreGained() {
        val totalScore = preferences.getInt(
            "achievement_total_score",
            0
        )

        preferences.edit()
            .putInt(
                "achievement_total_score",
                totalScore + 1
            )
            .apply()
    }

    fun onRoundFinished() {
        val rounds = preferences.getInt(
            "achievement_total_rounds",
            0
        )

        preferences.edit()
            .putInt(
                "achievement_total_rounds",
                rounds + 1
            )
            .apply()
    }

    fun claimReward(achievementId: String): Int {
        val achievement = getAchievements().find {
            it.id == achievementId
        } ?: return 0

        if (!achievement.isCompleted || achievement.isClaimed) {
            return 0
        }

        preferences.edit()
            .putBoolean(
                "achievement_claimed_$achievementId",
                true
            )
            .apply()

        coinManager.addCoins(achievement.reward)

        return achievement.reward
    }

    private fun isClaimed(achievementId: String): Boolean {
        return preferences.getBoolean(
            "achievement_claimed_$achievementId",
            false
        )
    }
}