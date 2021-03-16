package com.example.colortooth.utilities

import android.content.Context
import android.webkit.JavascriptInterface

class SizeJSInterface internal constructor(c: Context, width: Int, height: Int) {
    var mContext: Context = c

    var w = width
    var h = height

    @get:JavascriptInterface
    val fromAndroid: Array<Int>
        get() = arrayOf(w, h)
}