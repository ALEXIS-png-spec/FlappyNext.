
package com.example.flappynext.player

import android.graphics.Color

data class PlayerCharacter(
    val id: String,
    val name: String,
    val bodyColor: Int,
    val wingColor: Int,
    val highlightColor: Int,
    val outlineColor: Int,
    val beakColor: Int
) {
    companion object {

        val CLASSIC = PlayerCharacter(
            id = "classic",
            name = "Классика",
            bodyColor = Color.rgb(255, 211, 55),
            wingColor = Color.rgb(245, 165, 30),
            highlightColor = Color.rgb(255, 239, 145),
            outlineColor = Color.rgb(210, 135, 25),
            beakColor = Color.rgb(255, 125, 35)
        )

        val BLUE = PlayerCharacter(
            id = "blue",
            name = "Синяя птица",
            bodyColor = Color.rgb(60, 160, 255),
            wingColor = Color.rgb(30, 110, 220),
            highlightColor = Color.rgb(180, 225, 255),
            outlineColor = Color.rgb(20, 75, 160),
            beakColor = Color.rgb(255, 170, 45)
        )

        val RED = PlayerCharacter(
            id = "red",
            name = "Огненная птица",
            bodyColor = Color.rgb(245, 80, 75),
            wingColor = Color.rgb(190, 45, 55),
            highlightColor = Color.rgb(255, 175, 135),
            outlineColor = Color.rgb(135, 25, 40),
            beakColor = Color.rgb(255, 180, 35)
        )

        val FIRE = PlayerCharacter(
            id = "fire",
            name = "Огненный",
            bodyColor = Color.rgb(255, 90, 25),
            wingColor = Color.rgb(220, 45, 15),
            highlightColor = Color.rgb(255, 190, 70),
            outlineColor = Color.rgb(150, 35, 10),
            beakColor = Color.rgb(255, 210, 40)
        )

        val DIAMOND = PlayerCharacter(
            id = "diamond",
            name = "Алмазный",
            bodyColor = Color.rgb(90, 220, 255),
            wingColor = Color.rgb(45, 170, 220),
            highlightColor = Color.rgb(210, 250, 255),
            outlineColor = Color.rgb(25, 110, 160),
            beakColor = Color.rgb(255, 200, 50)
        )

        val ROYAL = PlayerCharacter(
            id = "royal",
            name = "Королевский",
            bodyColor = Color.rgb(150, 80, 220),
            wingColor = Color.rgb(105, 45, 175),
            highlightColor = Color.rgb(220, 170, 255),
            outlineColor = Color.rgb(65, 25, 110),
            beakColor = Color.rgb(255, 205, 50)
        )

        val ALL = listOf(
            CLASSIC,
            BLUE,
            RED
        )
    }
}
