package blog.cmcmcmcm.webvideoarchiving.common.webkit

import android.view.View
import android.webkit.JavascriptInterface

class WebScriptInterface(val button: View) {


    @JavascriptInterface
    fun hideFloatingButton() {
        if (button.visibility == View.VISIBLE) {
            button.visibility = View.GONE
        }
    }

}