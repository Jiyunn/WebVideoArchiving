package blog.cmcmcmcm.webvideoarchiving.fragment

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.activity.MainActivity
import blog.cmcmcmcm.webvideoarchiving.activity.VideoActivity
import blog.cmcmcmcm.webvideoarchiving.adapter.ArchiveAdapter
import blog.cmcmcmcm.webvideoarchiving.data.Video
import blog.cmcmcmcm.webvideoarchiving.databinding.FragmentArchiveBinding
import blog.cmcmcmcm.webvideoarchiving.util.RxBus.RxBus
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where

class ArchiveFragment : Fragment() {

    lateinit var binding: FragmentArchiveBinding
    val realm: Realm = Realm.getDefaultInstance()
    val disposables: CompositeDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_archive, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initToolbar()
        initRecyclerView()
    }

    private fun initToolbar() {
        (activity as MainActivity).setSupportActionBar(binding.toolbar?.toolbarArchive)
    }

    //initialize recycler view
    private fun initRecyclerView() {
        val adapter = ArchiveAdapter(activity).apply {
            updateItems(realm.where<Video>().sort(Video::addedDate.name, Sort.DESCENDING).findAll())
            disposables.add(clickSubject
                    .subscribe { data ->
                        //상세정보 보는 화면으로 이동
                        val rxBus = RxBus.getInstance() //비디오 정보 담아서 보내기.
                        rxBus.takeBus(data)
                        Log.d("Archive", "bus data : ${data.url}")
                        startActivity(Intent(activity, VideoActivity::class.java))
                    })
        }

        val layoutManager = LinearLayoutManager(activity)

        binding.recyclerArchive.apply {
            this.adapter = adapter
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        }


    }


override fun onDestroy() {
    super.onDestroy()
    binding.recyclerArchive.adapter = null
    disposables.clear()
    realm.close()
}
}