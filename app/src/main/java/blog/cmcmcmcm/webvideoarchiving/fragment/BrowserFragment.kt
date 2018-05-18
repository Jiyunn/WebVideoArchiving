package blog.cmcmcmcm.webvideoarchiving.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.activity.MainActivity
import blog.cmcmcmcm.webvideoarchiving.data.addVideoAsync
import blog.cmcmcmcm.webvideoarchiving.databinding.FragmentBrowserBinding
import blog.cmcmcmcm.webvideoarchiving.fragment.webkit.JyWebChromeClient
import blog.cmcmcmcm.webvideoarchiving.fragment.webkit.JyWebClient
import blog.cmcmcmcm.webvideoarchiving.fragment.webkit.WebScriptInterface
import blog.cmcmcmcm.webvideoarchiving.util.hideSoftKeyboard
import io.realm.Realm

class BrowserFragment : Fragment() {

    lateinit var binding: FragmentBrowserBinding
    val realm: Realm = Realm.getDefaultInstance()

    var videoURL: String? = null

    var isFloatingCollectVisible = false
        set (visibility) {
            if (visibility) {
                binding.floatBrowserCollect.visibility = View.VISIBLE
            } else {
                binding.floatBrowserCollect.visibility = View.GONE
            }
        }

    var isFloatingNaviVisible = false
        set (visibility) {
            if (visibility) {
                binding.floatBrowserBack.visibility = View.VISIBLE
                binding.floatBrowserForward.visibility = View.VISIBLE
            } else {
                binding.floatBrowserBack.visibility = View.GONE
                binding.floatBrowserForward.visibility = View.GONE
            }
        }

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
    private fun goURL(enteredURL: String) {
        if (!enteredURL.contains("https://")) {
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

    //웹뷰 설정
    private fun initWebView() {
        defaultWebViewSetting()

        binding.web.apply {
            webViewClient = JyWebClient(this@BrowserFragment)
            webChromeClient = JyWebChromeClient(this@BrowserFragment)
            addJavascriptInterface(WebScriptInterface(binding.floatBrowserCollect), "App")
            loadUrl(getString(R.string.default_url))


            setOnScrollChangedCallback { _, t, _, oldt ->
                if (t > oldt) {
                    isFloatingNaviVisible = false
                } else if (t < oldt) {
                    isFloatingNaviVisible = true
                }
            }
        }
    }

    //비디오 URL 을 데이터베이스에 저장
    fun onCollectClick() {
        videoURL?.let {
            addVideoAsync(realm, it)
            Toast.makeText(activity, getString(R.string.msg_collect), Toast.LENGTH_SHORT).show()
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


    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}