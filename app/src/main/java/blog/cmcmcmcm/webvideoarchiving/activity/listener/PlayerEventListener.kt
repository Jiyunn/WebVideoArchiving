package blog.cmcmcmcm.webvideoarchiving.activity.listener

import blog.cmcmcmcm.webvideoarchiving.common.activity.BaseVideoActivity
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.ui.PlayerView

class PlayerEventListener(val activity: BaseVideoActivity,
                          val playerView: PlayerView) : Player.DefaultEventListener(), PlayerEvent {


    override fun onPlayerError(error: ExoPlaybackException?) {
        if (isBehindLiveWindow(error)) {
            clearStartPosition()
            initPlayer()
        } else {
            updateStartPosition()
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
        updateStartPosition()

        if (playbackState == Player.STATE_READY) {
            playerView.hideController()
        }
    }

    override fun clearStartPosition() {
        activity.clearStartPosition()
    }

    override fun initPlayer() {
        activity.initPlayer()
    }

    override fun updateStartPosition() {
        activity.updateStartPosition()
    }


}