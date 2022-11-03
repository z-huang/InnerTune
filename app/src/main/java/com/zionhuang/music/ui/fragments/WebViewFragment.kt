package com.zionhuang.music.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.edit
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.constants.Constants.ACCOUNT_EMAIL
import com.zionhuang.music.constants.Constants.ACCOUNT_NAME
import com.zionhuang.music.constants.Constants.INNERTUBE_COOKIE
import com.zionhuang.music.constants.Constants.VISITOR_DATA
import com.zionhuang.music.databinding.FragmentWebviewBinding
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.ui.fragments.base.BindingFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WebViewFragment : BindingFragment<FragmentWebviewBinding>() {
    override fun getViewBinding() = FragmentWebviewBinding.inflate(layoutInflater)

    @OptIn(DelicateCoroutinesApi::class)
    private val webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            if (url.startsWith("https://music.youtube.com")) {
                val cookies = CookieManager.getInstance().getCookie(url)
                if (sharedPreferences.getString(INNERTUBE_COOKIE, null) != cookies) {
                    sharedPreferences.edit {
                        putString(INNERTUBE_COOKIE, cookies)
                    }
                    GlobalScope.launch {
                        YouTube.getAccountInfo().onSuccess {
                            sharedPreferences.edit {
                                putString(ACCOUNT_NAME, it?.name)
                                putString(ACCOUNT_EMAIL, it?.email)
                            }
                        }.onFailure {
                            it.printStackTrace()
                        }
                    }
                }
            }
        }

        override fun onPageFinished(view: WebView, url: String?) {
            binding.webview.loadUrl("javascript:Android.onRetrieveVisitorData(window.yt.config_.VISITOR_DATA)")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webview.apply {
            if (savedInstanceState != null) {
                restoreState(savedInstanceState)
            } else {
                loadUrl("https://accounts.google.com/ServiceLogin?ltmpl=music&service=youtube&passive=true&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Faction_handle_signin%3Dtrue%26next%3Dhttps%253A%252F%252Fmusic.youtube.com%252F")
            }
            webViewClient = this@WebViewFragment.webViewClient
            settings.apply {
                javaScriptEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
            }
            addJavascriptInterface(this@WebViewFragment, "Android")
        }
    }

    @JavascriptInterface
    fun onRetrieveVisitorData(visitorData: String?) {
        if (visitorData != null && sharedPreferences.getString(VISITOR_DATA, null) != visitorData) {
            sharedPreferences.edit {
                putString(VISITOR_DATA, visitorData)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webview.saveState(outState)
    }
}