package com.example.AIEye

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {
    private var email: String? = null
    private var password: String? = null
    private var isFirst = true

    companion object {
        const val ADDRESS = "**********************"
        var CameraId = 0
    }

    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webView = findViewById(R.id.webView)

        email = intent.getStringExtra("email")
        password = intent.getStringExtra("password")

        setWebView()

        //앱이 켜지면 서비스 해제
        stopService(Intent(this, MessagingService::class.java))

        //웹뷰 시작
        webView.loadUrl("https://$ADDRESS:8101/Identity/Account/Login")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebView() {

        val webViewSettings = webView.settings
        webViewSettings.javaScriptEnabled = true  //자바 스크립트를 쓸수 있게
        webViewSettings.mediaPlaybackRequiresUserGesture = false //웹뷰에서 재생가능한 콘텐츠를 자동으로 재생할 수 있도록 설정
        webViewSettings.databaseEnabled = true  // 웹뷰 내부 DB 사용 가능
        webViewSettings.setSupportMultipleWindows(false) // 여러 창 또는 탭 열리는 것 비허용
        webViewSettings.loadWithOverviewMode = true //컨텐츠가 웹뷰보다 클때 스크린크기에 맞추기
        webViewSettings.cacheMode = WebSettings.LOAD_NO_CACHE   // 캐시 사용 안함
        webViewSettings.domStorageEnabled = true // 로컬스토리지 사용 허용

        //ssl 에러 무시
        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                // WebView 에서 JavaScript 코드 활성화
                if (url == "https://$ADDRESS:8101/Identity/Account/Login" && isFirst) {
                    view?.loadUrl(
                        "javascript:document.getElementById('email-input').value = '$email';document.getElementById('password-input').value='$password';document.getElementById('login-submit').click();"
                    )
                    isFirst = false
                }

                //로그인 성공
                if (url == "https://$ADDRESS:8101/" && CameraId != 0) {
                    view?.loadUrl("https://$ADDRESS:8101/camera/$CameraId/event")
                    CameraId = 0
                }
                super.onPageFinished(view, url)
            }

        }

        //웹뷰에서 사용하는 권한들 요청 수락
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
    }

    //뒤로가기 버튼이 웹에서도 적용되게 변경
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        //앱이 꺼지면 서비스 등록
        startService(Intent(this, MessagingService::class.java))

        //웹뷰가 가진 리소스 해제
        webView.stopLoading()
        webView.webChromeClient = null
        webView.webViewClient = object : WebViewClient() {}
        webView.settings.javaScriptEnabled = false
        webView.clearHistory()
        webView.clearCache(true)
        webView.loadUrl("about:blank")
        webView.removeAllViews()
        webView.destroy()

        super.onDestroy()
    }
}