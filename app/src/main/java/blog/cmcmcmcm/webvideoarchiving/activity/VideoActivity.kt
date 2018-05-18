package blog.cmcmcmcm.webvideoarchiving.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.EditText
import blog.cmcmcmcm.webvideoarchiving.ArchivingApplication
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.data.Video
import blog.cmcmcmcm.webvideoarchiving.data.addTagAsync
import blog.cmcmcmcm.webvideoarchiving.data.getTagsByVideoId
import blog.cmcmcmcm.webvideoarchiving.databinding.ActivityVideoBinding
import blog.cmcmcmcm.webvideoarchiving.fragment.adapter.TagAdapter
import blog.cmcmcmcm.webvideoarchiving.util.PlayerEventListener
import blog.cmcmcmcm.webvideoarchiving.util.RxBus.RxBus
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackPreparer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import java.net.URLEncoder

class VideoActivity : AppCompatActivity(), PlaybackPreparer {

    lateinit var binding: ActivityVideoBinding
    val realm: Realm = Realm.getDefaultInstance()

    var player: SimpleExoPlayer? = null
    val bandWidthMeter = DefaultBandwidthMeter()
    lateinit var mediaDataSourceFactory: DataSource.Factory

    lateinit var trackSelector: DefaultTrackSelector

    var startPosition: Long = 0L
    var startWindow: Int = 0

    val keyPosition = "position"
    val keyWindow = "window"

    var video: Video? = null
    var url: String? = null

    val sharedUrl = "video_url" //링크 공유 혹은 접속 시 queryParameter.
    val sharedPoint = "seek_point"

    val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        binding.activity = this

        checkDeepLinkParameter()
        getOffBus()

        mediaDataSourceFactory =  buildDataSourceFactory(true)

        //재생하다 종료 된 지점이 있는지 찾기
        savedInstanceState?.let {
            startPosition = it.getLong(keyPosition)
            startWindow = it.getInt(keyWindow)
        }

        initRecyclerView()
    }

    //initialize recyclerView
    private fun initRecyclerView() {

        val adapter = TagAdapter().apply {

            if (video != null) {
                updateItems(getTagsByVideoId(realm, video?.id))
            }

            //태그 클릭 시 해당 지점으로 이동
            disposables.add(clickSubject.subscribe { data ->
                seekToTagPoint(data.point)
            })

            //태그 롱 클릭시 공유 하기
            disposables.add(longClickSubject.subscribe { data ->
                setShareMessage(url, data.text, data.point)
            })
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        binding.recyclerVideoTag.apply {
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            this.adapter = adapter
        }
    }


    /**
     * 공유 된 비디오 url 이 있는지 확인.
     */
    private fun checkDeepLinkParameter(): Boolean {
        val intent = intent
        //메니피스트에 정의한 액션과 같은 액션이라면,
        if (Intent.ACTION_VIEW == getIntent().action) {
            val uri = intent.data

            val videoURL = uri.getQueryParameter(sharedUrl)
            val seekPoint = uri.getQueryParameter(sharedPoint)

            if (videoURL != null) {
                url = videoURL
            }
            if (seekPoint != null) {
                startPosition = seekPoint.toLong()
            }
            return true
        }
        return false
    }


    //태그 포인트로 이동
    private fun seekToTagPoint(point: Long) {
        player?.let {
            if (point <= it.duration) {
                player?.seekTo(point) //포인트로 이동.
            } else {
                player?.seekTo(it.duration) //더 크게 태그를 잡으면 영사의 마지막으로 이동.
            }
        }
    }

    //공유할 영상 uri 설정
    private fun setShareMessage(videoURL: String?, tagMsg: String, point: Long) {
        //공유 할 uri
        val videoLink = URLEncoder.encode(videoURL, "UTF-8")
        val deepLink = "https://archive.ljy?video_url=$videoLink&seek_point=$point"

        shortDynamicLinkWithDomain(deepLink, videoLink, tagMsg) //메소드 호출.
    }


    private fun shortDynamicLinkWithDomain(uri: String, videoLink:String, msg: String) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(uri))
                .setDynamicLinkDomain(getString(R.string.link_dynamic))
                .setSocialMetaTagParameters(DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(getString(R.string.share_title))
                        .setImageUrl(Uri.parse(videoLink))
                        .setDescription(msg)
                        .build())
                .buildShortDynamicLink()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val shortLink = task.result.shortLink

                        //이너클래스에서 아우터클래스의 변수 변경 불가능. 그래서 여기서 호출함.
                        shareWithUser(shortLink.toString()) //공유하는 Chooser 나타냄.
                    }else if (task.isCanceled) {
                        Log.w("Dynamic", "Task is cancelled")
                    }
                }
    }


    //공유 인텐트 & chooser 생성
    private fun shareWithUser(shortUri:String) {
        val intent = Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, shortUri)
                .setType("text/plain")
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    //버스에서 데이터 가져오기
    private fun getOffBus() {
        val rxBus = RxBus.getInstance()

        disposables.add(rxBus.toObservable()
                .subscribe { data ->
                    if (data is Video) {
                        video = data
                        url = data.url
                    }
                })
    }

    //포지션 업데이트
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        updateStartPosition()

        outState?.putLong(keyPosition, startPosition)
        outState?.putInt(keyWindow, startWindow)
    }

    //시작 포지션 업데이트.
    fun updateStartPosition() {
        player?.let {
            startPosition = Math.max(0, it.currentPosition)
        }
    }

    //initialize player
     fun initPlayer() {
        if (player == null) {
            val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandWidthMeter)
            trackSelector = DefaultTrackSelector(trackSelectionFactory)

            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
                    .apply {
                        addListener(PlayerEventListener(this@VideoActivity, binding.playerVideo))
                        addAnalyticsListener(EventLogger(trackSelector))

                        val haveStartPosition = startWindow != C.INDEX_UNSET
                        if (haveStartPosition) {
                            seekTo(startWindow, startPosition)
                        }
                        prepare(buildMediaSource(url), !haveStartPosition, false)
                    }

            binding.playerVideo.requestFocus()
            binding.playerVideo.player = player
            binding.playerVideo.setPlaybackPreparer(this)
        }
    }


    private fun buildDataSourceFactory(useBandwidthMeter: Boolean): DataSource.Factory {
        return (application as ArchivingApplication)
                .buildDataSourceFactory(if (useBandwidthMeter) bandWidthMeter else null)
    }

    private fun buildMediaSource(url: String?): MediaSource {
        return ExtractorMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(Uri.parse(url))
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    //태그 입력하는 다이얼로그 띄움
    fun onAddTagClick() {
        val editText = EditText(this)

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.hint_input_tag))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_ok), { _ , _ ->
                    addTagAsync(realm, player!!.currentPosition, editText.text.toString(), video?.id)
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .create()
                .show()
    }

    override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23 || player == null) {
            initPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        disposables.dispose()
        realm.close()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    fun clearStartPosition() {
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    override fun preparePlayback() {
        initPlayer()
    }

}
