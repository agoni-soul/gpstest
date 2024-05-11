package com.soul.network

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.soul.base.BaseActivity
import com.soul.gpstest.R
import java.lang.Exception

class NetworkActivity : BaseActivity() {
    private val mWebView: WebView by lazy {
        findViewById(R.id.web_view)
    }

    private var mExitTime = 0L

    override fun getLayoutId(): Int = R.layout.activity_network

    override fun initView() {
        mWebView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                url?.let{
                    try {
                        if(it.startsWith("http") || it.startsWith("https")) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            return true
                        }
                    } catch (e: Exception) {
                        return true
                    }
                    mWebView.loadUrl(url)
                    return true
                }
                return false
            }
        }
        mWebView.settings.javaScriptEnabled = true
        mWebView.loadUrl("https://www.baidu.com")
    }

    override fun initData() {
    }
}