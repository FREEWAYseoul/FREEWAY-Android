package com.free.freeway

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    //    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val MICROPHONE_PERMISSION_REQUEST_CODE = 2

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* requestPermissionLauncher = registerForActivityResult(
             RequestPermission()
         ) { isGranted: Boolean ->
             if (!isGranted) {
                 println("마이크 권한을 허용하지 않으면 음성 검색 기능을 사용하실 수 없습니다!")
             }
         }*/

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
                val permission = Manifest.permission.ACCESS_FINE_LOCATION
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(permission),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                    print("위치 권한 허용")
                } else {
                    println("이미 위치 권한 허용됨")
                    callback.invoke(origin, true, false)
                }
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                if (request.resources.contains("android.webkit.resource.AUDIO_CAPTURE")) {
                    print("마이크 권한 요청")
                    val permission = Manifest.permission.RECORD_AUDIO
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            permission
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(permission),
                            MICROPHONE_PERMISSION_REQUEST_CODE
                        )
                        print("마이크 권한 허용")
                    } else {
                        print("이미 마이크 권한 허용")
                        request.grant(request.resources)
                    }
                }
            }
        }

        print("크롬 설정 끝")

//        requestAudioPermission()
    }

    /*private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher!!.launch(Manifest.permission.RECORD_AUDIO)
        }
    }*/

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