package ua.nure.holovashenko.vmptf_lb3_2dtanks.util

import android.content.Context
import android.media.MediaPlayer
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playRandomHitSound(context: Context) {
        mediaPlayer?.release()

        val hitSounds = listOf(
            R.raw.shoot,
            R.raw.shoot,
            R.raw.shoot,
            R.raw.shoot,
            R.raw.shoot,
            R.raw.shoot
        )

        val soundRes = hitSounds.random()

        mediaPlayer = MediaPlayer.create(context, soundRes)
        mediaPlayer?.start()
    }

    fun playShootSound(context: Context) {
        mediaPlayer?.release()

        val hitSounds = listOf(
            R.raw.domashniy,
            R.raw.hutor,
            R.raw.fufelyok,
            R.raw.myhi,
            R.raw.vaflya,
            R.raw.vtykalovo
        )

        val soundRes = hitSounds.random()

        mediaPlayer = MediaPlayer.create(context, soundRes)
        mediaPlayer?.start()
    }
}