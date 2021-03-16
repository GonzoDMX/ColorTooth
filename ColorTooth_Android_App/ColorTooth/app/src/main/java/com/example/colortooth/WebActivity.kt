package com.example.colortooth

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.colortooth.databinding.ActivityWebchartBinding
import com.example.colortooth.utilities.JSInterface
import com.example.colortooth.utilities.SizeJSInterface


class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebchartBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webchart)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        binding = ActivityWebchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myWebViewWidth = binding.webViewChart.width
        val myWebViewHeight = binding.webViewChart.height
        val webSettings = binding.webViewChart.settings
        binding.webViewChart.requestFocusFromTouch()
        //binding.webViewChart.loadDataWithBaseURL("file:///android_asset/lineChart.html", content, "text/html", "utf-8", null)
        val myWebView = binding.webViewChart as WebView
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        // Test retrieving data from a kotlin JavaScript Interface
        myWebView.addJavascriptInterface(JSInterface(this), "Android")
        myWebView.addJavascriptInterface(SizeJSInterface(this, myWebViewWidth, myWebViewHeight), "AndSize")
        myWebView.loadUrl("file:///android_asset/lineChart.html")

        //Test triggering a javascript function from android / kotlin
        binding.buttonBack.setOnClickListener {
            myWebView.loadUrl("javascript:testIt()")
        }


    }
}