package com.example.flappynext.game

class GameLoop(
    private val update: () -> Unit,
    private val render: () -> Unit
) : Thread() {

    @Volatile
    private var isRunning = false
    private val targetFps = 60
    private val targetTime = 1000L / targetFps

    fun startLoop() {
        if (!isRunning) {
            isRunning = true
            try {
                start()
            } catch (_: IllegalThreadStateException) {}
        }
    }

    fun stopLoop() {
        isRunning = false
        try {
            join(500)
        } catch (_: InterruptedException) {}
    }

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long

        while (isRunning) {
            startTime = System.currentTimeMillis()

            try {
                update()
                render()
            } catch (_: Exception) {}

            timeMillis = System.currentTimeMillis() - startTime
            waitTime = targetTime - timeMillis

            if (waitTime > 0) {
                try {
                    sleep(waitTime)
                } catch (_: InterruptedException) {}
            }
        }
    }
}