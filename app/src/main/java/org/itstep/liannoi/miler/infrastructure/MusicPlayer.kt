package org.itstep.liannoi.miler.infrastructure

import android.content.Context
import android.media.MediaPlayer
import org.itstep.liannoi.miler.application.common.interfaces.MusicPlayerFacade

class MusicPlayer private constructor(private val context: Context) :
    MusicPlayerFacade {

    private var mediaPlayer: MediaPlayer? = null

    override fun play(resource: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, resource).also { it.start() }
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun playOrPause() {
        mediaPlayer?.also {
            when {
                it.isPlaying -> it.pause()
                else -> it.start()
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Singleton
    ///////////////////////////////////////////////////////////////////////////

    companion object {
        @Volatile
        private var INSTANCE: MusicPlayerFacade? = null

        fun getInstance(context: Context): MusicPlayerFacade =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): MusicPlayerFacade = MusicPlayer(context)
    }
}
