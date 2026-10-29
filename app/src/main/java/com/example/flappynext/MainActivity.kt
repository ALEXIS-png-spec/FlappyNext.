package com.example.flappynext

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.flappynext.game.GameView
import android.app.Activity
import com.example.flappynext.updater.UpdateManager
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(GameView(this))

        val updateManager = UpdateManager(
            context = this,
            githubUser = "твой_ник_на_github",
            githubRepo = "FlappyNext"
        )
        updateManager.checkForUpdates()
    }
}