
package com.example.flappynext.player

enum class BirdCharacter(
    val displayName: String,
    val bodyColor: Int,
    val wingColor: Int,
    val highlightColor: Int,
    val outlineColor: Int
) {
    YELLOW(
        "Жёлтая",
        android.graphics.Color.rgb(255, 211, 55),
        android.graphics.Color.rgb(245, 165, 30),
        android.graphics.Color.rgb(255, 239, 145),
        android.graphics.Color.rgb(210, 135, 25)
    ),

    BLUE(
        "Синяя",
        android.graphics.Color.rgb(60, 160, 255),
        android.graphics.Color.rgb(30, 105, 220),
        android.graphics.Color.rgb(170, 220, 255),
        android.graphics.Color.rgb(25, 75, 165)
    ),

    RED(
        "Красная",
        android.graphics.Color.rgb(245, 75, 75),
        android.graphics.Color.rgb(190, 35, 45),
        android.graphics.Color.rgb(255, 165, 155),
        android.graphics.Color.rgb(145, 25, 35)
    )
}
