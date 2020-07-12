package org.itstep.liannoi.miler.presentation.music

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.adapter_card_raw_music_list.view.*
import org.itstep.liannoi.miler.R
import org.itstep.liannoi.miler.application.common.MusicPlayerFacade
import org.itstep.liannoi.miler.application.storage.music.models.RawMusicModel
import org.itstep.liannoi.miler.infrastructure.MusicPlayer

class RawMusicListCardAdapter(
    private val music: List<RawMusicModel>,
    private val musicPlayer: MusicPlayerFacade,
    private val handler: MusicPlayer.Handler
) : RecyclerView.Adapter<RawMusicListCardAdapter.ViewHolder>() {

    class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        class Builder(
            private val context: Context,
            private val resource: Int,
            private val container: ViewGroup
        ) {

            private var attachToRoot: Boolean = false

            fun attachToRoot(): Builder {
                this.attachToRoot = true
                return this
            }

            fun create(): ViewHolder =
                ViewHolder(LayoutInflater.from(context).inflate(resource, container, attachToRoot))
        }

        val musicTitleText: TextView = container.music_title_text
        val cardView: CardView = container.card_view
    }

    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.Builder(container.context, R.layout.adapter_card_raw_music_list, container)
            .create()

    override fun getItemCount(): Int {
        return music.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: RawMusicModel = music[position]
        holder.musicTitleText.text = model.name
        subscribeStarting(holder, model)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        destroy()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private fun subscribeStarting(holder: ViewHolder, model: RawMusicModel) {
        holder.cardView
            .clicks()
            .subscribe({ musicPlayer.play(model, handler) }, { processException(holder, it) })
            .follow()
    }

    private fun processException(holder: ViewHolder, exception: Throwable) {
        Toast.makeText(holder.itemView.context, exception.message.toString(), Toast.LENGTH_LONG)
            .show()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dispose
    ///////////////////////////////////////////////////////////////////////////

    private fun destroy() {
        disposable.dispose()
    }

    private fun Disposable.follow() {
        disposable.add(this)
    }
}
