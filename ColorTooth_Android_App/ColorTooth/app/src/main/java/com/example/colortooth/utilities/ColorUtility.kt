package com.example.colortooth.utilities

import android.graphics.Color


fun nameOfColor(alpha: Int, red: Int, green: Int, blue: Int) : String {
    var colorName: String = "Black"

    // Convert input values to color type
    val inColor = Color.argb(alpha, red, green, blue)
    // Declare float array to store HSV
    val hsv = FloatArray(3)
    // Convert input color to HSV
    Color.colorToHSV(inColor, hsv)

    // Check if Black
    if(hsv[2] > 0.15) {
        // Check if White
        if (hsv[1] > 0.2) {
            colorName = when (hsv[0]) {
                in 0.0..20.0 -> "Red"
                in 20.0..50.0 -> "Orange"
                in 50.0..70.0 -> "Yellow"
                in 70.0..155.0 -> "Green"
                in 155.0..195.0 -> "Cyan"
                in 195.0..225.0 -> "Blue"
                in 225.0..255.0 -> "Indigo"
                in 255.0..285.0 -> "Violet"
                in 285.0..340.0 -> "Magenta"
                in 340.0..360.0 -> "Red"
                else -> "Error"
            }
        } else {
            colorName = "White"
        }
    }
    return colorName
}