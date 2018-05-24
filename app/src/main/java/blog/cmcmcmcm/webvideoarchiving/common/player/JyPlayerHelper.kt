package blog.cmcmcmcm.webvideoarchiving.common.player

import android.content.Context
import android.media.session.PlaybackState
import android.net.Uri
import blog.cmcmcmcm.webvideoarchiving.data.Video
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class JyPlayerHelper(val context: Context?,
                     val video: Video,
                     val playView: PlayerView?) {


    val videoTrackSelection = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter()) //비디오 트랙셀렉션
    val trackSelector = DefaultTrackSelector(videoTrackSelection) //트랙셀렉터
    var player: SimpleExoPlayer?

    init {
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector) //플레이어 설정
        preparePlayer()

        playView?.player = player //플레이어 설정해 줌.
    }

    /**
     * 미디어 소스 가져오기
     */
    private fun getMediaSource(url: String?): MediaSource {
        val uri = Uri.parse(url)
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "ArchivingApplication"))

        //매개변수로 받은 url 을 미디어 소스로 설정 해줌.
        return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    //재생 준비
    fun preparePlayer() {
        player?.let {
            it.prepare(getMediaSource(video.url))
            it.seekTo(video.seeingPoint)
        }
    }

    //비디오 플레이하기.
    fun playVideo() {
        if (player?.playbackState == PlaybackState.STATE_PLAYING) {
            player?.playWhenReady = true
        }
    }

    fun stopPlayer() {
        player?.stop()
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}