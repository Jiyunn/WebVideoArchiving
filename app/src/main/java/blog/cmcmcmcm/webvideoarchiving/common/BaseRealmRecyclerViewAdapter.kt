package blog.cmcmcmcm.webvideoarchiving.common

import android.support.v7.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults

abstract class BaseRealmRecyclerViewAdapter<T : RealmModel, S : RecyclerView.ViewHolder> : RecyclerView.Adapter<S>() {


    private var itemList: OrderedRealmCollection<T>? = null

    //update items
    fun updateItems(data: OrderedRealmCollection<T>?) {
        if (itemList != null) {
            itemList?.clear()
        }
        addListener(data)

        itemList = data
    }

    fun addItems(data: OrderedRealmCollection<T>) {
        itemList?.let {
            addListener(data)
            it.addAll(data)
        }
    }


    private fun addListener(data: OrderedRealmCollection<T>?) {
        if (data is RealmResults) {
            data.addChangeListener { _, changeSet ->
                //change Listener 초기화
                notifyDataSetChanged()

                // For deletions, the adapter has to be notified in reverse order.
                val deletions = changeSet.deletionRanges
                for (range in deletions.reversedArray()) {
                    notifyItemRangeChanged(range.startIndex, range.length)
                }

                val insertions = changeSet.insertionRanges
                for (range in insertions.iterator()) {
                    notifyItemRangeInserted(range.startIndex, range.length)
                }

                val modifications = changeSet.changeRanges
                for (range in modifications.iterator()) {
                    notifyItemRangeChanged(range.startIndex, range.length)
                }
            }
        } else if (data is RealmList) {
            data.addChangeListener { _, changeSet ->
                //change Listener 초기화
                notifyDataSetChanged()

                // For deletions, the adapter has to be notified in reverse order.
                val deletions = changeSet.deletionRanges
                for (range in deletions.reversedArray()) {
                    notifyItemRangeChanged(range.startIndex, range.length)
                }

                val insertions = changeSet.insertionRanges
                for (range in insertions.iterator()) {
                    notifyItemRangeInserted(range.startIndex, range.length)
                }

                val modifications = changeSet.changeRanges
                for (range in modifications.iterator()) {
                    notifyItemRangeChanged(range.startIndex, range.length)
                }
            }
        } else {
            throw IllegalArgumentException("RealmCollection not supported this class")
        }
    }

    override fun getItemCount(): Int {
        return itemList?.size ?: 0
    }

    //return item found by position
    fun getItem(position: Int): T? {
        if (itemList == null) {
            return null
        }
        return itemList!![position] ?: throw IllegalStateException("position is invalid.")
    }


}