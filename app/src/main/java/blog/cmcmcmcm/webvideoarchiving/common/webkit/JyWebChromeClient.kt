package blog.cmcmcmcm.webvideoarchiving.common.webkit

import android.webkit.WebChromeClient
import android.webkit.WebView

class JyWebChromeClient(val helper:WebClientHelper)  : WebChromeClient() {


    override fun onProgressChanged(view: WebView?, newProgress: Int) {

        if (newProgress < 100) { //새로운 페이지가 열리는 중!
            helper.videoURL?.let {
                helper.videoURL = null
            }
        }

        //  새로운 페이지로 들어감 의미.
        if ( !helper.isFloatingCollectVisible && helper.videoURL == null) {
            helper.isFloatingCollectVisible = false
        }
    }
}