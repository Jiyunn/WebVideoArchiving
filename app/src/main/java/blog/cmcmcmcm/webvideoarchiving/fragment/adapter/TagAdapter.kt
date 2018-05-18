package blog.cmcmcmcm.webvideoarchiving.fragment.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.common.BaseRealmRecyclerViewAdapter
import blog.cmcmcmcm.webvideoarchiving.data.Tag
import blog.cmcmcmcm.webvideoarchiving.databinding.ItemTagBinding
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class TagAdapter: BaseRealmRecyclerViewAdapter<Tag, TagAdapter.TagViewHolder>() {

    val clickSubject: PublishSubject<Tag> = PublishSubject.create()
    val longClickSubject: PublishSubject<Tag> = PublishSubject.create()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = getItem(position)

        tag?.let {
            holder.binding?.tag = it
            holder.getObservable(it).subscribe(clickSubject)
            holder.getLongClickObservable(it).subscribe(longClickSubject)
        }
    }


    class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemTagBinding? = DataBindingUtil.bind(itemView)

        fun getObservable(tag: Tag): Observable<Tag> {
            return Observable.create { emitter ->
                itemView.setOnClickListener {
                    emitter.onNext(tag)
                }
            }
        }

        fun getLongClickObservable(tag: Tag): Observable<Tag> {
            return Observable.create { emitter ->
                itemView.setOnLongClickListener {
                    emitter.onNext(tag)
                    true
                }
            }
        }
    }
}