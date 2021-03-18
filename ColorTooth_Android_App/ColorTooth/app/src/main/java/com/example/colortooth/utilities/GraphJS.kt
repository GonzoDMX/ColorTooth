package com.example.colortooth.utilities

import android.R.attr.data
import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter


object GraphJS {

    private var timeArr = Array<Int>(25, init = { i: Int -> 0 })
    private var alphaArr = Array<Int>(25, init = { i: Int -> 0 })
    private var redArr = Array<Int>(25, init = { i: Int -> 0 })
    private var greenArr = Array<Int>(25, init = { i: Int -> 0 })
    private var blueArr = Array<Int>(25, init = { i: Int -> 0 })

    private var index = 0

    private var jsonCOLS = JSONArray()

    init {
        var timeJ = JSONObject()
        timeJ.put("label", "Time")
        timeJ.put("type", "string")

        var alphaJ = JSONObject()
        alphaJ.put("label", "Alpha")
        alphaJ.put("type", "number")

        var redJ = JSONObject()
        redJ.put("label", "Red")
        redJ.put("type", "number")

        var greenJ = JSONObject()
        greenJ.put("label", "Green")
        greenJ.put("type", "number")

        var blueJ = JSONObject()
        blueJ.put("label", "Blue")
        blueJ.put("type", "number")

        jsonCOLS.put(timeJ)
        jsonCOLS.put(alphaJ)
        jsonCOLS.put(redJ)
        jsonCOLS.put(greenJ)
        jsonCOLS.put(blueJ)
    }

    fun pullJSON() : JSONObject {
    //fun pullJSON(): String {

        var jsonMAIN = JSONObject()
        var jsonROWS = JSONArray()

        for (i in 0..24) {
            var jsonC = JSONObject()
            var columns = JSONArray()

            var t = JSONObject().put("v", timeArr[index].toString())
            var a = JSONObject().put("v", alphaArr[index])
            var r = JSONObject().put("v", redArr[index])
            var g = JSONObject().put("v", greenArr[index])
            var b = JSONObject().put("v", blueArr[index])

            columns.put(t)
            columns.put(a)
            columns.put(r)
            columns.put(g)
            columns.put(b)
            jsonC.put("c", columns)
            jsonROWS.put(jsonC)
            index = if (index < 24) (index + 1) else 0
        }

        jsonMAIN.put("cols", jsonCOLS)
        jsonMAIN.put("rows", jsonROWS)

        val jsonParent = JSONObject()
        jsonParent.put("main", jsonMAIN)

        Log.d("INIT_TEST", jsonParent.getString("main"))
        //Log.d("INIT_TEST", jsonMAIN.getString("rows"))
        return jsonMAIN
        //return header
    }

    fun updateJSON(t: Int, a: Int, r: Int, g: Int, b: Int) {
        timeArr[index] = t
        alphaArr[index] = a
        redArr[index] = r
        greenArr[index] = g
        blueArr[index] = b
        index = if (index < 24) (index + 1) else 0
    }

    fun reInitJSON() {
        for (i in 0..24) {
            timeArr[i] = 0
            alphaArr[i] = 0
            redArr[i] = 0
            greenArr[i] = 0
            blueArr[i] = 0
        }
    }
}