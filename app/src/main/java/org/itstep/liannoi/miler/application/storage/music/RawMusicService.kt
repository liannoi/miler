package org.itstep.liannoi.miler.application.storage.music

import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.itstep.liannoi.miler.R
import org.itstep.liannoi.miler.application.storage.music.models.RawMusicModel
import org.itstep.liannoi.miler.application.storage.music.queries.ListQuery
import org.itstep.liannoi.miler.infrastructure.presentation.ResourceRecognizer

class RawMusicService private constructor() {
    private val disposable: CompositeDisposable = CompositeDisposable()

    fun getAll(query: ListQuery, handler: ListQuery.Handler, recognizer: ResourceRecognizer) {
        val result = ArrayList<RawMusicModel>()
        val fields = R.raw::class.java.fields

        fields.indices
            .forEach {
                val name: String = fields[it].name
                result.add(RawMusicModel(recognizer.recognize(name), name))
            }

        Maybe.just(result)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { handler.onRawMusicFetchedSuccess(it) },
                { handler.onRawMusicFetchedError(it.message.toString()) })
            .follow()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dispose
    ///////////////////////////////////////////////////////////////////////////

    fun stop() {
        disposable.clear()
    }

    fun destroy() {
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
        private var INSTANCE: RawMusicService? = null

        fun getInstance(): RawMusicService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build().also { INSTANCE = it }
            }

        private fun build(): RawMusicService = RawMusicService()
    }
}
