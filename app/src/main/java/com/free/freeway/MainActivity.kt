package com.free.freeway

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private val locationPermissionRequestCode = 1
    private var geolocationCallback: GeolocationPermissions.Callback? = null
    private var microphonePermissionRequest: PermissionRequest? = null

    private val requestMicrophonePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                microphonePermissionRequest?.grant(microphonePermissionRequest?.resources)
            } else {
                print("마이크 권한 거절됨")
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.web_view)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.databaseEnabled = true
        webSettings.domStorageEnabled = true

        webView.webViewClient = InternalWebViewClient()
        webView.loadUrl("https://freeway-web.vercel.app/")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                geolocationCallback = callback

                val permission = Manifest.permission.ACCESS_FINE_LOCATION
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(permission),
                        locationPermissionRequestCode
                    )
                } else {
                    geolocationCallback?.invoke(origin, true, false)
                }
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                if (request.resources.contains("android.webkit.resource.AUDIO_CAPTURE")) {
                    microphonePermissionRequest = request

                    print("마이크 권한 요청")
                    val permission = Manifest.permission.RECORD_AUDIO
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestMicrophonePermissionLauncher.launch(permission)
                        print("마이크 권한 허용")
                    } else {
                        print("이미 마이크 권한 허용")
                        request.grant(request.resources)
                    }
                }
            }
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