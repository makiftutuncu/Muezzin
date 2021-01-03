package com.mehmetakiftutuncu.muezzin.extension

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.AttrRes
import androidx.core.content.res.use

fun Context.themeColor(@AttrRes attrRes: Int): Int =
    obtainStyledAttributes(intArrayOf(attrRes)).use {
        it.getColor(0, Color.DKGRAY)
    }


fun Context.resourceColor(resource: Int): Int =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        resources.getColor(resource)
    } else {
        resources.getColor(resource, theme)
    }
