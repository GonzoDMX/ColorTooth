package com.example.colortooth.utilities

import android.content.Context
import android.webkit.JavascriptInterface


class JSInterface internal constructor(c: Context) {
    var mContext: Context = c

    var jsonData = ""

    @get:JavascriptInterface
    val fromAndroid: String
        get() = "This is from android."
}