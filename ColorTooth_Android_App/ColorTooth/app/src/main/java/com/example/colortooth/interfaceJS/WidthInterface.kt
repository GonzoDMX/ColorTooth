package com.example.colortooth.interfaceJS

import android.content.Context
import android.webkit.JavascriptInterface
import com.example.colortooth.myWebViewHeight
import com.example.colortooth.myWebViewWidth
import kotlinx.coroutines.CoroutineScope

class WidthInterface internal constructor(c: Context) {
    var mContext: Context = c

    var w = myWebViewWidth

    @get:JavascriptInterface
    val fromAndroid: Int
        get() = w
}