package com.example.colortooth.interfaceJS

import android.content.Context
import android.webkit.JavascriptInterface
import com.example.colortooth.myWebViewHeight

class HeightInterface internal constructor(c: Context) {
    var mContext: Context = c

    var h = myWebViewHeight

    @get:JavascriptInterface
    val fromAndroid: Int
        get() = h
}