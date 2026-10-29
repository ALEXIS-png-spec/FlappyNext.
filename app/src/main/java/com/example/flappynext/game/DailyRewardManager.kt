
package com.example.flappynext.game

import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyRewardManager(
    private val preferences: SharedPreferences,
    private val coinManager: CoinManager
) {
    private val rewards = listOf(10, 15, 20, 25, 30, 40, 60)

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    )

    private fun getDate(offsetDays: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offsetDays)
        return dateFormat.format(calendar.time)
    }

    fun canClaimToday(): Boolean {
        val lastClaimDate = preferences.getString(
            "daily_last_claim_date",
            ""
        )

        return lastClaimDate != getDate()
    }

    fun getNextRewardDay(): Int {
        val lastClaimDate = preferences.getString(
            "daily_last_claim_date",
            ""
        )

        val lastRewardDay = preferences.getInt(
            "daily_streak_day",
            0
        )

        return when {
            lastClaimDate == getDate() ->
                lastRewardDay.coerceIn(1, rewards.size)

            lastClaimDate == getDate(-1) ->
                (lastRewardDay % rewards.size) + 1

            else -> 1
        }
    }

    fun getRewardForDay(day: Int): Int {
        return rewards.getOrElse(day - 1) { 0 }
    }

    fun getRewardAmount(): Int {
        return getRewardForDay(getNextRewardDay())
    }

    fun hasClaimedDay(day: Int): Boolean {
        if (day !in 1..rewards.size) return false

        val lastClaimDate = preferences.getString(
            "daily_last_claim_date",
            ""
        )

        val lastRewardDay = preferences.getInt(
            "daily_streak_day",
            0
        )

        if (lastClaimDate == getDate()) {
            return day <= lastRewardDay
        }

        if (lastClaimDate == getDate(-1)) {
            return day < getNextRewardDay()
        }

        return false
    }

    fun claimToday(): Int {
        if (!canClaimToday()) return 0

        val rewardDay = getNextRewardDay()
        val reward = getRewardForDay(rewardDay)

        if (reward <= 0) return 0

        coinManager.addCoins(reward)

        preferences.edit()
            .putString("daily_last_claim_date", getDate())
            .putInt("daily_streak_day", rewardDay)
            .apply()

        return reward
    }
}
