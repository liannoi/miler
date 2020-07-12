package org.itstep.liannoi.miler.infrastructure

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.itstep.liannoi.miler.application.common.MusicPlayerFacade
import org.itstep.liannoi.miler.application.storage.music.models.RawMusicModel

class MusicPlayer private constructor(
    private val context: Context,
    private val music: List<RawMusicModel>,
    private val seekTime: Int,
    private val audioManager: AudioManager
) : MusicPlayerFacade {

    data class Details(
        val length: String,
        val isLooping: Boolean,
        val volume: Int
    )

    interface Handler {
        fun onPlayingStartedSuccess(model: RawMusicModel)
        fun onPlayingPausedSuccess()
        fun onPlayingContinuedSuccess()
    }

    private var mediaPlayer: MediaPlayer? = null
    private val disposable: CompositeDisposable = CompositeDisposable()
    private lateinit var current: RawMusicModel

    override fun playNext(handler: Handler) {
        var result: RawMusicModel? = null

        Observable.fromIterable(music.withIndex())
            .filter { it.value.compositionId == current.compositionId }
            .map {
                result = when (it.index + 1) {
                    music.size -> music[0]
                    else -> music[it.index + 1]
                }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { play(result as RawMusicModel, handler) }
            .follow()
    }

    override fun playPrevious(handler: Handler) {
        var result: RawMusicModel? = null

        Observable.fromIterable(music.withIndex())
            .filter { it.value.compositionId == current.compositionId }
            .map {
                result = when (it.index - 1) {
                    -1 -> music[music.size - 1]
                    else -> music[it.index - 1]
                }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { play(result as RawMusicModel, handler) }
            .follow()
    }

    override fun play(model: RawMusicModel, handler: Handler) {
        Completable.fromAction {
            current = model
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, model.compositionId).also { it.start() }
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { handler.onPlayingStartedSuccess(model) }
            .follow()
    }

    override fun pause(handler: Handler) {
        Completable.fromAction { mediaPlayer?.pause() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { handler.onPlayingPausedSuccess() }
            .follow()
    }

    override fun pauseOrContinue(handler: Handler) {
        mediaPlayer?.also {
            when {
                it.isPlaying -> pause(handler)
                else -> playContinue(handler)
            }
        }
    }

    override fun back() {
        mediaPlayer?.also { it.seekTo(it.currentPosition - seekTime) }
    }

    override fun forward() {
        mediaPlayer?.also { it.seekTo(it.currentPosition + seekTime) }
    }

    override fun info(): Details {
        var result: Details? = null

        mediaPlayer?.also {
            val length = "${it.currentPosition}/${it.duration}"

            result = Details(
                length,
                it.isLooping,
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            )
        }

        return result as Details
    }

    override fun loop() {
        mediaPlayer?.also { it.isLooping = !it.isLooping }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private fun playContinue(handler: Handler) {
        Completable.fromAction { mediaPlayer?.start() }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { handler.onPlayingContinuedSuccess() }
            .follow()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dispose
    ///////////////////////////////////////////////////////////////////////////

    override fun stop() {
        disposable.clear()
    }

    override fun destroy() {
        disposable.dispose()
    }

    private fun Disposable.follow() {
        disposable.add(this)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Singleton
    ///////////////////////////////////////////////////////////////////////////

    companion object {
        @Volatile
        private var INSTANCE: MusicPlayerFacade? = null

        fun getInstance(
            context: Context,
            music: List<RawMusicModel>,
            seekTime: Int,
            audioManager: AudioManager
        ): MusicPlayerFacade =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context, music, seekTime, audioManager).also { INSTANCE = it }
            }

        private fun build(
            context: Context,
            music: List<RawMusicModel>,
            seekTime: Int,
            audioManager: AudioManager
        ): MusicPlayerFacade = MusicPlayer(context, music, seekTime, audioManager)
    }
}
