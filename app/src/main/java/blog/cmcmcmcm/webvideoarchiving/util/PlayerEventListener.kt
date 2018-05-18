package blog.cmcmcmcm.webvideoarchiving.util

import blog.cmcmcmcm.webvideoarchiving.activity.VideoActivity
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.ui.PlayerView

class PlayerEventListener(val videoAcivity: VideoActivity, val playerView: PlayerView) : Player.DefaultEventListener() {


    override fun onPlayerError(error: ExoPlaybackException?) {
        if (isBehindLiveWindow(error)) {
            videoAcivity.clearStartPosition()
            videoAcivity.initPlayer()
        } else {
            videoAcivity.updateStartPosition()
        }
    }

    private fun isBehindLiveWindow(error: ExoPlaybackException?): Boolean {
        if (error?.type != ExoPlaybackException.TYPE_SOURCE) {
            return false
        }
        var cause: Throwable? = error.sourceException
        while (cause != null) {
            if (cause is BehindLiveWindowException) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        videoAcivity.updateStartPosition()

        if (playbackState == Player.STATE_READY) {
            playerView.hideController()
        }
    }

}