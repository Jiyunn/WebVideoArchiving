package blog.cmcmcmcm.webvideoarchiving.activity.listener

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.ui.PlayerView

class PlayerEventListener(val event: PlayerEvent,
                          val playerView: PlayerView) : Player.DefaultEventListener() {


    override fun onPlayerError(error: ExoPlaybackException?) {
        if (isBehindLiveWindow(error)) {
            event.clearStartPosition()

            event.initPlayer()
        } else {
            event.updateStartPosition()
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
        event.updateStartPosition()

        if (playbackState == Player.STATE_READY) {
            playerView.hideController()
        }
    }




}