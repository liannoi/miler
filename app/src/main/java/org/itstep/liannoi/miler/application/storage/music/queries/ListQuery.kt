package org.itstep.liannoi.miler.application.storage.music.queries

import org.itstep.liannoi.miler.application.storage.music.models.RawMusicModel

class ListQuery {
    interface Handler {
        fun onRawMusicFetchedSuccess(music: List<RawMusicModel>)

        // TODO: Replace with custom Exception.
        fun onRawMusicFetchedError(exception: String)
    }
}
