package com.example.flappynext.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {

    var isEnabled = true

    private val sampleRate = 22050
    private val executor = Executors.newSingleThreadExecutor()

    private val jumpTrack: AudioTrack = createStaticTrack(generateJumpSound())
    private val scoreTrack: AudioTrack = createStaticTrack(generateCoinSound())
    private val collisionTrack: AudioTrack = createStaticTrack(generateExplosionSound())
    private val shieldTrack: AudioTrack = createStaticTrack(generateShieldBreakSound())

    private fun createStaticTrack(data: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(data.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(data, 0, data.size)
        return track
    }

    private fun playTrack(track: AudioTrack) {
        if (!isEnabled) return
        executor.execute {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.reloadStaticData()
                track.play()
            } catch (_: Exception) {}
        }
    }

    private fun generateJumpSound(): ShortArray {
        val count = (sampleRate * 0.09).toInt()
        val b = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val p = i.toDouble() / count
            val f = 520.0 - 260.0 * p
            phase += 2.0 * PI * f / sampleRate
            b[i] = (sin(phase) * (1.0 - p) * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        return b
    }

    private fun generateCoinSound(): ShortArray {
        val count = (sampleRate * 0.12).toInt()
        val b = ShortArray(count)
        val half = count / 2
        var phase = 0.0
        for (i in 0 until count) {
            val f = if (i < half) 987.0 else 1318.0
            val p = (i % half).toDouble() / half
            phase += 2.0 * PI * f / sampleRate
            val sq = if (sin(phase) >= 0) 0.5 else -0.5
            b[i] = (sq * (1.0 - p) * Short.MAX_VALUE * 0.3).toInt().toShort()
        }
        return b
    }

    private fun generateExplosionSound(): ShortArray {
        val count = (sampleRate * 0.20).toInt()
        val b = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val p = i.toDouble() / count
            phase += 2.0 * PI * (120.0 * (1.0 - p * 0.7)) / sampleRate
            val n = (Math.random() * 2.0 - 1.0) * 0.4
            b[i] = ((sin(phase) * 0.5 + n) * (1.0 - p) * Short.MAX_VALUE * 0.5).toInt().toShort()
        }
        return b
    }

    private fun generateShieldBreakSound(): ShortArray {
        val count = (sampleRate * 0.18).toInt()
        val b = ShortArray(count)
        var p1 = 0.0
        for (i in 0 until count) {
            val p = i.toDouble() / count
            p1 += 2.0 * PI * (800.0 * (1.0 - p * 0.5)) / sampleRate
            b[i] = (sin(p1) * (1.0 - p) * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        return b
    }

    fun playJump() = playTrack(jumpTrack)
    fun playScore() = playTrack(scoreTrack)
    fun playCollision() = playTrack(collisionTrack)
    fun playShieldBreak() = playTrack(shieldTrack)

    fun release() {
        try {
            executor.shutdownNow()
            jumpTrack.release()
            scoreTrack.release()
            collisionTrack.release()
            shieldTrack.release()
        } catch (_: Exception) {}
    }
}