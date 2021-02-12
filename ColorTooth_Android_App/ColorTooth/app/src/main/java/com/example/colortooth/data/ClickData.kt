package com.example.colortooth.data

import android.icu.text.SimpleDateFormat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "click_table")
data class ClickData(
        @PrimaryKey(autoGenerate = true) val click_id: Int,
        @ColumnInfo(name = "step") var step: Int,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "alpha_val") val alpha: Int,
        @ColumnInfo(name = "red_val") val red: Int,
        @ColumnInfo(name = "green_val") val green: Int,
        @ColumnInfo(name = "blue_val") val blue: Int,
        @ColumnInfo(name = "time")
    val time: String = SimpleDateFormat("HH:mm:ss:SSS").format(Date())
)