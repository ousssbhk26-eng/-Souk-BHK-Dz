package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.parseColor("#071327")),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE),
        )
        setContent {
            SoukBhkWebAppScreen(
                onExitApp = { finish() }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SoukBhkWebAppScreen(
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var fileChooserCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val callback = fileChooserCallback
        fileChooserCallback = null
        if (callback != null) {
            if (uri != null) {
                callback.onReceiveValue(arrayOf(uri))
            } else {
                callback.onReceiveValue(null)
            }
        }
    }

    fun launchExternalUri(uri: Uri) {
        val scheme = uri.scheme?.lowercase()
        val intent = if (scheme == "tel") {
            Intent(Intent.ACTION_DIAL, uri)
        } else {
            Intent(Intent.ACTION_VIEW, uri)
        }
        runCatching {
            context.startActivity(intent)
        }
    }

    BackHandler {
        val webView = webViewRef
        if (webView != null) {
            webView.evaluateJavascript("(function(){return window.handleAndroidBack ? window.handleAndroidBack() : false;})()") { result ->
                if (result != "true") {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        onExitApp()
                    }
                }
            }
        } else {
            onExitApp()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor(0xFF071327))
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(ComposeColor(0xFFF1F5F9))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("souk_bhk_webview_container")
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(Color.parseColor("#F1F5F9"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        setSupportMultipleWindows(true)
                        javaScriptCanOpenWindowsAutomatically = true
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url ?: return false
                            val scheme = url.scheme?.lowercase() ?: return false
                            if (scheme == "file" || scheme == "data" || scheme == "about") {
                                return false
                            }
                            launchExternalUri(url)
                            return true
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            fileChooserCallback?.onReceiveValue(null)
                            fileChooserCallback = filePathCallback
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                            return true
                        }

                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val href = view?.hitTestResult?.extra
                            if (!href.isNullOrBlank()) {
                                launchExternalUri(Uri.parse(href))
                                return false
                            }
                            val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                            val tempWebView = WebView(ctx).apply {
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        v: WebView?,
                                        req: WebResourceRequest?
                                    ): Boolean {
                                        req?.url?.let { launchExternalUri(it) }
                                        destroy()
                                        return true
                                    }
                                }
                            }
                            transport.webView = tempWebView
                            resultMsg.sendToTarget()
                            return true
                        }
                    }

                    loadUrl("file:///android_asset/index.html")
                    webViewRef = this
                }
            }
        )
    }
}
