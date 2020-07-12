package org.itstep.liannoi.miler.presentation.music

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import com.jakewharton.rxbinding4.view.clicks
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_main.*
import org.itstep.liannoi.miler.R
import org.itstep.liannoi.miler.application.common.MusicPlayerFacade
import org.itstep.liannoi.miler.application.storage.music.RawMusicService
import org.itstep.liannoi.miler.application.storage.music.models.RawMusicModel
import org.itstep.liannoi.miler.application.storage.music.queries.ListQuery
import org.itstep.liannoi.miler.infrastructure.InfrastructureDefaults
import org.itstep.liannoi.miler.infrastructure.MusicPlayer
import org.itstep.liannoi.miler.infrastructure.presentation.ResourceRecognizer
import org.itstep.liannoi.miler.presentation.AbstractActivity

class MainActivity : AbstractActivity(),
    ListQuery.Handler,
    ResourceRecognizer,
    MusicPlayer.Handler {

    private val rawMusicService: RawMusicService
        get() = RawMusicService.getInstance()

    private val musicPlayer: MusicPlayerFacade
        get() = MusicPlayer.getInstance(
            this,
            music,
            InfrastructureDefaults.MUSIC_SEEK_MILLISECONDS,
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        )

    private lateinit var music: List<RawMusicModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catchErrors()

        music_raw_playlist.layout(1)
        rawMusicService.getAll(ListQuery(), this, this)

        subscribeAction()
        subscribeBack()
        subscribeForward()
        subscribeInfo()
        subscribeLoop()
        subscribeNext()
        subscribePrevious()
    }

    ///////////////////////////////////////////////////////////////////////////
    // ListQuery.Handler
    ///////////////////////////////////////////////////////////////////////////

    override fun onRawMusicFetchedSuccess(music: List<RawMusicModel>) {
        this.music = music
        music_raw_playlist.adapter = RawMusicListCardAdapter(music, musicPlayer, this)
    }

    override fun onRawMusicFetchedError(exception: String) {
        processException(exception, "onRawMusicFetchedError: ")
    }

    ///////////////////////////////////////////////////////////////////////////
    // MusicPlayer.Handler
    ///////////////////////////////////////////////////////////////////////////

    override fun onPlayingStartedSuccess(model: RawMusicModel) {
        music_playing_title.text = model.name
        music_action_button.text = "Pause"
        music_action_button.isEnabled = true
        music_back_button.isEnabled = true
        music_forward_button.isEnabled = true
        music_info_button.isEnabled = true
        music_loop_check.isEnabled = true
    }

    override fun onPlayingPausedSuccess() {
        music_action_button.text = "Play"
    }

    override fun onPlayingContinuedSuccess() {
        music_action_button.text = "Pause"
    }

    ///////////////////////////////////////////////////////////////////////////
    // ResourceRecognizer
    ///////////////////////////////////////////////////////////////////////////

    override fun recognize(name: String): Int = rawIdentifier(name)

    ///////////////////////////////////////////////////////////////////////////
    // Subscriptions
    ///////////////////////////////////////////////////////////////////////////

    private fun subscribeAction() {
        music_action_button.clicks()
            .bindToLifecycle(this)
            .subscribe { musicPlayer.pauseOrContinue(this) }
    }

    private fun subscribeBack() {
        music_back_button.clicks()
            .bindToLifecycle(this)
            .subscribe { musicPlayer.back() }
    }

    private fun subscribeForward() {
        music_forward_button.clicks()
            .bindToLifecycle(this)
            .subscribe { musicPlayer.forward() }
    }

    private fun subscribeInfo() {
        music_info_button.clicks()
            .bindToLifecycle(this)
            .subscribe { showToast(musicPlayer.info().toString()) }
    }

    private fun subscribeLoop() {
        music_loop_check.clicks()
            .bindToLifecycle(this)
            .subscribe { musicPlayer.loop() }
    }

    private fun subscribeNext() {
        music_next_button.clicks()
            .bindToLifecycle(this)
            .subscribe { musicPlayer.playNext(this) }
    }

    private fun subscribePrevious() {
        music_previous_button.clicks()
            .bindToLifecycle(this)
            .subscribe { musicPlayer.playPrevious(this) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dispose
    ///////////////////////////////////////////////////////////////////////////

    override fun onStop() {
        super.onStop()
        rawMusicService.stop()
        musicPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        rawMusicService.destroy()
        musicPlayer.destroy()
    }
}
