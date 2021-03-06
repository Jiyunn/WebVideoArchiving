package blog.cmcmcmcm.webvideoarchiving.fragment.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.common.adapter.BaseRealmRecyclerViewAdapter
import blog.cmcmcmcm.webvideoarchiving.common.player.JyPlayer
import blog.cmcmcmcm.webvideoarchiving.common.player.JyPlayerHelper
import blog.cmcmcmcm.webvideoarchiving.data.Video
import blog.cmcmcmcm.webvideoarchiving.databinding.ItemArchiveBinding
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

    override fun onViewDetachedFromWindow(holder: ArchiveViewHolder) {
        holder.release()
    }

    override fun onViewAttachedToWindow(holder: ArchiveViewHolder) {
        holder.binding?.video?.let {
            holder.initPlayer(it, context)
        }
    }

    //ViewHolder class
    class ArchiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), JyPlayer {

        val binding: ItemArchiveBinding? = DataBindingUtil.bind(itemView)
        private var playerHelper: JyPlayerHelper? = null

        //initialize player
        fun initPlayer(video: Video, context: Context?) {
            Log.d("PlayVideo", "initPlayer")
            playerHelper = JyPlayerHelper(context, video, binding?.playerItemArchive)
        }

        override fun prepare() {
            playerHelper?.preparePlayer()
        }

        override fun play() {
            playerHelper?.playVideo()
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
                        it.releasePlayer()
                    }
                    emitter.onNext(item)
                }
            }
        }
    }
}