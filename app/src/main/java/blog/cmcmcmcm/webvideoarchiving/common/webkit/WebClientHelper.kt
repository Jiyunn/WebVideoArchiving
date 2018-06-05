package blog.cmcmcmcm.webvideoarchiving.common.webkit

interface WebClientHelper {

    var videoURL:String?
    var isFloatingCollectVisible:Boolean

    fun setURLTextInToolbar(text:String)

}