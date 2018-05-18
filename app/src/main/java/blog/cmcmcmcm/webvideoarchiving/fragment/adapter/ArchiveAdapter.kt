package me.ljy.archiving.archive.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.common.BaseRealmRecyclerViewAdapter
import blog.cmcmcmcm.webvideoarchiving.data.Video
import blog.cmcmcmcm.webvideoarchiving.databinding.ItemArchiveBinding
import blog.cmcmcmcm.webvideoarchiving.util.player.JyPlayer
import blog.cmcmcmcm.webvideoarchiving.util.player.JyPlayerHelper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ArchiveAdapter(val context: Context?) : BaseRealmRecyclerViewAdapter<Video, ArchiveAdapter.ArchiveViewHolder>() {

    val clickSubject: PublishSubject<Video> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.item_archive, parent, false)

        return ArchiveViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        val video = getItem(position)

        video?.let {
            holder.binding?.video = it
            holder.getClickObservable(it).subscribe(clickSubject)
            holder.initPlayer(it, context)
        }
    }

    override fun onViewAttachedToWindow(holder: ArchiveViewHolder) {
        holder.prepare()
    }

    override fun onViewDetachedFromWindow(holder: ArchiveViewHolder) {
        holder.stop()
    }

    //ViewHolder class
    class ArchiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), JyPlayer {

        val binding: ItemArchiveBinding? = DataBindingUtil.bind(itemView)
        var playerHelper: JyPlayerHelper? = null


        fun initPlayer(video: Video, context: Context?) { //초기화 해줌.
            playerHelper = JyPlayerHelper(context, video, binding?.playerItemArchive)
        }

        override fun prepare() {
            playerHelper?.preparePlayer()
        }

        override fun stop() {
            playerHelper?.stopPlayer()
        }

        override fun release() {
            playerHelper?.releasePlayer()
            playerHelper = null
        }


        fun getClickObservable(item: Video): Observable<Video> {
            return Observable.create { emitter ->
                itemView.setOnClickListener { _ ->
                    playerHelper?.let {
                        it.stopPlayer() //재생 중이면 플레이를 멈춤,
                        it.preparePlayer()
                    }
                    emitter.onNext(item)
                }
            }
        }
    }

}