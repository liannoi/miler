package org.itstep.liannoi.miler.presentation.music

import android.os.Bundle
import com.jakewharton.rxbinding4.view.clicks
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_main.*
import org.itstep.liannoi.miler.R
import org.itstep.liannoi.miler.application.common.interfaces.ResourceRecognizer
import org.itstep.liannoi.miler.application.storage.music.RawMusicService
import org.itstep.liannoi.miler.application.storage.music.models.RawMusicModel
import org.itstep.liannoi.miler.application.storage.music.queries.ListQuery
import org.itstep.liannoi.miler.infrastructure.MusicPlayer
import org.itstep.liannoi.miler.presentation.AbstractActivity

class MainActivity : AbstractActivity(),
    ListQuery.Handler,
    ResourceRecognizer {

    private val rawMusicService: RawMusicService
        get() = RawMusicService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catchErrors()
        music_raw_playlist.layout(1)
        rawMusicService.getAll(ListQuery(), this, this)
        subscribeAction()
    }

    ///////////////////////////////////////////////////////////////////////////
    // ListQuery.Handler
    ///////////////////////////////////////////////////////////////////////////

    override fun onRawMusicFetchedSuccess(music: List<RawMusicModel>) {
        music_raw_playlist.adapter = RawMusicListCardAdapter(music)
    }

    override fun onRawMusicFetchedError(exception: String) {
        processException(exception, "onRawMusicFetchedError: ")
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    override fun recognize(name: String): Int = rawIdentifier(name)

    private fun subscribeAction() {
        music_action_button.clicks()
            .bindToLifecycle(this)
            .subscribe { MusicPlayer.getInstance(this).playOrPause() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dispose
    ///////////////////////////////////////////////////////////////////////////

    override fun onStop() {
        super.onStop()
        rawMusicService.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        rawMusicService.destroy()
    }
}
