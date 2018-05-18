package blog.cmcmcmcm.webvideoarchiving.fragment

import android.annotation.TargetApi
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.activity.MainActivity
import blog.cmcmcmcm.webvideoarchiving.data.addVideoAsync
import blog.cmcmcmcm.webvideoarchiving.databinding.FragmentBrowserBinding
import blog.cmcmcmcm.webvideoarchiving.util.WebScriptInterface
import blog.cmcmcmcm.webvideoarchiving.util.hideSoftKeyboard
import io.realm.Realm
import java.io.InputStream

class BrowserFragment : Fragment() {

    lateinit var binding: FragmentBrowserBinding
    val realm: Realm = Realm.getDefaultInstance()

    var videoURL: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_browser, container, false)
        binding.fragment = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initToolbar()
        initWebView()
    }

    private fun initToolbar() {
        (activity as MainActivity).setSupportActionBar(binding.toolbar?.toolbarBrowser)
        binding.toolbar?.apply {
            editToolbarBrowser.setOnEditorActionListener { view, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (view != null) {
                        hideSoftKeyboard(context, view) //소프트 키보드 자동으로 내림.
                        goURL(editToolbarBrowser.text.toString())
                    }
                }
                true
            }

            imgBtnRefresh.setOnClickListener {
                binding.web.reload()
            }
        }
    }

    //사용자가 입력한 주소로 이동
    private fun goURL(enteredURL : String) {
        if ( !enteredURL.contains("https://")) {
            binding.web.loadUrl("https://$enteredURL")
        }
    }


    private fun defaultWebViewSetting() {
        binding.web.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            setSupportZoom(false)
            setAppCacheEnabled(true)
        }
    }


    //    //웹뷰 설정
    private fun initWebView() {
        defaultWebViewSetting()

        binding.web.apply {
            webViewClient = MyWebViewClient()
            webChromeClient = MyWebChromeClient()
            addJavascriptInterface(WebScriptInterface(binding.floatBrowserCollect), "App")
            loadUrl("https://m.tv.naver.com")


            setOnScrollChangedCallback { l, t, oldl, oldt ->
                if (t > oldt) {
                    setFloatingVisibility(binding.floatBrowserBack, false)
                    setFloatingVisibility(binding.floatBrowserForward, false)
                } else if (t < oldt) {

                    setFloatingVisibility(binding.floatBrowserBack, true)
                    setFloatingVisibility(binding.floatBrowserForward, true)
                }
            }
        }
    }


    //비디오 URL 을 데이터베이스에 저장
    fun onCollectClick( view : View) {
        videoURL?.let {
            addVideoAsync(realm, it)
        }
    }

    //브라우저 뒤로가기, 앞으로 가기.
    fun onBrowserNavigateClick(view: View) {
        when (view.id) {
            R.id.float_browser_back -> {
                if (binding.web.canGoBack()) {
                    binding.web.goBack()
                }
            }
            R.id.float_browser_forward -> {
                if (binding.web.canGoForward()) {
                    binding.web.goForward()
                }
            }
        }
    }

    //웹뷰 크롬 클라이언트
    inner class MyWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {

            if (newProgress < 100) { //새로운 페이지가 열리는 중!
                videoURL?.let {
                    videoURL = null
                }
            }

            //  새로운 페이지로 들어감 의미.
            if (isFloatingVisible() && videoURL == null) {
                binding.floatBrowserCollect.visibility = View.GONE
            }
        }
    }
    //플로팅 버튼의 보이는 지 여부 반환
    fun isFloatingVisible(): Boolean {
        if (binding.floatBrowserCollect.visibility == View.VISIBLE) {
            return true
        }
        return false
    }

    //플로팅 버튼의 가시성 설정
    fun setFloatingVisibility(button: View, visible: Boolean) {
        if (visible) {
            button.visibility = View.VISIBLE
        } else {
            button.visibility = View.GONE
        }
    }

    inner class MyWebViewClient : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            injectScriptFile(view, "js/script.js")
            binding.toolbar?.editToolbarBrowser?.setText(binding.web.url.toString()) //url 텍스트 설정해줌.
            binding.web.requestFocus()
        }


        override fun onLoadResource(view: WebView?, url: String?) {
            val directory: String = url.toString()

            //광고를 제외한 비디오
            if (directory.contains(".mp4") && !directory.contains("/video/")) {
                videoURL = url

                Log.d("WebView", "current url $videoURL")
                if (!isFloatingVisible()) { //플로팅 버튼이 안보일 때.
                    setFloatingVisibility(binding.floatBrowserCollect, true)
                }
            }
        }

        //새로운 페이지가 요청될 때만 호출됨. URL 이 변경되야함.
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.web.requestFocus()

            if (isFloatingVisible()) { //플로팅 버튼이 안보일 때.
                setFloatingVisibility(binding.floatBrowserCollect, false)
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

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}