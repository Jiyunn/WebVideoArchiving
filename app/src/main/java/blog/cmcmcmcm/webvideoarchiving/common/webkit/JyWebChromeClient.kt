package blog.cmcmcmcm.webvideoarchiving.common.webkit

import android.webkit.WebChromeClient
import android.webkit.WebView
import blog.cmcmcmcm.webvideoarchiving.fragment.BrowserFragment

class JyWebChromeClient(val context : BrowserFragment)  : WebChromeClient() {


    override fun onProgressChanged(view: WebView?, newProgress: Int) {

        if (newProgress < 100) { //새로운 페이지가 열리는 중!
            context.videoURL?.let {
                context.videoURL = null
            }
        }

        //  새로운 페이지로 들어감 의미.
        if ( !context.isFloatingCollectVisible && context.videoURL == null) {
            context.isFloatingCollectVisible = false
        }
    }
}