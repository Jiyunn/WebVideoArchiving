package blog.cmcmcmcm.webvideoarchiving.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.EditText
import blog.cmcmcmcm.webvideoarchiving.ArchivingApplication
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.activity.listener.PlayerEvent
import blog.cmcmcmcm.webvideoarchiving.activity.listener.PlayerEventListener
import blog.cmcmcmcm.webvideoarchiving.common.rx.RxBus
import blog.cmcmcmcm.webvideoarchiving.data.Video
import blog.cmcmcmcm.webvideoarchiving.data.addTagAsync
import blog.cmcmcmcm.webvideoarchiving.data.getTagsByVideoId
import blog.cmcmcmcm.webvideoarchiving.databinding.ActivityVideoBinding
import blog.cmcmcmcm.webvideoarchiving.fragment.adapter.TagAdapter
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

class VideoActivity : AppCompatActivity(), PlaybackPreparer, PlayerEvent {

    lateinit var binding: ActivityVideoBinding
    var realm: Realm = Realm.getDefaultInstance()

    private var player: SimpleExoPlayer? = null
    private val bandWidthMeter = DefaultBandwidthMeter()
    private lateinit var mediaDataSourceFactory: DataSource.Factory

    private lateinit var trackSelector: DefaultTrackSelector

    private var startPosition: Long = 0L
    private var startWindow: Int = 0

    private val keyPosition = "position"
    private val keyWindow = "window"

    private var url: String? = null
    private var videoId: String? = null

    //링크 공유 및 접속 시 필요한 query parameter.
    private val sharedUrl = "video_url"
    private val sharedPoint = "seek_point"

    private val disposables: CompositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        binding.activity = this

        mediaDataSourceFactory = buildDataSourceFactory(true)

        //재생하다 종료 된 지점이 있는지 찾기
        savedInstanceState?.let {
            startPosition = it.getLong(keyPosition)
            startWindow = it.getInt(keyWindow)
        }

        //딥링크로 들어왔는지 확인하고, 아닐 경우 bus 로 전송된 데이터 받음.
        if (!checkDeepLinkParameter() && url==null) {
            getOffBus()
        }
    }

    private fun initRecyclerView() {

        val adapter = TagAdapter().apply {
            updateItems(getTagsByVideoId(realm, videoId))

            //태그 클릭 시 해당 지점으로 이동
            disposables.add(clickSubject.subscribe { data ->
                seekToTagPoint(data.point)
            })

            //태그 롱 클릭시 공유 하기
            disposables.add(longClickSubject.subscribe { data ->
                setShareMessage(url, data.text, data.point)
            })
        }

        val layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
            isSmoothScrollbarEnabled = true
        }

        binding.recyclerVideoTag.apply {
            this.layoutManager = layoutManager
            setHasFixedSize(true)
            this.adapter = adapter
        }
    }


    /**
     * 사용자가 딥링크를 통해 들어온 경우, 링크에 담긴 정보를 추출함.
     */
    private fun checkDeepLinkParameter(): Boolean {
        val intent = intent

        //Manifest 에 등록한 action
        if (Intent.ACTION_VIEW == getIntent().action) {
            intent.data.apply {
                // query 에서 비디오 URL 과 재생 지점 추출
                val videoURL = getQueryParameter(sharedUrl)
                val seekPoint = getQueryParameter(sharedPoint)

                if (videoURL != null && seekPoint != null) {
                    url = videoURL
                    startPosition = seekPoint.toLong() //영상의 시작 포지션을 설정해줌.
                }
            }
            return true
        }
        return false
    }


    /**
     *  사용자가 지정한 태그 지점으로 이동.
     */
    private fun seekToTagPoint(point: Long) {
        player?.let {
            if (point <= it.duration) player?.seekTo(point)
        }
    }


    /**
     * 메세지와 함께 URL 공유하는 메소드.
     */
    private fun setShareMessage(videoURL: String?, tagMsg: String, point: Long) {

        //비디오 url 의 인코딩 타입 지정해줌
        val videoLink = URLEncoder.encode(videoURL, "UTF-8")
        val deepLink = "https://archive.ljy?video_url=$videoLink&seek_point=$point"

        //다이나믹 링크로 만들어주는 메소드
        shortDynamicLinkWithDomain(deepLink, videoLink, tagMsg)
    }


    private fun shortDynamicLinkWithDomain(uri: String, videoLink: String, msg: String) {
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

                        //outer class 의 변수를 참조할 수 없어 여기서 호출함
                        //공유 Chooser 보여주는 메소드
                        shareWithUser(shortLink.toString())
                    }
                }
    }

    //공유 인텐트 & chooser 생성
    private fun shareWithUser(shortUri: String) {
        val intent = Intent(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, shortUri)
                .setType("text/plain")
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    //버스에서 데이터 가져오기
    private fun getOffBus() {
        RxBus.getInstance().apply {
            disposables.add(toObservable()
                    .subscribe { data ->
                        if (data is Video) {
                            videoId = data.id
                            url = data.url
                        }
                    })
        }
    }


    //지점 정하기
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        updateStartPosition()

        outState?.apply {
            putLong(keyPosition, startPosition)
            putInt(keyWindow, startWindow)
        }
    }

    //시작 지점 업데이트.
    override fun updateStartPosition() {
        player?.let {
            startPosition = Math.max(0, it.currentPosition)
        }
    }


    override fun initPlayer() {
        if (player == null) {
            trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandWidthMeter))

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

            //this@를 붙여줄 것.
            binding.playerVideo.apply {
                requestFocus()
                player = this@VideoActivity.player
                setPlaybackPreparer(this@VideoActivity)
            }
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
                .setPositiveButton(getString(R.string.dialog_ok), { _, _ ->
                    addTagAsync(realm, player!!.currentPosition, editText.text.toString(), videoId)
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
        initRecyclerView()
    }

    private fun releaseResources() {
        disposables.dispose()
        if (!realm.isClosed) {
            realm.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        releaseResources()
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) {
            initPlayer()
            if (realm.isClosed) {
                realm = Realm.getDefaultInstance()
            }
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
            releaseResources()
        }
    }

    override fun onBackPressed() {
        releaseResources()

        super.onBackPressed()
    }

    override fun clearStartPosition() {
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    override fun preparePlayback() {
        initPlayer()
    }

}
