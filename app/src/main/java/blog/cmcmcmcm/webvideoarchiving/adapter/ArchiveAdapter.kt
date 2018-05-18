package blog.cmcmcmcm.webvideoarchiving.adapter

import android.content.Context
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.common.BaseRealmRecyclerViewAdapter
import blog.cmcmcmcm.webvideoarchiving.data.Video
import blog.cmcmcmcm.webvideoarchiving.databinding.ItemArchiveBinding
import im.ene.toro.ToroPlayer
import im.ene.toro.ToroUtil
import im.ene.toro.exoplayer.ExoPlayerViewHelper
import im.ene.toro.media.PlaybackInfo
import im.ene.toro.widget.Container
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

        }
    }


    //ViewHolder class
    class ArchiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ToroPlayer {

        var helper: ExoPlayerViewHelper? = null
        val binding: ItemArchiveBinding? = DataBindingUtil.bind(itemView)


        override fun isPlaying(): Boolean {
           helper?.let {
               return it.isPlaying
           }
            return false
        }

        override fun getPlayerView(): View {
          return binding!!.playerItemArchive
        }

        override fun pause() {
            if (helper !=null) helper?.pause()
        }

        override fun wantsToPlay(): Boolean {
            return ToroUtil.visibleAreaOffset(this, itemView.parent) >= 0.85
        }

        override fun play() {
            if (helper !=null)  helper?.play()
        }

        override fun onSettled(container: Container?) {

        }

        override fun getCurrentPlaybackInfo(): PlaybackInfo {
            return helper?.latestPlaybackInfo ?: PlaybackInfo()
        }

        override fun release() {
            if (helper != null) {
                helper?.release()
                helper = null
            }
        }
        override fun initialize(container: Container, playbackInfo: PlaybackInfo?) {
            if (helper == null) {
                helper = ExoPlayerViewHelper(this, Uri.parse(binding?.video?.url))
            }
            helper?.initialize(container, playbackInfo)
        }

        override fun getPlayerOrder(): Int {
            return adapterPosition
        }


        fun getClickObservable(item: Video): Observable<Video> {
            return Observable.create { emitter ->
                itemView.setOnClickListener { _ ->

                    emitter.onNext(item)
                }
            }
        }
    }

}
