package com.free.freeway

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionLauncher = registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                println("마이크 권한을 허용하지 않으면 음성 검색 기능을 사용하실 수 없습니다!")
            }
        }

        val webView = findViewById<WebView>(R.id.web_view)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true

        webView.webViewClient = InternalWebViewClient()
        webView.loadUrl("http://freewaykr.s3-website.ap-northeast-2.amazonaws.com/")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }

        requestAudioPermission()
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher!!.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    private inner class InternalWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url
            return if (url != null && url.scheme == "tel") {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url.toString())))
                true
            } else if (url != null && url.scheme == "mailto") {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url.toString())))
                true
            } else {
                super.shouldOverrideUrlLoading(view, request)
            }
        }
    }
}