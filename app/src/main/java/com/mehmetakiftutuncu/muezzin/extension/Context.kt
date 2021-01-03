package com.mehmetakiftutuncu.muezzin.extension

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.core.content.res.use

fun Context.themeColor(@AttrRes attrRes: Int): Int {
    return obtainStyledAttributes(
        intArrayOf(attrRes)
    ).use {
        it.getColor(0, Color.BLACK)
    }
}