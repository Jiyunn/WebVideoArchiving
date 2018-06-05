package blog.cmcmcmcm.webvideoarchiving.common.webkit

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.InputStream

class JyWebClient(val helper:WebClientHelper ) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        injectScriptFile(view, "js/script.js")
        helper.setURLTextInToolbar(view?.url.toString()) //url 텍스트 설정해줌.
        view?.requestFocus()
    }


    override fun onLoadResource(view: WebView?, url: String?) {
        url?.let {
            //광고를 제외한 비디오
            if (it.contains(".mp4") && !it.contains("/video/")) {
                helper.videoURL = url //비디오 url

                if (!helper.isFloatingCollectVisible) { //플로팅 버튼이 안보일 때.
                    helper.isFloatingCollectVisible = true
                }
            }
        }
    }

    //새로운 페이지가 요청될 때만 호출됨. URL 이 변경되야함.
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.requestFocus()

        if (helper.isFloatingCollectVisible) { //플로팅 버튼이 보일 때
            helper.isFloatingCollectVisible = false
        }
    }


    @SuppressWarnings("deprecation")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view?.loadUrl(url)

        return true
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        view?.loadUrl(request?.url.toString())

        return true
    }

    /**
     * 스크립트 파일 삽입
     */
    private fun injectScriptFile(view: WebView?, scriptFile: String) {
        view?.context?.assets?.let {
            val inputStream: InputStream = it.open(scriptFile)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()

            val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)

            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    "script.innerHTML = window.atob('$encoded');" +
                    "parent.appendChild(script)" +
                    "})()")
        }
    }
}
