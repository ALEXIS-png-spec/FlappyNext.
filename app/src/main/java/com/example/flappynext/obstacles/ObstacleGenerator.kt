package com.example.flappynext.obstacles

// Замени пути пакетов на твои, если они лежат в другой папке:
import com.example.flappynext.game.CollectibleCoin
import com.example.flappynext.game.PowerUp
import com.example.flappynext.game.PowerUpType
import kotlin.random.Random

class ObstacleGenerator(
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    private var lastGapY: Float = screenHeight * 0.48f

    fun createObstacle(
        currentSpeed: Float,
        score: Int,
        biomeIndex: Int = 0
    ): Obstacle {
        val gapSize = (380f - (score / 4) * 3f).coerceAtLeast(270f)
        val minGapY = screenHeight * 0.28f
        val maxGapY = screenHeight * 0.65f

        val maxDeltaY = 110f
        val targetMin = (lastGapY - maxDeltaY).coerceAtLeast(minGapY)
        val targetMax = (lastGapY + maxDeltaY).coerceAtMost(maxGapY)

        val gapY = if (targetMax > targetMin) {
            Random.nextFloat() * (targetMax - targetMin) + targetMin
        } else {
            targetMin
        }

        lastGapY = gapY

        val obs = Obstacle(
            x = screenWidth.toFloat() + 50f,
            gapY = gapY,
            gapSize = gapSize,
            width = 125f,
            screenHeight = screenHeight,
            biomeIndex = biomeIndex
        )
        obs.setSpeed(currentSpeed)
        return obs
    }

    fun getHorizontalSpacing(currentSpeed: Float): Float {
        return (screenWidth * 0.62f).coerceAtLeast(420f)
    }

    fun maybeCreateCoin(spawnY: Float, spawnX: Float): CollectibleCoin? {
        return if (Random.nextFloat() < 0.45f) {
            CollectibleCoin(spawnX, spawnY)
        } else {
            null
        }
    }

    fun maybeCreatePowerUp(spawnY: Float, spawnX: Float): PowerUp? {
        return if (Random.nextFloat() < 0.12f) {
            val randomType = PowerUpType.values().random()
            PowerUp(spawnX, spawnY, randomType)
        } else {
            null
        }
    }

    fun reset() {
        lastGapY = screenHeight * 0.48f
    }
}