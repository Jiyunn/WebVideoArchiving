package blog.cmcmcmcm.webvideoarchiving.util

import android.view.View
import android.webkit.JavascriptInterface

class WebScriptInterface(
        val button: View){

    @JavascriptInterface
    fun showFloatingButton() {
        if (button.visibility == View.GONE) {
            button.visibility = View.VISIBLE
        }
    }

    @JavascriptInterface
    fun hideFloatingButton() {
        if (button.visibility == View.VISIBLE) {
            button.visibility = View.GONE
        }
    }

}