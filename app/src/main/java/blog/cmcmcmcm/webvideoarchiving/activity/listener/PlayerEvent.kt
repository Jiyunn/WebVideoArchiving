package blog.cmcmcmcm.webvideoarchiving.activity.listener

interface PlayerEvent {

    fun initPlayer()
    fun clearStartPosition()
    fun updateStartPosition()
}